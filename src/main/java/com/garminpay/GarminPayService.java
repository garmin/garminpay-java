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

@Slf4j
class GarminPayService {
    private final GarminPayProxy garminPayProxy;
    private final EncryptionService encryptionService = new EncryptionService();

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
        garminPayProxy.refreshRootLinks();

        // * Generate a new key
        log.debug("Generating new key");
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

        ExchangeKeysResponse exchangeKeysResponse = garminPayProxy.exchangeKeys(clientPublicKey);

        // * Obtain shared secret
        SecretKey secretKey = encryptionService.generateKeyAgreement(
            exchangeKeysResponse.getServerPublicKey(),
            clientPrivateKey
        );

        String encryptedCardData = encryptionService.encryptCardData(
            garminPayCardData, secretKey, exchangeKeysResponse.getKeyId()
        );

        RegisterCardResponse registerCardResponse = garminPayProxy.registerCard(encryptedCardData);

        if (registerCardResponse != null
            && registerCardResponse.getDeepLinkUrl() != null
            && !registerCardResponse.getDeepLinkUrl().isEmpty()) {
            return registerCardResponse.getDeepLinkUrl();
        }
        log.warn("Expected deeplink URL was null or empty");
        throw new GarminPaySDKException("Expected deeplink URL was null or empty");
    }
}
