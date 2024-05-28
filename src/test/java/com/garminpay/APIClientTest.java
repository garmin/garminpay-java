package com.garminpay;

import com.garminpay.exception.GarminPayApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class APIClientTest {
    private APIClient apiClient;
    private HttpClient httpClientMock;
    private HttpResponse<String> httpResponseMock;

    @BeforeEach
    void setUp() {
        httpClientMock = mock(HttpClient.class);
        httpResponseMock = mock(HttpResponse.class);

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
    void canGetEndpoint() throws IOException, InterruptedException {
        // Mock HttpResponse behavior
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("Success");
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpResponse<String> response = apiClient.get("/", HttpResponse.BodyHandlers.ofString());

        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.statusCode(), "Response status code should be 200");
        assertEquals("Success", response.body(), "Response body should be 'Success'");
        verify(httpClientMock, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void canHandleIOException() throws IOException, InterruptedException {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new IOException("IO Error"));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            apiClient.get("/", HttpResponse.BodyHandlers.ofString());
        });

        assertEquals("Failed to get endpoint: /", exception.getMessage());
    }

    @Test
    void canHandleInterruptedException() throws IOException, InterruptedException {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new InterruptedException("Interrupted Error"));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            apiClient.get("/", HttpResponse.BodyHandlers.ofString());
        });

        assertEquals("Failed to get endpoint: /", exception.getMessage());
    }
}
