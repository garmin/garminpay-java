package com.garminpay;

import com.garminpay.model.GarminPayCardData;
import com.garminpay.proxy.GarminPayProxy;
import lombok.Getter;

/**
 * SDK class for handling clientId and clientSecret.
 */
public class GarminPay {
    @Getter
    private static String clientId;
    @Getter
    private static String clientSecret;
    private static final String BASE_URL = "https://api.qa.fitpay.ninja";
    private static final String AUTH_URL = "https://auth.qa.fitpay.ninja";

    /**
     * Initializes the SDK with the given clientId and clientSecret.
     *
     * @param clientId     the client ID provided by the issuer
     * @param clientSecret the client secret provided by the issuer
     * @throws IllegalArgumentException if clientID or clientSecret is NULL
     */
    public static void initialize(String clientId, String clientSecret) {
        if (clientId == null || clientId.trim().isEmpty() || clientSecret == null || clientSecret.trim().isEmpty()) {
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
        GarminPayProxy garminPayProxy = new GarminPayProxy(BASE_URL, AUTH_URL);
        GarminPayService garminPayService = new GarminPayService(garminPayProxy);
        return garminPayService.registerCard(garminCardDataObject, clientId, clientSecret);
    }
}
