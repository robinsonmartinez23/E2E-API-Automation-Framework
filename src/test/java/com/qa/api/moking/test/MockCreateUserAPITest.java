package com.qa.api.moking.test;

import com.qa.api.base.BaseTest;
import com.qa.api.constants.AuthType;
import com.qa.api.moking.APIMocks;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MockCreateUserAPITest extends BaseTest {
    @Test
    public void createAFakeUserTest() {

        APIMocks.defineCreateUserMock();

        String name = "Robin";
        String dummyUserJson = "{\n"
                + "    \"name\": \"" + name + "\",\n"
                + "    \"age\": 47\n"
                + "}";

        Response response = restClient.post(BASE_URL_MOCK_SERVER, "/api/users", dummyUserJson, null, null, AuthType.NO_AUTH, ContentType.JSON);

        // Assert using REST Assured
        response.then().assertThat().statusCode(201);

        // Assert using TestNG
        Assert.assertEquals(response.statusCode(), 201);
        Assert.assertEquals(response.jsonPath().getString("name"), name);
        Assert.assertEquals(response.jsonPath().getInt("age"), 47);
        Assert.assertEquals(response.jsonPath().getString("status"), "Active");
        Assert.assertTrue(response.jsonPath().getInt("_id") > 0);
    }
}
