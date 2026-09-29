package com.qa.api.gorest.tests;

import com.aventstack.chaintest.plugins.ChainTestListener;
import com.qa.api.base.BaseTest;
import com.qa.api.configmanager.ConfigManager;
import com.qa.api.constants.AppConstants;
import com.qa.api.constants.AuthType;
import com.qa.api.pojo.User;
import com.qa.api.utils.ExcelUtil;
import com.qa.api.utils.StringUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

@Epic("Epic 100: Go Rest Get User API Feature")
@Story("US 104: Feature go rest api - Create user api")
public class CreateUserTest extends BaseTest {

    /**
     * This method is useful when we have few (3-5) entries More than that is better use *.xml files
     */
    @DataProvider
    public Object[][] getUserData(){
        return new Object[][]{
                {"Robinson", "male", "active"},
                {"Amanda", "female", "active"},
                {"Frank", "male", "inactive"}
        };
    }

    @DataProvider
    public Object[][] getUserExcelData(){
        return ExcelUtil.readData(AppConstants.CREATE_USER_SHEET);
    }

    /*
     The @DataProvider is the tool that enables the Data Driven Testing (DDT):Logic and data are clearly separated
     DDT Hardcoded @Test (dataProvider = "getUserData")
     DDT Using external file @Test (dataProvider = "getUserExcelData")
     */
    //@Test (dataProvider = "getUserData")
    @Test (dataProvider = "getUserExcelData")
    public void createAUserWithDataProviderTest(String name, String gender, String status){

        User user = new User(null,name, StringUtils.getRandomEmailId(), gender, status);
        Response response = restClient.post(BASE_URL_GOREST,GOREST_USERS_ENDPOINT,user,null,null, AuthType.BEARER_TOKEN, ContentType.JSON);
        Assert.assertEquals(response.jsonPath().getString("name"), user.getName());
        Assert.assertEquals(response.jsonPath().getString("gender"), user.getGender());
        Assert.assertEquals(response.jsonPath().getString("status"), user.getStatus());
        Assert.assertNotNull(response.jsonPath().getString("id"), "User Id is null");
        ChainTestListener.log("User id: "+ response.jsonPath().getString("id")); // ChainTestListener log

    }

    @Test
    public void createAUserWithPOJOTest(){
        // Create a user object from a pojo class
        User user = new User(null,"Maria", StringUtils.getRandomEmailId(), "female", "inactive");
        Response response = restClient.post(BASE_URL_GOREST,GOREST_USERS_ENDPOINT,user,null,null, AuthType.BEARER_TOKEN, ContentType.JSON);
        Assert.assertEquals(response.jsonPath().getString("name"), user.getName());
        Assert.assertEquals(response.jsonPath().getString("gender"), user.getGender());
        Assert.assertEquals(response.jsonPath().getString("status"), user.getStatus());
        Assert.assertNotNull(response.jsonPath().getString("id"), "User Id is null");
        ChainTestListener.log("User id: "+ response.jsonPath().getString("id")); // ChainTestListener log

    }

    @Test (enabled = false)
    public void createAUserWithJsonStringTest(){
        String userJson = "{\n"
                + "\"name\": \"naveen\",\n"
                + "\"email\": \"naveen89913@gmail.com\",\n"  // Task: I need to make email random
                + "\"gender\": \"male\",\n"
                + "\"status\": \"active\"\n"
                + "}";

        Response response = restClient.post(BASE_URL_GOREST, GOREST_USERS_ENDPOINT, userJson, null, null, AuthType.BEARER_TOKEN, ContentType.JSON);
        Assert.assertEquals(response.jsonPath().getString("name"), "naveen");
        Assert.assertNotNull(response.jsonPath().getString("id"));
    }

    @Test (enabled = false)
    public void createAUserTestWithJsonfile() {
        File userFile = new File("./src/test/resources/jsons/user.json");
        Response response = restClient.post(BASE_URL_GOREST, GOREST_USERS_ENDPOINT, userFile, null, null, AuthType.BEARER_TOKEN, ContentType.JSON);
        Assert.assertEquals(response.jsonPath().getString("name"), "Amanda");
        Assert.assertNotNull(response.jsonPath().getString("id"));
    }
}
