package com.garminpay.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for publishing JSON bodies in HTTP requests.
 */
public class JsonBodyPublisher {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Converts an object to a JSON string and returns an HttpRequest.BodyPublisher for that JSON string.
     *
     * @param object the object to be converted to JSON
     * @return a BodyPublisher for the JSON string
     * @throws RuntimeException if the object cannot be serialized to JSON
     */
    public static HttpRequest.BodyPublisher ofObject(Object object) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(object);
            return HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
