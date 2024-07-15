package com.garminpay.client;

import com.garminpay.exception.GarminPayApiException;
import com.garminpay.model.SDKVersion;
import com.garminpay.model.dto.APIResponseDTO;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class APIClientTest {
    private static final String TESTING_URL = "http://testing";
    private static final String VERSION_HEADER_NAME = "X-GP-SDK-Version";
    private APIClient client;
    private CloseableHttpClient httpClientMock;

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
            .status(HttpStatus.SC_OK)
            .build();

        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class))).thenReturn(responseSuccess);

        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL).build();

        APIResponseDTO response = client.executeRequest(request);

        assertEquals(HttpStatus.SC_OK, response.getStatus());
        verify(httpClientMock, times(1)).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));
    }

    @Test
    void canPostEndpoint() throws IOException {
        APIResponseDTO responseSuccess = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .build();

        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class))).thenReturn(responseSuccess);

        ClassicHttpRequest request = ClassicRequestBuilder.post(TESTING_URL).build();

        APIResponseDTO response = client.executeRequest(request);

        assertEquals(HttpStatus.SC_OK, response.getStatus());
        verify(httpClientMock, times(1)).execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class));
    }

    @Test
    void canHandleIOException() throws IOException {
        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
            .thenThrow(new IOException("Simulated Interrupt Exception"));

        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL).build();

        assertThrows(GarminPayApiException.class, () -> client.executeRequest(request));
    }

    @Test
    void requestHasVersionHeader() throws IOException {
        APIResponseDTO responseSuccess = APIResponseDTO.builder()
            .status(HttpStatus.SC_OK)
            .build();

        when(httpClientMock.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class))).thenReturn(responseSuccess);

        ClassicHttpRequest request = ClassicRequestBuilder.get(TESTING_URL).build();

        client.executeRequest(request);

        ArgumentCaptor<ClassicHttpRequest> requestCaptor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
        verify(httpClientMock).execute(requestCaptor.capture(), any(HttpClientResponseHandler.class));
        ClassicHttpRequest capturedRequest = requestCaptor.getValue();

        Header[] headers = capturedRequest.getHeaders();
        BasicHeader expectedHeader = new BasicHeader(VERSION_HEADER_NAME, SDKVersion.VERSION);

        boolean headerFound = Arrays.stream(headers)
            .anyMatch(header -> header.getName().equals(expectedHeader.getName()) && header.getValue().equals(expectedHeader.getValue()));

        assertTrue(headerFound);
    }

}
