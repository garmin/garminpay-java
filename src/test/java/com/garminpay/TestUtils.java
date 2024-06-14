package com.garminpay;

import com.garminpay.model.Address;
import com.garminpay.model.GarminPayCardData;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

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
}
