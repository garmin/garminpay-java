package com.garminpay.client;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class APIClientTest {
    private APIClient client;
    private CloseableHttpClient httpClientMock;
    private static final String testingURL = "http://testing";

    @BeforeEach
    void setUp() {
        httpClientMock = mock(CloseableHttpClient.class);

        client = new APIClient();
        try {
            Field httpClientField = APIClient.class.getDeclaredField("httpClient");
            httpClientField.setAccessible(true);
            httpClientField.set(client, httpClientMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void canGetEndpoint() throws IOException {
        APIResponseDTO responseSuccess = APIResponseDTO.builder()
            .status(200)
            .build();

        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class))).thenReturn(responseSuccess);

        ClassicHttpRequest request = ClassicRequestBuilder.get(testingURL).build();

        APIResponseDTO response = client.executeRequest(request);

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

        APIResponseDTO response = client.executeRequest(request);

        assertEquals(200, response.getStatus());
        verify(httpClientMock, times(1)).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));
    }

    @Test
    void canHandleIOException() throws IOException {
        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
            .thenThrow(new IOException("Simulated Interrupt Exception"));

        ClassicHttpRequest request = ClassicRequestBuilder.get(testingURL).build();

        assertThrows(GarminPayApiException.class, () -> client.executeRequest(request));
    }
}
