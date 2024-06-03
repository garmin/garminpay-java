package com.garminpay;

import com.garminpay.exception.GarminPayApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class APIClientTest {
    private APIClient apiClient;
    private HttpClient httpClientMock;
    private HttpResponse<String> httpResponseMock;

    private static final String baseApiUrl = "https://api.qa.fitpay.ninja/";
    private static final String authUrl = "https://auth.qa.fitpay.ninja/oauth/token";

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
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("Success");
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        HttpResponse<String> response = apiClient.get(baseApiUrl, HttpResponse.BodyHandlers.ofString());

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
            apiClient.get(baseApiUrl, HttpResponse.BodyHandlers.ofString());
        });

        assertEquals("Failed to get URL: " + baseApiUrl, exception.getMessage());
    }

    @Test
    void canHandleInterruptedException() throws IOException, InterruptedException {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new InterruptedException("Interrupted Error"));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            apiClient.get(baseApiUrl, HttpResponse.BodyHandlers.ofString());
        });

        assertEquals("Failed to get URL: " + baseApiUrl, exception.getMessage());
    }

    @Test
    void canPostEndpoint() throws IOException, InterruptedException {
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("Success");
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponseMock);

        Map<String, String> headers = Map.of("Content-Type", "application/json");

        HttpResponse<String> response = apiClient.post(authUrl, "grant_type=client_credentials", headers, HttpResponse.BodyHandlers.ofString());

        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.statusCode(), "Response status code should be 200");
        assertEquals("Success", response.body(), "Response body should be 'Success'");
        verify(httpClientMock, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void canHandlePostIOException() throws IOException, InterruptedException {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new IOException("IO Error"));

        Map<String, String> headers = Map.of("Content-Type", "application/json");

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            apiClient.post(authUrl, "grant_type=client_credentials", headers, HttpResponse.BodyHandlers.ofString());
        });

        assertEquals("Failed to post to URL: " + authUrl, exception.getMessage());
    }

    @Test
    void canHandlePostInterruptedException() throws IOException, InterruptedException {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new InterruptedException("Interrupted Error"));

        Map<String, String> headers = Map.of("Content-Type", "application/json");

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            apiClient.post(authUrl, "grant_type=client_credentials", headers, HttpResponse.BodyHandlers.ofString());
        });

        assertEquals("Failed to post to URL: " + authUrl, exception.getMessage());
    }
}
