package com.garminpay.proxy;

import com.garminpay.APIClient;
import com.garminpay.exception.GarminPayApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GarminPayProxyTest {
    private GarminPayProxy garminPayProxy;
    private APIClient apiClientMock;
    private HttpResponse<String> httpResponseMock; // Used to store response data received from mock

    @BeforeEach
    void setUp() {
        apiClientMock = mock(APIClient.class);
        httpResponseMock = mock(HttpResponse.class);
        garminPayProxy = new GarminPayProxy();

        // Used reflection to inject the mocked APIClient into the GarminPayProxy instance
        try {
            Field apiClientField = GarminPayProxy.class.getDeclaredField("apiClient");
            apiClientField.setAccessible(true);
            apiClientField.set(garminPayProxy, apiClientMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new GarminPayApiException("Could not get set mock because: ", e);
        }
    }

    @Test
    void canGetRootEndpoint() {
        when(apiClientMock.get(eq("/"), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponseMock);
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("Success");

        HttpResponse<String> response = garminPayProxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());

        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.statusCode(), "Response status code should be 200");
        assertEquals("Success", response.body(), "Response body should be 'Success'");
        verify(apiClientMock, times(1)).get(eq("/"), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void canHandleGarminPayApiException() {
        when(apiClientMock.get(eq("/"), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new GarminPayApiException("Error", new Exception()));

        GarminPayApiException exception = assertThrows(GarminPayApiException.class, () -> {
            garminPayProxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());
        });

        assertEquals("Error", exception.getMessage());
    }
}
