package com.garminpay.proxy;

import com.garminpay.APIClient;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.request.CreateECCEncryptionKeyRequest;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.RootResponse;
import com.garminpay.util.JsonBodyHandler;
import com.garminpay.model.response.ECCEncryptionKeyResponse;
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
    private HttpResponse<RootResponse> httpRootResponseMock;
    private HttpResponse<OAuthTokenResponse> httpOAuthTokenMock;
    private HttpResponse<ECCEncryptionKeyResponse> httpECCEncryptionKeyMock;
    private HttpResponse<HealthResponse> httpHealthResponseMock;

    private static final String baseApiUrl = "https://api.qa.fitpay.ninja";
    private static final String authUrl = "https://auth.qa.fitpay.ninja/oauth/token";
    private static final String keyExchangeUrl = baseApiUrl + "/config/encryptionKeys";

    @BeforeEach
    void setUp() {
        apiClientMock = mock(APIClient.class);
        httpRootResponseMock = mock(HttpResponse.class);
        httpOAuthTokenMock = mock(HttpResponse.class);
        httpECCEncryptionKeyMock = mock(HttpResponse.class);
        httpHealthResponseMock = mock(HttpResponse.class);
        garminPayProxy = new GarminPayProxy();

        try {
            Field apiClientField = GarminPayProxy.class.getDeclaredField("apiClient");
            apiClientField.setAccessible(true);
            apiClientField.set(garminPayProxy, apiClientMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new GarminPaySDKException("Could not set mock APIClient", e);
        }
    }

    @Test
    void canGetRootEndpoint() {
        Map<String, RootResponse.HalLink> mockLinks = Map.of(
            "self", RootResponse.HalLink.builder().href("https://api.qa.fitpay.ninja/").build()
        );

        RootResponse mockRootResponse = RootResponse.builder()
            .links(mockLinks)
            .build();

        when(apiClientMock.get(eq(baseApiUrl + "/"), any(JsonBodyHandler.class))).thenReturn(httpRootResponseMock);
        when(httpRootResponseMock.statusCode()).thenReturn(200);
        when(httpRootResponseMock.body()).thenReturn(mockRootResponse);

        RootResponse response = garminPayProxy.getRootEndpoint();

        assertNotNull(response);
        assertNotNull(response.getLinks().get("self"));
        assertEquals("https://api.qa.fitpay.ninja/", response.getLinks().get("self").getHref());
        verify(apiClientMock, times(1)).get(eq(baseApiUrl + "/"), any(JsonBodyHandler.class));
    }


    @Test
    void canHandle404ResponseGetRootEndpoint() {
        RootResponse mockRootResponse = RootResponse.builder()
            .path("/")
            .status(404)
            .message("Not Found")
            .build();

        when(httpRootResponseMock.statusCode()).thenReturn(404);
        when(httpRootResponseMock.body()).thenReturn(mockRootResponse);
        when(apiClientMock.get(eq(baseApiUrl + "/"), any(JsonBodyHandler.class))).thenReturn(httpRootResponseMock);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getRootEndpoint();
        });

        assertEquals("/", exception.getPath());
        assertEquals(404, exception.getStatus());
        assertEquals("Not Found", exception.getMessage());
    }

    @Test
    void canHandle502ResponseGetRootEndpoint() {
        RootResponse mockRootResponse = RootResponse.builder()
            .path("/")
            .status(502)
            .message("Bad Gateway")
            .build();

        when(httpRootResponseMock.statusCode()).thenReturn(502);
        when(httpRootResponseMock.body()).thenReturn(mockRootResponse);
        when(apiClientMock.get(eq(baseApiUrl + "/"), any(JsonBodyHandler.class))).thenReturn(httpRootResponseMock);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getRootEndpoint();
        });

        assertEquals("/", exception.getPath());
        assertEquals(502, exception.getStatus());
        assertEquals("Bad Gateway", exception.getMessage());
    }

    @Test
    void canGetHealthStatus() {
        HealthResponse mockHealth = HealthResponse.builder()
            .healthStatus("OK")
            .build();

        when(apiClientMock.get(eq(baseApiUrl + "/health"), any(JsonBodyHandler.class))).thenReturn(httpHealthResponseMock);
        when(httpHealthResponseMock.statusCode()).thenReturn(200);
        when(httpHealthResponseMock.body()).thenReturn(mockHealth);

        HealthResponse health = garminPayProxy.getHealthStatus();

        assertNotNull(health, "Health response should not be null");
        assertEquals("OK", health.getHealthStatus());
        verify(apiClientMock, times(1)).get(eq(baseApiUrl + "/health"), any(JsonBodyHandler.class));
    }

    @Test
    void canHandle502ResponseHealthStatus() {
        HealthResponse mockHealthResponse = HealthResponse.builder()
            .path("/health")
            .status(502)
            .message("Bad Gateway")
            .build();

        when(httpHealthResponseMock.statusCode()).thenReturn(502);
        when(httpHealthResponseMock.body()).thenReturn(mockHealthResponse);
        when(apiClientMock.get(eq(baseApiUrl + "/health"), any(JsonBodyHandler.class))).thenReturn(httpHealthResponseMock);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals("/health", exception.getPath());
        assertEquals(502, exception.getStatus());
        assertEquals("Bad Gateway", exception.getMessage());
    }

    @Test
    void canGetOAuthAccessToken() {
        OAuthTokenResponse mockToken = OAuthTokenResponse.builder()
            .accessToken("mockAccessToken")
            .build();

        when(httpOAuthTokenMock.statusCode()).thenReturn(200);
        when(httpOAuthTokenMock.body()).thenReturn(mockToken);
        when(apiClientMock.post(eq(authUrl), eq(Map.of("grant_type", "client_credentials")), any(Map.class), any(JsonBodyHandler.class))).thenReturn(httpOAuthTokenMock);

        String accessToken = garminPayProxy.getOAuthAccessToken("clientId", "clientSecret");

        assertNotNull(accessToken, "Access token should not be null");
        assertEquals("mockAccessToken", accessToken, "Access token should be 'mockAccessToken'");
        verify(apiClientMock, times(1)).post(eq(authUrl), eq(Map.of("grant_type", "client_credentials")), any(Map.class), any(JsonBodyHandler.class));
    }

    @Test
    void canHandle401FromGenerateOAuthToken() {
        OAuthTokenResponse mockOAuthTokenResponse = OAuthTokenResponse.builder()
            .path("/oauth/token")
            .status(401)
            .message("Unauthorized")
            .build();

        when(httpOAuthTokenMock.statusCode()).thenReturn(401);
        when(httpOAuthTokenMock.body()).thenReturn(mockOAuthTokenResponse);
        when(apiClientMock.post(eq(authUrl), eq(Map.of("grant_type", "client_credentials")), any(Map.class), any(JsonBodyHandler.class))).thenReturn(httpOAuthTokenMock);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getOAuthAccessToken("clientId", "clientSecret");
        });

        assertEquals(401, exception.getStatus());
        assertEquals("/oauth/token", exception.getPath());
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void canHandleGenerateOAuthTokenException() {
        when(apiClientMock.post(eq(authUrl), eq(Map.of("grant_type", "client_credentials")), any(Map.class), any(JsonBodyHandler.class)))
            .thenThrow(new GarminPayApiException(
                null, 0, null, null, null, null, null,
                "Failed to get OAuth token", new Exception()));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getOAuthAccessToken("clientId", "clientSecret");
        });

        assertEquals("Failed to get OAuth token", exception.getMessage());
    }

    @Test
    void canExchangeKeys() {
        String mockOauthToken = "mockAccessToken";
        String mockServerKeyId = UUID.randomUUID().toString();
        ECCEncryptionKeyResponse mockKey = ECCEncryptionKeyResponse.builder()
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .keyId(mockServerKeyId)
            .active(true)
            .build();

        when(httpECCEncryptionKeyMock.statusCode()).thenReturn(201);
        when(httpECCEncryptionKeyMock.body()).thenReturn(mockKey);
        when(apiClientMock.post(eq(keyExchangeUrl), any(), any(), any(JsonBodyHandler.class))).thenReturn(httpECCEncryptionKeyMock);

        ECCEncryptionKeyResponse eccEncryptionKeyResponse = garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);

        assertNotNull(eccEncryptionKeyResponse);
        assertEquals(eccEncryptionKeyResponse, mockKey);
        verify(apiClientMock, times(1)).post(eq(keyExchangeUrl), any(), any(), any());
    }

    @Test
    void canHandleNullResponseToExchangeKeys() {
        String mockOauthToken = "mockAccessToken";
        ECCEncryptionKeyResponse mockKey = ECCEncryptionKeyResponse.builder()
            .path("/config/encryptionKeys")
            .status(400)
            .message("Bad Request")
            .build();

        when(httpECCEncryptionKeyMock.statusCode()).thenReturn(400);
        when(httpECCEncryptionKeyMock.body()).thenReturn(mockKey);
        when(apiClientMock.post(eq(keyExchangeUrl), any(CreateECCEncryptionKeyRequest.class), any(Map.class), any(JsonBodyHandler.class))).thenReturn(httpECCEncryptionKeyMock);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        });

        assertEquals(400, exception.getStatus());
        assertEquals("/config/encryptionKeys", exception.getPath());
        assertEquals("Bad Request", exception.getMessage());
    }
}
