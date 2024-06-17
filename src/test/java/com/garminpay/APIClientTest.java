package com.garminpay;

import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.response.ErrorResponse;
import com.garminpay.model.response.RootResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
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
        RootResponse mockedResponse = RootResponse.builder()
            .status(200)
            .build();

        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class))).thenReturn(mockedResponse);

        RootResponse response = apiClient.get(testingURL, RootResponse.class);

        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatus(), "Response status code should be 200");
        verify(httpClientMock, times(1)).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));
    }

    @Test
    void canHandleIOException() throws IOException {
        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
            .thenThrow(new IOException("Simulated Interrupt Exception"));

        assertThrows(GarminPaySDKException.class, () -> apiClient.get(testingURL, ErrorResponse.class));
    }

    @Test
    void canPostEndpoint() throws IOException {
        RootResponse mockedResponse = RootResponse.builder()
            .status(200)
            .build();

        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class))).thenReturn(mockedResponse);

        RootResponse response = apiClient.post(testingURL, RootResponse.class, null);

        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatus(), "Response status code should be 200");
        verify(httpClientMock, times(1)).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));
    }

    @Test
    void canHandlePostIOException() throws IOException {
        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
            .thenThrow(new IOException("Simulated Interrupt Exception"));

        assertThrows(GarminPaySDKException.class, () -> apiClient.post(testingURL, RootResponse.class, null));
    }
}
