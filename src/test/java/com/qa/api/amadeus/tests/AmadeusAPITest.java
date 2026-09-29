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

    @BeforeMethod
    public void getOAuth2Token() {
        Response response = restClient.post(BASE_URL_OAUTH2_AMADEUS, AMADEUS_OAUTH2_TOKEN_ENDPOINT,
                ConfigManager.getProperty("clientid_amadeus"), ConfigManager.getProperty("clientsecret_amadeus"), ConfigManager.getProperty("granttype_amadeus"), ContentType.URLENC);

        accessToken = response.jsonPath().getString("access_token");
        System.out.println("Access Token: "+ accessToken);
        ConfigManager.setProperty("amadeus.token", accessToken); // Amadeus has its own key
    }

//    @Test
//    public void getFlightDetailsTest() {
//
//        //https://test.api.amadeus.com/v1/shopping/flight-destinations?origin=PAR&maxPrice=200
//        //https://test.api.amadeus.com
//        //https://test.api.amadeus.com/v1/shopping/flight-destinations
//        //?origin=PAR&maxPrice=200
//
//        //Maps.of("origin", "PAR", "maxPrice", "200");
//        Map<String, String> queryParams = new HashMap<String, String>();
//        queryParams.put("origin", "PAR");
//        queryParams.put("maxPrice", "200");
//
//
//        Response response = restClient.get(BASE_URL_OAUTH2_AMADEUS, AMADEUS_FLIGHT_DEST_ENDPOINT, queryParams, null, AuthType.BEARER_TOKEN, ContentType.ANY);
//        Assert.assertEquals(response.getStatusCode(), 200);
//    }
}
