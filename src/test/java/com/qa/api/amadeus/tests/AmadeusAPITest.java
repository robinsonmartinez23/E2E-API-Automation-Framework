package com.qa.api.amadeus.tests;

import com.qa.api.base.BaseTest;
import com.qa.api.configmanager.ConfigManager;
import com.qa.api.constants.AuthType;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class AmadeusAPITest extends BaseTest {

    private String accessToken;

    /**
     * OLD version: it used post(..., clientId, clientSecret, grantType, ...), which does not use setup(),
     * so no timeouts, no logs and no status validation. Avoid it! Replaced by getAccessToken() below.
     */
//    @BeforeMethod
//    public void getOAuth2Token() {
//        Response response = restClient.post(BASE_URL_OAUTH2_AMADEUS, AMADEUS_OAUTH2_TOKEN_ENDPOINT,
//                ConfigManager.getProperty("clientid_amadeus"),
//                ConfigManager.getProperty("clientsecret_amadeus"),
//                ConfigManager.getProperty("granttype_amadeus"),
//                ContentType.URLENC);
//
//        accessToken = response.jsonPath().getString("access_token");
//        System.out.println("Access Token: "+ accessToken);
//        ConfigManager.setProperty("amadeus.token", accessToken); // Amadeus has its own key
//    }

    // OAuth 2.0 client_credentials: the app identifies itself with its client_id + client_secret (no user)
    // and receives an access_token, later sent as "Authorization: Bearer <token>".
    // Uses post(..., formParams, ...): timeouts, logs and status validation (200) come from setup().
    @BeforeMethod
    public void getAccessToken() {
        Map<String, String> tokenForm = new HashMap<>();
        tokenForm.put("grant_type", ConfigManager.getProperty("granttype_amadeus"));
        tokenForm.put("client_id", ConfigManager.getProperty("clientid_amadeus"));
        tokenForm.put("client_secret", ConfigManager.getProperty("clientsecret_amadeus"));

        Response response = restClient.post(BASE_URL_OAUTH2_AMADEUS, AMADEUS_OAUTH2_TOKEN_ENDPOINT,
                tokenForm, AuthType.NO_AUTH, ContentType.URLENC, 200); // no token yet: we are asking for one

        accessToken = response.jsonPath().getString("access_token");
        System.out.println("Access Token: " + accessToken);
        ConfigManager.setProperty("amadeus.token", accessToken); // Amadeus has its own key
    }

    @Test
    public void getFlightDetailsTest() {

        //https://test.api.amadeus.com/v1/shopping/flight-destinations?origin=PAR&maxPrice=200
        //https://test.api.amadeus.com
        //https://test.api.amadeus.com/v1/shopping/flight-destinations
        //?origin=PAR&maxPrice=200

        //Maps.of("origin", "PAR", "maxPrice", "200");
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("origin", "PAR");
        queryParams.put("maxPrice", "200");


        Response response = restClient.get(BASE_URL_OAUTH2_AMADEUS, AMADEUS_FLIGHT_DEST_ENDPOINT, queryParams, null, AuthType.BEARER_TOKEN, ContentType.ANY);
        Assert.assertEquals(response.getStatusCode(), 200);
    }
}
