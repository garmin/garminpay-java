package com.garminpay;

import com.garminpay.client.APIClient;
import com.garminpay.client.Client;
import com.garminpay.client.RefreshableOauthClient;
import com.garminpay.exception.GarminPayCredentialsException;
import com.garminpay.model.GarminPayCardData;
import com.garminpay.proxy.GarminPayProxy;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * SDK class for handling clientId and clientSecret.
 */
@Slf4j
public class GarminPay {
    private static final String BASE_URL = "https://api.qa.fitpay.ninja";
    private static final String AUTH_URL = "https://auth.qa.fitpay.ninja/oauth/token";
    private final GarminPayService garminPayService;

    private GarminPay(String clientId, String clientSecret) {
        log.debug("Creating clients, proxy and service classes");

        byte[] credentials = (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8);
        Client baseClient = new APIClient();
        Client refreshableOauthClient = new RefreshableOauthClient(baseClient, credentials, AUTH_URL);

        GarminPayProxy garminPayProxy = new GarminPayProxy(refreshableOauthClient, BASE_URL);

        garminPayService = new GarminPayService(garminPayProxy);
    }

    /**
     * Initializes the SDK with the given clientId and clientSecret.
     *
     * @param clientId     the client ID provided by the issuer
     * @param clientSecret the client secret provided by the issuer
     * @return GarminPay object to be used to make method calls
     * @throws IllegalArgumentException if clientID or clientSecret is NULL
     */
    public static GarminPay initialize(String clientId, String clientSecret) {
        log.debug("Validating client credentials");

        if (clientId == null || clientId.trim().isEmpty() || clientSecret == null || clientSecret.trim().isEmpty()) {
            log.warn("Provided client credentials were invalid");
            throw new GarminPayCredentialsException(
                "ClientId and ClientSecret cannot be null or empty"
            );
        }

        return new GarminPay(clientId, clientSecret);
    }

    /**
     * Takes a card data object and registers it with the Garmin Pay platform.
     *
     * @param garminCardDataObject The card data object to register
     * @return Deeplink url to GCM app
     */
    public String registerCard(GarminPayCardData garminCardDataObject) {
        log.debug("Calling register card service");
        return garminPayService.registerCard(garminCardDataObject);
    }
}
