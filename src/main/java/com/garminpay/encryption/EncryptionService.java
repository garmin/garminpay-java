package com.garminpay.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.garminpay.exception.GarminPayEncryptionException;
import com.garminpay.model.CardData;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;

import java.text.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

class EncryptionService {
    private static final JWEAlgorithm ALGORITHM = JWEAlgorithm.ECDH_ES_A256KW;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Encrypts a CardData object for end to end payload encryption.
     *
     * @param cardData The CardData for card registration
     * @param publicKey server key for encrypting sensitive data
     * @return string representing the JWE of cardData
     */
    public String encryptCardData(@NonNull CardData cardData, @NonNull String publicKey) {
        ECDHEncrypter encryptor = createECDHEncryptorFromPublicKey(publicKey);
        String serializeCardData = serializeCardData(cardData);
        Payload payload = new Payload(serializeCardData);
        JWEObject jwe = createJWEObject(payload);

        try {
            jwe.encrypt(encryptor);
            return jwe.serialize();
        } catch (JOSEException e) {
            throw new GarminPayEncryptionException("Unable to encrypt JWE object", e);
        }
    }

    /**
     * Creates an ECDHEncrypter instance using the provided public key.
     *
     * @param publicKey The public key string used to create the ECDHEncrypter.
     * @return An ECDHEncrypter instance created from the provided public key.
     */
    private ECDHEncrypter createECDHEncryptorFromPublicKey(String publicKey) {
        try {
            JWK jwk = JWK.parse(publicKey);
            ECKey ecKey = (ECKey) jwk;
            return new ECDHEncrypter(ecKey);
        } catch (JOSEException | ParseException e) {
            throw new GarminPayEncryptionException("Invalid public key. Only ECC keys accepted.", e);
        }
    }

    /**
     * Creates a JWEObject with the given payload.
     *
     * @param payload The payload to be encapsulated in the JWEObject.
     * @return A JWEObject encapsulating the provided payload.
     */
    private JWEObject createJWEObject(Payload payload) {
        return new JWEObject(
            new JWEHeader.Builder(ALGORITHM, EncryptionMethod.A256GCM)
                .contentType("application/jwe")
                .build(),
            payload);
    }

    /**
     * Serializes a CardData object and returns it.
     *
     * @param cardData The CardData to serialize
     * @return String of serialized CardData object
     */
    private String serializeCardData(CardData cardData) {
        try {
            // Serialize CardData object to JSON
            return mapper.writeValueAsString(cardData);
        } catch (JsonProcessingException e) {
            throw new GarminPayEncryptionException("Could not map card data to JSON", e);
        }
    }
}
