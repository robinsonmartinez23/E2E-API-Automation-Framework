package com.qa.api.base;

import com.aventstack.chaintest.plugins.ChainTestListener;
import com.qa.api.client.RestClient;
import com.qa.api.configmanager.ConfigManager;
import com.qa.api.moking.WireMockSetup;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;

@Listeners(ChainTestListener.class) // Creates listener (test began, passed?, failed?) with this create the HTL report.
public class BaseTest {

    protected RestClient restClient;

    //***********API Base URLs***********//
    protected static String BASE_URL_GOREST;
    protected static String BASE_URL_CONTACTS;
    protected static String BASE_URL_REQRES;
    protected static String BASE_URL_BASIC_AUTH;
    protected static String BASE_URL_PRODUCTS;
    protected static String BASE_URL_OAUTH2_AMADEUS;
    protected static String BASE_URL_ERGAST_CIRCUIT;

    //***********API Endpoints***********//
    protected final static String GOREST_USERS_ENDPOINT = "/public/v2/users";
    protected final static String CONTACTS_LOGIN_ENDPOINT = "/users/login";
    protected final static String CONTACTS_ENDPOINT = "/contacts";
    protected final static String REQRES_ENDPOINT = "/api/users";
    protected final static String BASIC_AUTH_ENDPOINT = "/basic_auth";
    protected final static String PRODUCTS_ENDPOINT = "/products";
    protected final static String AMADEUS_OAUTH2_TOKEN_ENDPOINT = "/v1/security/oauth2/token";
    protected final static String AMADEUS_FLIGHT_DEST_ENDPOINT = "/v1/shopping/flight-destinations";
    protected final static String ERGAST_CIRCUIT_ENDPOINT = "/api/f1/2017/circuits.xml";

    //***********Localhost***********//
    protected final static String BASE_URL_MOCK_SERVER = "http://localhost:8089";


    @BeforeSuite
    public void setupAllureReport(){
        // Global REST Assured filter (not a TestNG listener):
        // intercepts every request/response and attaches them to the running test in the Allure report.
        // Test status (pass/fail) comes from the allure-testng listener, auto-registered via the pom.xml.
        RestAssured.filters((new AllureRestAssured()));
        BASE_URL_GOREST = ConfigManager.getProperty("baseurl.gorest").trim();
        BASE_URL_CONTACTS = ConfigManager.getProperty("baseurl.contacts").trim();
        BASE_URL_REQRES = ConfigManager.getProperty("baseurl.reqres").trim();
        BASE_URL_BASIC_AUTH = ConfigManager.getProperty("baseurl.basicAuth").trim();
        BASE_URL_PRODUCTS = ConfigManager.getProperty("baseurl.products").trim();
        BASE_URL_OAUTH2_AMADEUS = ConfigManager.getProperty("baseurl.oauth2Amadeus").trim();
        BASE_URL_ERGAST_CIRCUIT = ConfigManager.getProperty("baseurl.ergastCircuit").trim();
    }

    @BeforeTest
    public void setup(){
       restClient = new RestClient();
        WireMockSetup.startWireMockeServer();

    }
    @AfterTest
    public void stopMockServer(){
        WireMockSetup.stopWireMockServer();
    }
}
