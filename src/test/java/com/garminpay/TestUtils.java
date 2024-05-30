package com.garminpay;

import com.garminpay.model.Address;
import com.garminpay.model.CardData;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import lombok.SneakyThrows;

public class TestUtils {
    public static final Address TESTING_ADDRESS = Address.builder()
        .name("Billing Address")
        .street1("123 Main St")
        .city("Anytown")
        .state("CO")
        .postalCode("12345")
        .countryCode("US")
        .build();

    public static final CardData TESTING_CARD_DATA = CardData.builder()
        .pan("1234567890123456")
        .cvv("123")
        .expMonth(12)
        .expYear(2025)
        .name("John Doe")
        .address(TESTING_ADDRESS)
        .build();

    public static final ECKey TESTING_ECC_KEY = generateECKey();

    @SneakyThrows(JOSEException.class)
    private static ECKey generateECKey() {
        return new ECKeyGenerator(Curve.P_256).generate();
    }

    public static final String TESTING_PUBLIC_ECC_KEY_STRING = TESTING_ECC_KEY.toPublicJWK().toJSONString();
}
