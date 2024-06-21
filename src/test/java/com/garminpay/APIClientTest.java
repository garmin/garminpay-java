package com.garminpay;

import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.dto.APIResponseDTO;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class APIClientTest {
    private APIClient apiClient;
    private CloseableHttpClient httpClientMock;
    private static final String testingURL = "http://testing";

    @BeforeEach
    void setUp() {
        httpClientMock = mock(CloseableHttpClient.class);

        apiClient = APIClient.getInstance();
        try {
            Field httpClientField = APIClient.class.getDeclaredField("httpClient");
            httpClientField.setAccessible(true);
            httpClientField.set(apiClient, httpClientMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void canGetInstance() {
        APIClient instance1 = APIClient.getInstance();
        APIClient instance2 = APIClient.getInstance();

        assertNotNull(instance1, "APIClient instance1 should not be null");
        assertNotNull(instance2, "APIClient instance2 should not be null");
        assertEquals(instance1, instance2, "Both instances should be the same");
    }

    @Test
    void canGetEndpoint() throws IOException {
        APIResponseDTO responseSuccess = APIResponseDTO.builder()
            .status(200)
            .build();

        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class))).thenReturn(responseSuccess);

        ClassicHttpRequest request = ClassicRequestBuilder.get(testingURL).build();

        APIResponseDTO response = apiClient.send(request);

        assertEquals(200, response.getStatus());
        verify(httpClientMock, times(1)).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));
    }

    @Test
    void canPostEndpoint() throws IOException {
        APIResponseDTO responseSuccess = APIResponseDTO.builder()
            .status(200)
            .build();

        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class))).thenReturn(responseSuccess);

        ClassicHttpRequest request = ClassicRequestBuilder.post(testingURL).build();

        APIResponseDTO response = apiClient.send(request);

        assertEquals(200, response.getStatus());
        verify(httpClientMock, times(1)).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));
    }

    @Test
    void canHandleIOException() throws IOException {
        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
            .thenThrow(new IOException("Simulated Interrupt Exception"));

        ClassicHttpRequest request = ClassicRequestBuilder.get(testingURL).build();

        assertThrows(GarminPayApiException.class, () -> apiClient.send(request));
    }
}
