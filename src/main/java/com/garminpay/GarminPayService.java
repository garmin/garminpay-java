package com.garminpay;

import com.garminpay.encryption.EncryptionService;
import com.garminpay.exception.GarminPayEncryptionException;
import com.garminpay.model.GarminPayCardData;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.proxy.GarminPayProxy;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;

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
        ECKey key;
        try {
            ECKeyGenerator generator = new ECKeyGenerator(Curve.P_256);
             key = generator.generate();
        } catch (JOSEException e) {
            throw new GarminPayEncryptionException("Failed to generate client key", e);
        }

        String clientPublicKey;
        String clientPrivateKey;
        try {
            clientPublicKey = String.valueOf(Hex.encodeHex(key.toPublicKey().getEncoded()));
            clientPrivateKey = String.valueOf(Hex.encodeHex(key.toPrivateKey().getEncoded()));
        } catch (JOSEException e) {
            throw new GarminPayEncryptionException(
                "ECC key pair generation failed. Could not get public or private ECKeys"
            );
        }

        ExchangeKeysResponse exchangeKeysResponse = garminPayProxy.exchangeKeys(clientPublicKey);

        // * Obtain shared secret
        String serverPublicKey = exchangeKeysResponse.getServerPublicKey();

        SecretKey secretKey = encryptionService.generateSharedSecret(
            serverPublicKey, clientPrivateKey
        );

        String encryptedCardData = encryptionService.encryptCardData(
            garminPayCardData, secretKey, exchangeKeysResponse.getKeyId()
        );

        RegisterCardResponse registerCardResponse = garminPayProxy.registerCard(encryptedCardData);

        return registerCardResponse.getDeepLinkUrl();
    }
}
