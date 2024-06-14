package com.garminpay;

import com.garminpay.model.GarminPayCardData;
import lombok.Getter;

/**
 * SDK class for handling clientId and clientSecret.
 */
public class GarminPay {
    @Getter
    private static String clientId;
    @Getter
    private static String clientSecret;

    /**
     * Initializes the SDK with the given clientId and clientSecret.
     *
     * @param clientId     the client ID provided by the issuer
     * @param clientSecret the client secret provided by the issuer
     * @throws IllegalArgumentException if clientID or clientSecret is NULL
     */
    public static void initialize(String clientId, String clientSecret) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException(
                "clientId and clientSecret cannot be null or empty"
            );
        }

        GarminPay.clientId = clientId;
        GarminPay.clientSecret = clientSecret;
    }

    /**
     * Takes a card data object and registers it with the Garmin Pay platform.
     *
     * @param garminCardDataObject The card data object to register
     * @return Deeplink url to GCM app
     */
    public static String registerCard(GarminPayCardData garminCardDataObject) {
        GarminPayService garminPayService = new GarminPayService();
        return garminPayService.registerCard(garminCardDataObject, clientId, clientSecret);
    }
}
