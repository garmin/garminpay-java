package com.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.APIClient;
import com.garminpay.TestUtils;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.dto.APIResponseDTO;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.model.response.OAuthTokenResponse;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.RootResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GarminPayProxyTest {
    private APIClient apiClientMock;
    private GarminPayProxy garminPayProxy;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Header[] testingHeaders = {
        new BasicHeader("CF-RAY", "testing-cf-ray")
    };

    @BeforeEach
    void setUp() {
        apiClientMock = mock(APIClient.class);

        garminPayProxy = new GarminPayProxy("http//localhost", "http//localhost");

        try {
            Field apiClientField = GarminPayProxy.class.getDeclaredField("apiClient");
            apiClientField.setAccessible(true);
            apiClientField.set(garminPayProxy, apiClientMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new GarminPaySDKException("Could not set mock APIClient", e);
        }
    }

    @Test
    void testGetRootEndpointSuccess() throws JsonProcessingException {
        Map<String, RootResponse.HalLink> mockLinks = Collections.singletonMap(
            "self", RootResponse.HalLink.builder().href("https://testingHref").build()
        );

        RootResponse successResponse = RootResponse.builder()
            .links(mockLinks)
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        RootResponse rootResponse = garminPayProxy.getRootEndpoint();

        assertEquals(successResponse.getLinks(), rootResponse.getLinks());
        verify(apiClientMock, times(1)).send(any());
    }

    @Test
    void testGetRootEndpointFailure() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/")
            .status(HttpStatus.SC_NOT_FOUND)
            .message("Not Found")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_NOT_FOUND)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getRootEndpoint());

        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatus());
    }

    @Test
    void testGetRootEndpointBadGateway() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/")
            .status(HttpStatus.SC_BAD_GATEWAY)
            .message("Bad Gateway")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_BAD_GATEWAY)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getRootEndpoint();
        });

        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
    }

    @Test
    void testGetHealthStatusSuccess() throws JsonProcessingException {
        HealthResponse successResponse = HealthResponse.builder()
            .healthStatus("OK")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        HealthResponse healthResponse = garminPayProxy.getHealthStatus();

        assertEquals("OK", healthResponse.getHealthStatus());
        verify(apiClientMock, times(1)).send(any());
    }

    @Test
    void testGetHealthStatusFailure() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/")
            .status(HttpStatus.SC_NOT_FOUND)
            .message("Not Found")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_NOT_FOUND)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatus());
    }

    @Test
    void testGetHealthStatusBadGateway() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/health")
            .status(HttpStatus.SC_BAD_GATEWAY)
            .message("Bad Gateway")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_BAD_GATEWAY)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
    }

    @Test
    void testGetOAuthAccessTokenSuccess() throws JsonProcessingException {
        OAuthTokenResponse successResponse = OAuthTokenResponse.builder()
            .accessToken("mockAccessToken")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        OAuthTokenResponse response = garminPayProxy.getOAuthAccessToken("clientId", "clientSecret");

        assertEquals("mockAccessToken", response.getAccessToken());
        verify(apiClientMock, times(1)).send(any());
    }

    @Test
    void testGetOAuthAccessTokenFailure() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/oauth/token")
            .status(HttpStatus.SC_UNAUTHORIZED)
            .message("Unauthorized")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_UNAUTHORIZED)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getOAuthAccessToken("clientId", "clientSecret");
        });

        assertEquals(HttpStatus.SC_UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void testPostExchangeKeysSuccess() throws JsonProcessingException {
        String mockOauthToken = "mockAccessToken";
        String mockServerKeyId = UUID.randomUUID().toString();
        ExchangeKeysResponse successResponse = ExchangeKeysResponse.builder()
            .serverPublicKey(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY)
            .keyId(mockServerKeyId)
            .active(true)
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        ExchangeKeysResponse exchangeKeysResponse = garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);

        assertEquals(exchangeKeysResponse.getKeyId(), successResponse.getKeyId());
        assertEquals(exchangeKeysResponse.getServerPublicKey(), successResponse.getServerPublicKey());
        verify(apiClientMock, times(1)).send(any());
    }

    @Test
    void testPostExchangeKeysFailure() throws JsonProcessingException {
        String mockOauthToken = "mockAccessToken";
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/config/encryptionKeys")
            .status(HttpStatus.SC_BAD_REQUEST)
            .message("Bad Request")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_BAD_REQUEST)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .path("/config/encryptionKeys")
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.exchangeKeys(mockOauthToken, TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
        });

        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testPostRegisterCardSuccess() throws JsonProcessingException {
        String testingDeepLinkUrl = "testingDeepLinkUrl";
        RegisterCardResponse successResponse = RegisterCardResponse.builder()
            .deepLinkUrl(testingDeepLinkUrl)
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .content(objectMapper.writeValueAsString(successResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        RegisterCardResponse deepLinkResponse = garminPayProxy.registerCard("mockOAuthToken", "mockEncryptedCardData");

        assertEquals(testingDeepLinkUrl, deepLinkResponse.getDeepLinkUrl());
        verify(apiClientMock, times(1)).send(any());
    }

    @Test
    void testPostRegisterCardFailure() throws JsonProcessingException {
        ErrorResponse failureResponse = ErrorResponse.builder()
            .path("/config/encryptionKeys")
            .status(HttpStatus.SC_BAD_REQUEST)
            .message("Bad Request")
            .build();

        APIResponseDTO responseDTO = APIResponseDTO.builder()
            .status(HttpStatus.SC_BAD_REQUEST)
            .content(objectMapper.writeValueAsString(failureResponse))
            .headers(testingHeaders)
            .build();

        when(apiClientMock.send(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.registerCard("mockOAuthToken", "mockEncryptedCardData");
        });

        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
    }
}
