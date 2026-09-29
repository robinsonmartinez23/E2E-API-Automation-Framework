package com.qa.api.schema.tests;

import com.qa.api.base.BaseTest;

import com.qa.api.configmanager.ConfigManager;
import com.qa.api.constants.AuthType;
import com.qa.api.pojo.User;
import com.qa.api.utils.SchemaValidator;
import com.qa.api.utils.StringUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@Epic("Epic 100: Go Rest Get User API Feature")
@Story("US 200: Feature go rest api - Schema Validation")
public class GoRestUserAPISchemaTest extends BaseTest {

    @Test
    public void getAllUsersAPISchemaTest() {
        Response response = restClient.get(BASE_URL_GOREST, GOREST_USERS_ENDPOINT, null, null, AuthType.BEARER_TOKEN, ContentType.ANY);
        Assert.assertTrue(SchemaValidator.validateSchema(response, "schema/getuserschema.json"));
    }
    @Test
    public void createUserAPISchemaTest() {
        User user = User.builder()
                .name("Frank")
                .status("active")
                .email(StringUtils.getRandomEmailId())
                .gender("male")
                .build();

        Response response = restClient.post(BASE_URL_GOREST, GOREST_USERS_ENDPOINT, user, null, null, AuthType.BEARER_TOKEN, ContentType.JSON);
        Assert.assertTrue(SchemaValidator.validateSchema(response, "schema/createuserschema.json"));

    }
}
