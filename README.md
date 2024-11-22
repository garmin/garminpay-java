# Garmin Pay Java Library

Official Garmin Pay Java SDK

## Overview
The Garmin Pay SDK is a direct link to the Garmin Pay platform and allows bank issuers to easily provision a users card directly to a Garmin Pay wallet.
This SDK is intended to be used in Direct Push Provisioning scenarios and will return a deeplink for iOS and Android clients which can be used to have users finish the provisioning process in the Garmin Connect Mobile app.
The deeplink sends a user to the Garmin Connect Mobile app where they will then have their card added to the wallet.

It is important to note that oAuth and payload encryption are abstracted away by the SDK. Anytime the `checkHealthStatus` or `registerCard`
methods are called oAuth tokens to authenticate with Garmin Pay services are generated or used if previously generated. Anytime `registerCard` is called with a card, the card is encrypted securely to be sent to Garmin Pay.
## Onboarding
TBD

## Installation

### Requirments

- Java 1.8 or later

### Gradle

`implementation "garminpay:garminpay-java:0.9.0-SNAPSHOT"`

### Maven

```
<dependency>
  <groupId>garminpay</groupId>
  <artifactId>garminpay-java</artifactId>
  <version>0.9.0-SNAPSHOT</version>
</dependency>
```

### Others

Manually install the JAR

## Usage
### Initialize Garmin Pay
GarminPayExample.java
```java
import com.garminpay.GarminPayClient;

public class GarminPayExample {
    
    public static void main(String[] args) {
        // Create GarminPayClient with default HttpClient
        GarminPayClient client = new GarminPayClient("clientId", "clientSecret");
        ...
    }
}
```
#### Initialize Garmin Pay with custom HttpClient
This SDK allows for users to customize their own HttpClient for proxying and request configuration. We use [Apache HttpClient](https://hc.apache.org/httpcomponents-client-5.3.x/index.html).

GarminPayExample.java
```java
import com.garminpay.GarminPayClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHost;

public class GarminPayExample {

    public static void main(String[] args) {
        // Proxy settings
        String proxyHost = "your.proxy.host";
        int proxyPort = 8080;

        // Create proxy HTTP host
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);

        // Create HttpClient with proxy configuration
        HttpClient httpClient = HttpClients.custom()
            .setProxy(proxy)
            .build();

        // Create GarminPayClient with custom HttpClient
        GarminPayClient client = new GarminPayClient("clientId", "clientSecret", httpClient);
        ...
    }
}

```
#### Bean initialization

Initializing Garmin Pay as a [Spring Bean](https://docs.spring.io/spring-framework/reference/core/beans/definition.html) may also be beneficial for your uses.

GarminPayConfigExample.java

```java
import com.garminpay.GarminPayClient;
import com.garminpay.exception.GarminPayBaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GarminPayConfigExample {

    @Bean
    public GarminPayClient garminPayClient() {
        log.debug("Creating GarminPayClient");
        try {
            return new GarminPayClient("clientId", "clientSecret");
        } catch (IllegalArgumentException e) {
            // Handle this exception how you see fit
            log.warn("Failed to create GarminPayClient", e);
            e.printStackTrace();
            ...
        }
    }
}
```
**Note:** Note: if the client credentials are null or empty, an `IllegalArgumentException` will be thrown when the client is initialized. Otherwise, the provided credentials will be validated on the first request made to the Garmin Pay platform; not during client initialization.

### Checking the health of the Garmin Pay platform
The `checkHealthStatus` method will return a boolean representing the status of the Garmin Pay platform.

CheckHealthExample.java
```java
public class CheckHealthExample {
    public static void main (String[] args) {
        ...
        // Check health of GarminPay platform
        if (client.checkHealthStatus()) {
            // Register card
        } else {
            // Handle when GP platform is down
        }
        ...   
    }
}
```


### Registering a card
The `registerCard` method will return a map called `deepLinkUrls` containing the deeplink urls to Garmin Connect Mobile for both iOS (`ios`) and Android (`android`), or it will throw a relevant exception.

RegisterCardResponse
```json
{
  "deepLinkUrls": {
    "ios": "https://mock-dpp-url.com/payment/push/ios/provision?pushToken=jwstoken",
    "android": "https://mock-dpp-url.com/payment/push/android/provision?pushToken=jwstoken"
  }
}
```

The GarminPayCardData object requires that the `pan` field be present. The SDK allows for all other fields to be optional. 
However, by not providing critical information such as `cvv` and expiration dates, the card network may reject the provision request.

RegisterCardExample.java
```java
public class RegisterCardExample {

    public static void main(String[] args) {
        ...
        // Create Address object
        Address garminPayAddress = Address.builder()
            .name(...)
            .street1(...)
            .street2(...)
            .street3(...)
            .city(...)
            .state(...)
            .postalCode(...)
            .countryCode(...)
            .build();

        // Create GarminPayCardData object
        GarminPayCardData garminPayCardData = GarminPayCardData.builder()
            .pan(...)
            .cvv(...)
            .expMonth(...)
            .expYear(...)
            .name(...)
            .address(garminPayAddress)
            .build();

        // registerCard with Garmin Pay platform
        RegisterCardResponse response = client.registerCard(garminPayCardData);
        ...
    }
}
```

### Handling Maintenance Mode
Any request made through the SDK may return a response signaling that the platform is undergoing maintenance.
If this happens, the SDK will throw a GarminPayMaintenanceException.

CheckHealthAndMaintenanceExample.java
```java
public class CheckHealthExample {
    public static void main(String[] args) {
        ...
        try {
            client.checkHealthStatus();
        } catch (GarminPayMaintenanceException e) {
            // Handle GP platform maintenance mode
        }
        ...
    }
}
```

## Maintenance
### Adding license header to each file
If the License Check job fails due to a missing license header, you can add the license header to each file by running the following command:
```bash
mvn license:format
```
You can remove license headers from files by running the following command:
```bash
mvn license:remove
```
### Set up IntelliJ to auto-add license header to all new files
1. Navigate to Intellij IDEA -> Settings -> Editor -> Copyright
2. Change the Default project copyright to "Garmin Pay Software License Agreement"
