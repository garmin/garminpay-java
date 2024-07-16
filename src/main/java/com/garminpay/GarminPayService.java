package com.garminpay;

import com.garminpay.encryption.EncryptionService;
import com.garminpay.exception.GarminPayEncryptionException;
import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.GarminPayCardData;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.proxy.GarminPayProxy;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Slf4j
class GarminPayService {
    private final GarminPayProxy garminPayProxy;
    private final EncryptionService encryptionService = new EncryptionService();
    private ExchangeKeysResponse exchangeKeysObject = null;
    private SecretKey secretKey = null;
    private static final int OVERDUE_HOURS_THRESHOLD = 4;

    GarminPayService(GarminPayProxy garminPayProxy) {
        this.garminPayProxy = garminPayProxy;
    }

    /**
     * Registers a card with the Garmin Pay platform.
     *
     * @param garminPayCardData Card to be registered
     * @return String containing a deep link url to the Garmin Connect Mobile app
     */
    public String registerCard(GarminPayCardData garminPayCardData) {
        garminPayProxy.refreshRootLinks(); // Refresh root links for proxy

        if (this.exchangeKeysObject == null || isTimestampOverdue(this.exchangeKeysObject.getCreatedTs())) {
            refreshKeys(); // secretKey is set in here as well
        }
        log.debug("Proceeding with valid keys");

        RegisterCardResponse registerCardResponse = garminPayProxy.registerCard(
            encryptionService.encryptCardData(
                garminPayCardData,
                secretKey,
                exchangeKeysObject.getKeyId()
            )
        );

        if (registerCardResponse != null
            && registerCardResponse.getDeepLinkUrl() != null
            && !registerCardResponse.getDeepLinkUrl().isEmpty()) {
            return registerCardResponse.getDeepLinkUrl();
        }
        log.warn("Expected deeplink URL was null or empty");
        throw new GarminPaySDKException("Expected deeplink URL was null or empty");
    }

    // Does not check validity of keys when they are received
    private void refreshKeys() {
        // Generate a new key
        log.debug("Refreshing key agreement with GarminPay");
        String clientPublicKey;
        String clientPrivateKey;
        try {
            ECKey key;
            ECKeyGenerator generator = new ECKeyGenerator(Curve.P_256);
            key = generator.generate();

            clientPublicKey = String.valueOf(Hex.encodeHex(key.toPublicKey().getEncoded()));
            clientPrivateKey = String.valueOf(Hex.encodeHex(key.toPrivateKey().getEncoded()));
        } catch (JOSEException e) {
            log.warn("Failed to generate key");
            throw new GarminPayEncryptionException("Failed to generate client key", e);
        }

        // Exchange keys
        exchangeKeysObject = garminPayProxy.exchangeKeys(clientPublicKey);

        // Obtain shared secret
        secretKey = encryptionService.generateKeyAgreement(
            exchangeKeysObject.getServerPublicKey(),
            clientPrivateKey
        );
    }

    /**
     * Checks a UTC timestamp to see if it is 4 or more hours overdue.
     *
     * @param createdTs UTC timestamp to be checked
     * @return true if the timestamp is overdue, false otherwise
     */
    private boolean isTimestampOverdue(String createdTs) {
        log.debug("Checking if key creation timestamp is 4 or more hours overdue");

        try {
            long hoursBetween = ChronoUnit.HOURS.between(
                Instant.now(),
                Instant.parse(createdTs)
            );
            log.debug("Existing key is {} hours overdue", hoursBetween);
            return hoursBetween >= OVERDUE_HOURS_THRESHOLD;
        } catch (DateTimeParseException e) {
            log.warn("Could not parse key creation timestamp: {} marking key as overdue", createdTs);
            return true;
        }
    }
}
