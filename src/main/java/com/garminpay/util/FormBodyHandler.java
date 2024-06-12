package com.garminpay.util;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Map;

public class FormBodyHandler {

    /**
     * Converts a Map to a URL-encoded string.
     *
     * @param formData The Map containing form data.
     * @return The URL-encoded string.
     */
    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> formData) {
        StringBuilder encodedFormData = new StringBuilder();

        for (Map.Entry<Object, Object> entry : formData.entrySet()) {
            if (!encodedFormData.isEmpty()) {
                encodedFormData.append("&");
            }
            encodedFormData.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            encodedFormData.append("=");
            encodedFormData.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }

        return HttpRequest.BodyPublishers.ofString(encodedFormData.toString());
    }
}
