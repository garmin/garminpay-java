package com.garminpay.proxy;

import com.garminpay.APIClient;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.exception.GarminPayEncryptionException;
import com.garminpay.model.KeyExchangeDTO;
import com.garminpay.model.request.CreateECCEncryptionKeyRequest;
import com.garminpay.model.response.OAuthToken;
import com.garminpay.model.HealthResponse;
import com.garminpay.util.JsonBodyHandler;
import com.garminpay.model.response.ECCEncryptionKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GarminPayProxyTest {
    private GarminPayProxy garminPayProxy;
    private APIClient apiClientMock;
    private HttpResponse<String> httpResponseMock;
    private HttpResponse<OAuthToken> httpOAuthTokenMock;
    private HttpResponse<ECCEncryptionKey> httpECCEncryptionKeyMock;
    private HttpResponse<HealthResponse> httpHealthResponseMock;

    private static final String baseApiUrl = "https://api.qa.fitpay.ninja";
    private static final String authUrl = "https://auth.qa.fitpay.ninja/oauth/token";
    private static final String keyExchangeUrl = baseApiUrl + "/config/encryptionKeys";

    @BeforeEach
    void setUp() {
        apiClientMock = mock(APIClient.class);
        httpResponseMock = mock(HttpResponse.class);
        httpOAuthTokenMock = mock(HttpResponse.class);
        httpECCEncryptionKeyMock = mock(HttpResponse.class);
        httpHealthResponseMock = mock(HttpResponse.class);
        garminPayProxy = new GarminPayProxy();

        try {
            Field apiClientField = GarminPayProxy.class.getDeclaredField("apiClient");
            apiClientField.setAccessible(true);
            apiClientField.set(garminPayProxy, apiClientMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new GarminPayApiException("Could not set mock APIClient", e);
        }
    }

    @Test
    void canGetRootEndpoint() {
        when(apiClientMock.get(eq(baseApiUrl + "/"), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponseMock);
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("Success");

        HttpResponse<String> response = garminPayProxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());

        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.statusCode(), "Response status code should be 200");
        assertEquals("Success", response.body(), "Response body should be 'Success'");
        verify(apiClientMock, times(1)).get(eq(baseApiUrl + "/"), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void canHandleGarminPayApiException() {
        when(apiClientMock.get(eq(baseApiUrl + "/"), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new GarminPayApiException("Error", new Exception()));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());
        });

        assertEquals("Error", exception.getMessage());
    }

    @Test
    void canGetHealthStatus() {
        HealthResponse mockHealthResponse = HealthResponse.builder()
            .status("ok")
            .build();

        when(apiClientMock.get(eq(baseApiUrl + "/health"), any(JsonBodyHandler.class))).thenReturn(httpHealthResponseMock);
        when(httpHealthResponseMock.statusCode()).thenReturn(200);
        when(httpHealthResponseMock.body()).thenReturn(mockHealthResponse);

        HealthResponse healthResponse = garminPayProxy.getHealthStatus();

        assertNotNull(healthResponse, "Health response should not be null");
        assertEquals("ok", healthResponse.getStatus(), "Health status should be 'ok'");
        verify(apiClientMock, times(1)).get(eq(baseApiUrl + "/health"), any(JsonBodyHandler.class));
    }

    @Test
    void canHandle502ResponseHealthStatus() {
        when(httpHealthResponseMock.statusCode()).thenReturn(502);
        when(apiClientMock.get(eq(baseApiUrl + "/health"), any(JsonBodyHandler.class))).thenReturn(httpHealthResponseMock);
        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals("Failed to get URL: " + baseApiUrl + "/health, status code: 502", exception.getMessage());
    }

    @Test
    void canGenerateOAuthAccessToken() {
        OAuthToken mockToken = OAuthToken.builder()
            .accessToken("mockAccessToken")
            .build();

        when(httpOAuthTokenMock.statusCode()).thenReturn(200);
        when(httpOAuthTokenMock.body()).thenReturn(mockToken);
        when(apiClientMock.post(eq(authUrl), eq(Map.of("grant_type", "client_credentials")), any(Map.class), any(JsonBodyHandler.class))).thenReturn(httpOAuthTokenMock);

        String accessToken = garminPayProxy.generateOAuthAccessToken("clientId", "clientSecret");

        assertNotNull(accessToken, "Access token should not be null");
        assertEquals("mockAccessToken", accessToken, "Access token should be 'mockAccessToken'");
        verify(apiClientMock, times(1)).post(eq(authUrl), eq(Map.of("grant_type", "client_credentials")), any(Map.class), any(JsonBodyHandler.class));
    }

    @Test
    void canHandle401FromGenerateOAuthToken() {
        when(httpOAuthTokenMock.statusCode()).thenReturn(401);
        when(apiClientMock.post(eq(authUrl), eq(Map.of("grant_type", "client_credentials")), any(Map.class), any(JsonBodyHandler.class))).thenReturn(httpOAuthTokenMock);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.generateOAuthAccessToken("clientId", "clientSecret");
        });

        assertEquals("Failed to get OAuth token, status code: 401", exception.getMessage());
    }

    @Test
    void canHandleGenerateOAuthTokenException() {
        when(apiClientMock.post(eq(authUrl), eq(Map.of("grant_type", "client_credentials")), any(Map.class), any(JsonBodyHandler.class)))
            .thenThrow(new GarminPayApiException("Failed to get OAuth token", new Exception()));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.generateOAuthAccessToken("clientId", "clientSecret");
        });

        assertEquals("Failed to get OAuth token", exception.getMessage());
    }

    @Test
    void canExchangeKeys() {
        String mockOauthToken = "mockAccessToken";
        String mockServerKeyId = UUID.randomUUID().toString();
        ECCEncryptionKey mockKey = ECCEncryptionKey.builder()
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .keyId(mockServerKeyId)
            .active(true)
            .build();

        when(httpECCEncryptionKeyMock.statusCode()).thenReturn(201);
        when(httpECCEncryptionKeyMock.body()).thenReturn(mockKey);
        when(apiClientMock.post(eq(keyExchangeUrl), any(), any(), any(JsonBodyHandler.class))).thenReturn(httpECCEncryptionKeyMock);

        ECCEncryptionKey eccEncryptionKey = garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);

        assertNotNull(eccEncryptionKey);
        assertEquals(eccEncryptionKey, mockKey);
        verify(apiClientMock, times(1)).post(eq(keyExchangeUrl), any(), any(), any());
    }

    @Test
    void canHandleNullResponseToExchangeKeys() {
        String mockOauthToken = "mockAccessToken";
        ECCEncryptionKey mockKey = ECCEncryptionKey.builder().build();

        when(httpECCEncryptionKeyMock.statusCode()).thenReturn(400);
        when(httpECCEncryptionKeyMock.body()).thenReturn(mockKey);
        when(apiClientMock.post(eq(keyExchangeUrl), eq(CreateECCEncryptionKeyRequest.class), any(), any(JsonBodyHandler.class))).thenReturn(httpECCEncryptionKeyMock);

        assertThrows(GarminPayEncryptionException.class, () -> {
            garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        });
    }
}
