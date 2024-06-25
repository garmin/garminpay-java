package com.garminpay.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.exception.GarminPayCredentialsException;
import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.dto.APIResponseDTO;
import com.garminpay.model.request.OAuthTokenRequest;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;

import java.util.Base64;

public class RefreshableOauthClient extends APIClient {
    private final Client wrappedClient;
    private final byte[] credentials;
    private final String authUrl;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private String authToken;

    /**
     * Constructs a RefreshableOauthClient decorator for making HTTP calls with oauth refresh logic.
     *
     * @param client      the base client to apply the decorator too
     * @param credentials user credentials to use when authenticating
     * @param authUrl     URL to use when authenticating
     */
    public RefreshableOauthClient(Client client, byte[] credentials, String authUrl) {
        this.wrappedClient = client;
        this.credentials = credentials;
        this.authUrl = authUrl;

        // Immediately try to get an Oauth token
        // Will throw a GarminPayCredentialsException on failure
        this.refreshToken();
    }

    @Override
    public APIResponseDTO executeRequest(ClassicHttpRequest request) {
        // Add new header containing auth token
        Header authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.authToken, true);
        request.addHeader(authHeader);

        APIResponseDTO response = wrappedClient.executeRequest(request);

        if (response.getStatus() == 401) { // If 401, execute retry flow
            this.refreshToken();

            request.setHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.authToken, true));

            return wrappedClient.executeRequest(request);
        }

        return response;
    }

    private void refreshToken() {
        ClassicHttpRequest request = this.buildOAuthRequest();
        APIResponseDTO responseDTO = this.wrappedClient.executeRequest(request);

        // If response status is not 2xx client credentials may be invalid
        // TODO: This does not accurately show an issue with the credentials and the GP service may just be down
        // TODO: See PLAT-14381 on how we can better implement this
        if (responseDTO.getStatus() < 200 || responseDTO.getStatus() > 299) {
            try {
                ErrorResponse errorResponse = objectMapper.readValue(responseDTO.getContent(), ErrorResponse.class);

                throw new GarminPayCredentialsException(
                    "Failed to generate a new OAuth token, client credentials may be invalid.",
                    errorResponse,
                    responseDTO.findCFRay().orElse(null)
                );
            } catch (JsonProcessingException e) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(responseDTO.getStatus())
                    .path(responseDTO.getPath())
                    .build();

                throw new GarminPayCredentialsException(
                    "Failed to generate a new OAuth token, client credentials may be invalid.",
                    errorResponse,
                    responseDTO.findCFRay().orElse(null)
                );
            }
        }

        // Read in the response and set the new token
        try {
            OAuthTokenResponse oAuthTokenResponse = objectMapper.readValue(responseDTO.getContent(), OAuthTokenResponse.class);
            if (oAuthTokenResponse.getAccessToken() == null) {
                throw new GarminPaySDKException("Found a response but the token was either null or did not refresh");
            }

            this.authToken = oAuthTokenResponse.getAccessToken();
        } catch (JsonProcessingException e) {
            throw new GarminPaySDKException("Failed to read OAuth token response when generating a new OAuth token");
        }
    }

    private ClassicHttpRequest buildOAuthRequest() {
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials);

        OAuthTokenRequest requestModel = OAuthTokenRequest.builder()
            .grantType("client_credentials")
            .build();

        String serializedRequestBody;
        try {
            serializedRequestBody = objectMapper.writeValueAsString(requestModel);
        } catch (JsonProcessingException e) {
            throw new GarminPaySDKException("Failed to serialize request when generating a new OAuth token");
        }

        Header contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        Header authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials, true);

        return ClassicRequestBuilder.post(authUrl)
            .setEntity(new StringEntity(serializedRequestBody, ContentType.APPLICATION_JSON))
            .addHeader(contentTypeHeader)
            .addHeader(authHeader)
            .build();
    }
}
