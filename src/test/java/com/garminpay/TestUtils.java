package com.garminpay;

import com.garminpay.model.Address;
import com.garminpay.model.GarminPayCardData;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import java.util.Arrays;
import java.util.UUID;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import wiremock.org.apache.commons.lang3.StringUtils;

public class TestUtils {

    public static final Address TESTING_ADDRESS = Address.builder()
        .name("Billing Address")
        .street1("123 Main St")
        .city("Anytown")
        .state("CO")
        .postalCode("12345")
        .countryCode("US")
        .build();

    public static final GarminPayCardData TESTING_CARD_DATA = GarminPayCardData.builder()
        .pan("9999449825552964")
        .cvv("123")
        .expMonth(12)
        .expYear(2025)
        .name("John Doe")
        .address(TESTING_ADDRESS)
        .build();

    public static final String TESTING_KEY_ID = UUID.randomUUID().toString();

    public static final String TESTING_ENCODED_PUBLIC_ECC_KEY;

    public static final String TESTING_ENCODED_PRIVATE_ECC_KEY;

    static {
        try {
            ECKey testingECCkey = generateECKey();
            TESTING_ENCODED_PUBLIC_ECC_KEY = String.valueOf(Hex.encodeHex(testingECCkey.toPublicKey().getEncoded()));
            TESTING_ENCODED_PRIVATE_ECC_KEY = String.valueOf(Hex.encodeHex(testingECCkey.toPrivateKey().getEncoded()));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows(JOSEException.class)
    private static ECKey generateECKey() {
        return new ECKeyGenerator(Curve.P_256).generate();
    }

    public static final BasicHeader CF_RAY_HEADER = new BasicHeader("CF-RAY", "testing-cf-ray");
    public static final BasicHeader X_REQUEST_ID_HEADER = new BasicHeader("x-request-id", "testing-x-request-id");

    public static final Header[] TESTING_HEADERS = {
        CF_RAY_HEADER,
        X_REQUEST_ID_HEADER
    };

    public static boolean checkForHeader(Header expectedHeader, Header[] actualHeaders) {
        return Arrays.stream(actualHeaders)
            .anyMatch(
                h -> StringUtils.equalsIgnoreCase(expectedHeader.getName(), h.getName())
                    && StringUtils.equalsIgnoreCase(expectedHeader.getValue(), h.getValue())
            );
    }

    public static boolean checkForHeader(String expectedHeaderName, Header[] actualHeaders) {
        return Arrays.stream(actualHeaders)
            .anyMatch(
                h -> StringUtils.equalsIgnoreCase(expectedHeaderName, h.getName())
            );
    }
}
