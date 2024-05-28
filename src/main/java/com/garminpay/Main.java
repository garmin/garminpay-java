package com.garminpay;

import com.garminpay.proxy.GarminPayProxy;

import java.net.http.HttpResponse;

public class Main {
    /**
     * Acts as a mock service.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        GarminPayProxy proxy = new GarminPayProxy();
        // For a string response
        HttpResponse<String> response = proxy.getRootEndpoint(HttpResponse.BodyHandlers.ofString());
        System.out.println("Response: " + response.body());

        // For a byte array response
        HttpResponse<byte[]> responseBytes = proxy.getRootEndpoint(HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("Response length: " + responseBytes.body().length);

    }
}


