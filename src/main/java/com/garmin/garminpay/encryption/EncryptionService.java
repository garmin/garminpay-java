/*
 * Copyright 2024 Garmin International, Inc.
 * Licensed under the Garmin Pay Software License Agreement; you
 * may not use this file except in compliance with the Garmin Pay Software License Agreement.
 */
package com.garmin.garminpay.encryption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garmin.garminpay.exception.GarminPayEncryptionException;
import com.garmin.garminpay.model.GarminPayCardData;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;

@Slf4j
public final class EncryptionService {
    private static final JWEAlgorithm ALGORITHM = JWEAlgorithm.A256GCMKW;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a shared secret or "key agreement" between the server public and client private keys.
     *
     * @param serverPublicKey  server public key represented as a String
     * @param clientPrivateKey client private key represented as a String
     * @return SecretKey object that represents a keyAgreement between the two keys
     */
    public SecretKey generateKeyAgreement(@NonNull String serverPublicKey, @NonNull String clientPrivateKey) {
        log.debug("Generating key agreement from provided keys");
        try {
            Key publicKey = getPublicKey(serverPublicKey);
            Key privateKey = getPrivateKey(clientPrivateKey);

            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", BouncyCastleProviderSingleton.getInstance());
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);

            return keyAgreement.generateSecret("AES");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.warn("Unable to generate key agreement");
            throw new GarminPayEncryptionException("Unable to generate a new key agreement");
        }
    }

    /**
     * Encrypts a CardData object for end to end payload encryption.
     *
     * @param garminPayCardData  The CardData for card registration
     * @param secretKey secret key for encrypting sensitive data
     * @param keyId Server keyId to be encrypted
     * @return string representing the JWE of cardData
     */
    public String encryptCardData(
        @NonNull GarminPayCardData garminPayCardData, @NonNull SecretKey secretKey, @NonNull String keyId
    ) {
        log.debug("Encrypting card data");
        try {
            AESEncrypter encryptor = new AESEncrypter(secretKey);

            String serializedCardData = serializeCardData(garminPayCardData);
            Payload payload = new Payload(serializedCardData);

            log.debug("Building jwe object");
            JWEObject jwe = new JWEObject(
                new JWEHeader.Builder(ALGORITHM, EncryptionMethod.A256GCM)
                    .contentType("application/jwe")
                    .keyID(keyId)
                    .build(),
                payload);

            jwe.encrypt(encryptor);
            return jwe.serialize();
        } catch (JOSEException e) {
            log.warn("Unable to encrypt card data");
            throw new GarminPayEncryptionException("Unable to encrypt card data with provided secret key");
        }
    }

    /**
     * Serializes a CardData object and returns it.
     *
     * @param garminPayCardData The CardData to serialize
     * @return String of serialized CardData object
     */
    private String serializeCardData(GarminPayCardData garminPayCardData) {
        try {
            // map and serialize CardData object
            log.debug("Serializing card data");
            return mapper.writeValueAsString(garminPayCardData);
        } catch (JsonProcessingException e) {
            log.warn("Unable to serialize card data");
            throw new GarminPayEncryptionException("Could not map or serialize card data");
        }
    }

    /**
     * Creates a Java Security key from a character array.
     *
     * @param publicKey public key represented by a character array
     * @return Java Security Key
     */
    private Key getPublicKey(String publicKey) {
        log.debug("Getting public key from provided key");
        try {
            KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProviderSingleton.getInstance());

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Hex.decodeHex(publicKey.toCharArray()));
            return kf.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
            log.warn("Unable to get public key from provided key");
            throw new GarminPayEncryptionException("Unable to decrypt public key");
        }
    }

    /**
     * Creates a Java Security key from a character array.
     *
     * @param privateKey private key represented by a character array
     * @return Java Security Key
     */
    private Key getPrivateKey(String privateKey) {
        log.debug("Getting public key from provided key");
        try {
            KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProviderSingleton.getInstance());

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Hex.decodeHex(privateKey.toCharArray()));
            return kf.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
            log.warn("Unable to get private key from provided key");
            throw new GarminPayEncryptionException("Unable to decrypt private key");
        }
    }
}
