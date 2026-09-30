package com.qa.api.moking.test;

import com.qa.api.base.BaseTest;
import com.qa.api.constants.AuthType;
import com.qa.api.moking.APIMocks;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class MockGetUserAPITest extends BaseTest {
    @Test
    public void getDummyUserMockAPITest() {
        APIMocks.defineGetUserMock(); // define the stubs of server

        Response response =
                restClient.get(BASE_URL_MOCK_SERVER, "/api/users", null, null, AuthType.NO_AUTH, ContentType.JSON);
        response.then().assertThat().statusCode(200);
    }


    @Test
    public void getDummyUserMockAPIWithJsonFileTest() {
        APIMocks.defineGetUserMockWithJsonFile(); // define the stubs of server

        Response response =
                restClient.get(BASE_URL_MOCK_SERVER, "/api/users", null, null, AuthType.NO_AUTH, ContentType.JSON);
        response.then().assertThat().statusCode(200);
    }


    @Test
    public void getDummyUserMockAPIWithQueryParamTest() {
        APIMocks.defineGetUserMockWithQueryParam(); // define the stubs of server

        Map<String, String> userQueryMap = new HashMap<String, String>();
        userQueryMap.put("name", "Tom");

        Response response =
                restClient.get(BASE_URL_MOCK_SERVER, "/api/users", userQueryMap, null, AuthType.NO_AUTH, ContentType.JSON);
        response.then().assertThat().statusCode(200);
    }
}
