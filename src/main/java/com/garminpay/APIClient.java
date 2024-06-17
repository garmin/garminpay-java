package com.garminpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.exception.GarminPaySDKException;
import com.garminpay.model.response.ErrorResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;

/**
 * APIClient class responsible for configuring the HTTP client and providing a singleton instance.
 */
public class APIClient {

    private static APIClient instance;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private APIClient() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Returns the singleton instance of APIClient.
     *
     * @return the APIClient instance
     */
    public static synchronized APIClient getInstance() {
        if (instance == null) {
            instance = new APIClient();
        }
        return instance;
    }

    /**
     * Sends a GET request to the specified endpoint then returns the response.
     *
     * @param url           The URL to which the POST request will be sent.
     * @param responseClass The class type into which the response will be deserialized.
     * @param <T>           The type of the response class, extending ErrorResponse.
     * @return The deserialized response of type T.
     */
    public <T extends ErrorResponse> T get(String url, Class<T> responseClass) {
        T result;
        ClassicHttpRequest httpGet = ClassicRequestBuilder.get(url)
            .build();

        try {
            result = httpClient.execute(httpGet, response -> processResponse(response, responseClass));
        } catch (IOException e) {
            throw new GarminPaySDKException("HttpClient failed to GET URL: " + url, e);
        }

        return result;
    }

    /**
     * Sends a POST request to the specified endpoint with the provided request and headers, then returns the response.
     *
     * @param url           The URL to which the POST request will be sent.
     * @param responseClass The class type into which the response will be deserialized.
     * @param requestModel  The request model to be sent in the request body.
     * @param headers       Optional headers to be included in the request.
     * @param <T>           The type of the response class, extending ErrorResponse.
     * @param <R>           The type of the request model.
     * @return The deserialized response of type T.
     */
    public <T extends ErrorResponse, R> T post(String url, Class<T> responseClass, R requestModel, Header... headers) {
        T resultContent = null;

        String serializedRequestBody;
        try {
            serializedRequestBody = objectMapper.writeValueAsString(requestModel);
        } catch (JsonProcessingException e) {
            throw new GarminPaySDKException("Failed to parse request model: " + requestModel, e);
        }

        ClassicHttpRequest httpPost = ClassicRequestBuilder.post(url)
            .setEntity(new StringEntity(serializedRequestBody, ContentType.APPLICATION_JSON))
            .build();

        httpPost.setHeaders(headers);

        try {
            resultContent = httpClient.execute(httpPost, response -> processResponse(response, responseClass));
        } catch (IOException e) {
            throw new GarminPaySDKException("HttpClient failed to POST URL: " + url, e);
        }

        return resultContent;
    }

    private <T extends ErrorResponse> T processResponse(ClassicHttpResponse response, Class<T> responseClass) throws IOException {
        final HttpEntity entity = response.getEntity();
        String entityContent;
        T responseObject;

        try {
            entityContent = EntityUtils.toString(entity);
            responseObject = objectMapper.readValue(entityContent, responseClass);
            responseObject.setStatus(response.getCode());
        } catch (ParseException | JsonProcessingException e) {
            throw new GarminPaySDKException("Failed to parse response entity. ", e);
        }


        return responseObject;
    }
}
