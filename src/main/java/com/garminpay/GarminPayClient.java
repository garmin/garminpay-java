package com.garminpay;

import com.garminpay.client.APIClient;
import com.garminpay.client.Client;
import com.garminpay.client.RefreshableOauthClient;
import com.garminpay.model.GarminPayCardData;
import com.garminpay.proxy.GarminPayProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;

import java.nio.charset.StandardCharsets;

/**
 * This class serves as the main entrypoint when interacting with the GarminPay platform.
 */
@Slf4j
public class GarminPayClient {
    private static final String BASE_URL = "https://api.qa.fitpay.ninja";
    private static final String AUTH_URL = "https://auth.qa.fitpay.ninja/oauth/token";
    private final GarminPayService garminPayService;

    /**
     * Creates a GarminPayClient with the given clientId and clientSecret.
     *
     * @param clientId     the client ID provided by the issuer
     * @param clientSecret the client secret provided by the issuer
     */
    public GarminPayClient(String clientId, String clientSecret) {
        this(clientId, clientSecret, null);
    }

    /**
     * Creates a GarminPayClient with the given clientId, clientSecret and httpClient.
     * httpClient must use an HTTP/S configured proxy, not TCP, etc.
     *
     * @param clientId     the client ID provided by the issuer
     * @param clientSecret the client secret provided by the issuer
     * @param httpClient   the httpClient to use for requests
     * @throws IllegalArgumentException if clientID or clientSecret is NULL
     */
    public GarminPayClient(String clientId, String clientSecret, HttpClient httpClient) {
        byte[] credentials = validateAndBuildCredentials(clientId, clientSecret);

        log.debug("Creating clients, proxy and service classes");
        Client baseClient = new APIClient(httpClient);
        Client refreshableOauthClient = new RefreshableOauthClient(baseClient, credentials, AUTH_URL);

        GarminPayProxy garminPayProxy = new GarminPayProxy(refreshableOauthClient, BASE_URL);

        garminPayService = new GarminPayService(garminPayProxy);
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

    private byte[] validateAndBuildCredentials(String clientId, String clientSecret) {
        log.debug("Validating client credentials");

        if (clientId == null || clientId.trim().isEmpty() || clientSecret == null || clientSecret.trim().isEmpty()) {
            log.warn("Provided client credentials were invalid");
            throw new IllegalArgumentException(
                "ClientId and ClientSecret cannot be null or empty"
            );
        }

        return (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8);
    }
}
