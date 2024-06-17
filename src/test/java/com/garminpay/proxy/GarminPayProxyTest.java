package com.garminpay.proxy;

import com.garminpay.APIClient;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.response.PaymentCardDeepLinkResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.RootResponse;
import com.garminpay.model.response.ECCEncryptionKeyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GarminPayProxyTest {
    private APIClient apiClientMock;
    private GarminPayProxy garminPayProxy;

    @BeforeEach
    void setUp() {
        apiClientMock = mock(APIClient.class);

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
    void testGetRootEndpointSuccess() {
        Map<String, RootResponse.HalLink> mockLinks = Collections.singletonMap(
            "self", RootResponse.HalLink.builder().href("https://testingHref").build()
        );

        RootResponse successResponse = RootResponse.builder()
            .links(mockLinks)
            .status(200)
            .build();

        when(apiClientMock.get(any(), eq(RootResponse.class))).thenReturn(successResponse);

        RootResponse rootResponse = garminPayProxy.getRootEndpoint();

        assertEquals(successResponse, rootResponse);
        verify(apiClientMock, times(1)).get(any(), eq(RootResponse.class));
    }

    @Test
    void testGetRootEndpointFailure() {
        RootResponse failureResponse = RootResponse.builder()
            .path("/")
            .status(404)
            .message("Not Found")
            .build();

        when(apiClientMock.get(any(), eq(RootResponse.class))).thenReturn(failureResponse);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getRootEndpoint());

        assertEquals(failureResponse.getStatus(), exception.getStatus());
    }

    @Test
    void testGetRootEndpointBadGateway() {
        RootResponse failureResponse = RootResponse.builder()
            .path("/")
            .status(502)
            .message("Bad Gateway")
            .build();

        when(apiClientMock.get(any(), any())).thenReturn(failureResponse);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getRootEndpoint();
        });

        assertEquals("/", exception.getPath());
        assertEquals(502, exception.getStatus());
        assertEquals("Bad Gateway", exception.getMessage());
    }

    @Test
    void testGetHealthStatusSuccess() {
        HealthResponse successResponse = HealthResponse.builder()
            .healthStatus("OK")
            .status(200)
            .build();

        when(apiClientMock.get(any(), eq(HealthResponse.class))).thenReturn(successResponse);

        HealthResponse healthResponse = garminPayProxy.getHealthStatus();

        assertEquals("OK", healthResponse.getHealthStatus());
        verify(apiClientMock, times(1)).get(any(), eq(HealthResponse.class));
    }

    @Test
    void testGetHealthStatusFailure() {
        HealthResponse failureResponse = HealthResponse.builder()
            .path("/")
            .status(404)
            .message("Not Found")
            .build();


        when(apiClientMock.get(any(), eq(HealthResponse.class))).thenReturn(failureResponse);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals(failureResponse.getStatus(), exception.getStatus());
    }

    @Test
    void testGetHealthStatusBadGateway() {
        HealthResponse failureResponse = HealthResponse.builder()
            .path("/health")
            .status(502)
            .message("Bad Gateway")
            .build();

        when(apiClientMock.get(any(), eq(HealthResponse.class))).thenReturn(failureResponse);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals("/health", exception.getPath());
        assertEquals(502, exception.getStatus());
        assertEquals("Bad Gateway", exception.getMessage());
    }

    @Test
    void testGetOAuthAccessTokenSuccess() {
        OAuthTokenResponse successResponse = OAuthTokenResponse.builder()
            .accessToken("mockAccessToken")
            .status(200)
            .build();

        when(apiClientMock.post(any(), eq(OAuthTokenResponse.class), any(), any())).thenReturn(successResponse);

        String accessToken = garminPayProxy.getOAuthAccessToken("clientId", "clientSecret");

        assertEquals("mockAccessToken", accessToken, "Access token should be 'mockAccessToken'");
        verify(apiClientMock, times(1)).post(any(), eq(OAuthTokenResponse.class), any(), any());
    }

    @Test
    void testGetOAuthAccessTokenFaiure() {
        OAuthTokenResponse failureResponse = OAuthTokenResponse.builder()
            .path("/oauth/token")
            .status(401)
            .message("Unauthorized")
            .build();

        when(apiClientMock.post(any(), eq(OAuthTokenResponse.class), any(), any())).thenReturn(failureResponse);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getOAuthAccessToken("clientId", "clientSecret");
        });

        assertEquals(401, exception.getStatus());
        assertEquals("/oauth/token", exception.getPath());
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void testPostExchangeKeysSuccess() {
        String mockOauthToken = "mockAccessToken";
        String mockServerKeyId = UUID.randomUUID().toString();
        ECCEncryptionKeyResponse successResponse = ECCEncryptionKeyResponse.builder()
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .keyId(mockServerKeyId)
            .active(true)
            .status(200)
            .build();

        when(apiClientMock.post(any(), eq(ECCEncryptionKeyResponse.class), any(), any())).thenReturn(successResponse);

        ECCEncryptionKeyResponse eccEncryptionKeyResponse = garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);

        assertEquals(eccEncryptionKeyResponse, successResponse);
        verify(apiClientMock, times(1)).post(any(), eq(ECCEncryptionKeyResponse.class), any(), any());
    }

    @Test
    void testPostExchangeKeysFailure() {
        String mockOauthToken = "mockAccessToken";
        ECCEncryptionKeyResponse failureResponse = ECCEncryptionKeyResponse.builder()
            .path("/config/encryptionKeys")
            .status(400)
            .message("Bad Request")
            .build();

        when(apiClientMock.post(any(), eq(ECCEncryptionKeyResponse.class), any(), any())).thenReturn(failureResponse);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        });

        assertEquals(400, exception.getStatus());
        assertEquals("/config/encryptionKeys", exception.getPath());
        assertEquals("Bad Request", exception.getMessage());
    }

    @Test
    void testPostRegisterCardSuccess() {
        String testingDeepLinkUrl = "testingDeepLinkUrl";
        PaymentCardDeepLinkResponse successResponse = PaymentCardDeepLinkResponse.builder()
            .deepLinkUrl(testingDeepLinkUrl)
            .status(200)
            .build();

        when(apiClientMock.post(any(), eq(PaymentCardDeepLinkResponse.class), any(), any())).thenReturn(successResponse);

        PaymentCardDeepLinkResponse deepLinkResponse = garminPayProxy.registerCard("mockOAuthToken", "mockEncryptedCardData");

        assertEquals(testingDeepLinkUrl, deepLinkResponse.getDeepLinkUrl());
        verify(apiClientMock, times(1)).post(any(), eq(PaymentCardDeepLinkResponse.class), any(), any());
    }

    @Test
    void testPostRegisterCardFailure() {
        PaymentCardDeepLinkResponse failureResponse = PaymentCardDeepLinkResponse.builder()
            .path("/config/encryptionKeys")
            .status(400)
            .message("Bad Request")
            .build();

        when(apiClientMock.post(any(), eq(PaymentCardDeepLinkResponse.class), any(), any())).thenReturn(failureResponse);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.registerCard("mockOAuthToken", "mockEncryptedCardData");
        });

        assertEquals(400, exception.getStatus());
        assertEquals("/config/encryptionKeys", exception.getPath());
        assertEquals("Bad Request", exception.getMessage());
    }
}
