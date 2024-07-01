package com.garminpay.model.dto;

import com.garminpay.exception.GarminPayApiException;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Value
@Builder
@Slf4j
public class APIResponseDTO {
    int status;
    String content;
    Header[] headers;
    String path;

    /**
     * Converts a ClassicHttpResponse object to an APIResponseDTO object.
     *
     * @param response The response to build the DTO from
     * @param path The path used by the API
     * @return An APIResponseDTO object
     */
    public static APIResponseDTO fromHttpResponse(ClassicHttpResponse response, String path) {
        log.debug("Mapping {} http response to DTO", path);
        try {
            return APIResponseDTO.builder()
                .status(response.getCode())
                .content(EntityUtils.toString(response.getEntity()))
                .headers(response.getHeaders())
                .path(path)
                .build();
        } catch (ParseException | IOException e) {
            log.warn("Failed to parse http response to DTO", e);
            throw new GarminPayApiException(path, "Failed to build APIResponseDTO from ClassicHttpResponse");
        }
    }

    /**
     * Filters headers of a response object to find and return the CF-RAY field if it exists.
     *
     * @return CF-RAY id
     */
    public Optional<String> findCFRay() {
        if (headers != null) {
            return Arrays.stream(headers)
                .filter(header -> "CF-RAY".equalsIgnoreCase(header.getName()))
                .findFirst()
                .map(Header::getValue);
        }
        return Optional.empty();
    }

    /**
     * Filters headers of a response object to find and return the x-request-id field if it exists.
     *
     * @return x-request-id
     */
    public Optional<String> findXRequestId() {
        if (headers != null) {
            return Arrays.stream(headers)
                .filter(header -> "x-request-id".equalsIgnoreCase(header.getName()))
                .findFirst()
                .map(Header::getValue);
        }
        return Optional.empty();
    }
}
