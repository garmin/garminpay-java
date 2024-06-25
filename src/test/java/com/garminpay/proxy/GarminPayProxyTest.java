package com.garminpay.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.TestUtils;
import com.garminpay.client.RefreshableOauthClient;
import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.dto.APIResponseDTO;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.ExchangeKeysResponse;
import com.garminpay.model.response.RegisterCardResponse;
import com.garminpay.model.response.HealthResponse;
import com.garminpay.model.response.RootResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    private RefreshableOauthClient refreshableOauthClient;
    private GarminPayProxy garminPayProxy;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Header[] testingHeaders = {
        new BasicHeader("CF-RAY", "testing-cf-ray")
    };

    @BeforeEach
    void setUp() {
        refreshableOauthClient = mock(RefreshableOauthClient.class);
        garminPayProxy = new GarminPayProxy(refreshableOauthClient,"http://localhost");
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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        RootResponse rootResponse = garminPayProxy.getRootEndpoint();

        assertEquals(successResponse.getLinks(), rootResponse.getLinks());
        verify(refreshableOauthClient, times(1)).executeRequest(any());
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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        HealthResponse healthResponse = garminPayProxy.getHealthStatus();

        assertEquals("OK", healthResponse.getHealthStatus());
        verify(refreshableOauthClient, times(1)).executeRequest(any());
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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> garminPayProxy.getHealthStatus());

        assertEquals(HttpStatus.SC_BAD_GATEWAY, exception.getStatus());
    }

    @Test
    void testPostExchangeKeysSuccess() throws JsonProcessingException {
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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        ExchangeKeysResponse exchangeKeysResponse = garminPayProxy.exchangeKeys(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);

        assertEquals(exchangeKeysResponse.getKeyId(), successResponse.getKeyId());
        assertEquals(exchangeKeysResponse.getServerPublicKey(), successResponse.getServerPublicKey());
        verify(refreshableOauthClient, times(1)).executeRequest(any());
    }

    @Test
    void testPostExchangeKeysFailure() throws JsonProcessingException {
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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.exchangeKeys(TestUtils.TESTING_ENCODED_PUBLIC_ECC_KEY);
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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        RegisterCardResponse deepLinkResponse = garminPayProxy.registerCard("mockEncryptedCardData");

        assertEquals(testingDeepLinkUrl, deepLinkResponse.getDeepLinkUrl());
        verify(refreshableOauthClient, times(1)).executeRequest(any());
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

        when(refreshableOauthClient.executeRequest(any())).thenReturn(responseDTO);

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.registerCard("mockEncryptedCardData");
        });

        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
    }
}
