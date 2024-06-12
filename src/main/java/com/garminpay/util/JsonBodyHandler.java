package com.garminpay.util;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garminpay.exception.GarminPaySDKException;

/**
 * A generic HTTP response body handler that maps the response body to a specified type.
 *
 * @param <T> The type to which the response body should be mapped.
 */
public class JsonBodyHandler<T> implements HttpResponse.BodyHandler<T> {
    private final Class<T> type;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructs a new JsonBodyHandler for the specified type.
     *
     * @param type The class of the type to which the response body should be mapped.
     */
    public JsonBodyHandler(Class<T> type) {
        this.type = type;
    }

    /**
     * Applies this response body handler by creating a subscriber that converts the response body
     * from a string to the specified type.
     *
     * @param responseInfo The response information.
     * @return A body subscriber that converts the response body to the specified type.
     */
    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        return HttpResponse.BodySubscribers.mapping(
            HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
            (String body) -> {
                try {
                    return objectMapper.readValue(body, type);
                } catch (IOException e) {
                    throw new GarminPaySDKException("Failed to parse response", e);
                }
            }
        );
    }
}
