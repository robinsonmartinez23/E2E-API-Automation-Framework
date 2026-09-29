package com.qa.api.reqres.tests;

import com.qa.api.base.BaseTest;
import com.qa.api.constants.AuthType;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ReqResTest extends BaseTest {
    @Test
    public void getUserTest(){
        Map<String,String> queryParams = new java.util.HashMap<>();
        queryParams.put("page", "2");

        Response response = restClient.get(BASE_URL_REQRES,REQRES_ENDPOINT,queryParams,null, AuthType.API_KEY, ContentType.JSON);
        Assert.assertEquals(response.statusCode(), 200);
    }
}
