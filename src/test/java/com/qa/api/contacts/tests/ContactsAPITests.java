package com.qa.api.contacts.tests;

import com.qa.api.base.BaseTest;
import com.qa.api.configmanager.ConfigManager;
import com.qa.api.constants.AuthType;
import com.qa.api.pojo.ContactsCredentials;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Epic 100: Contacts Get API Feature")
@Story("US 200: Feature Contacts api - Get constats api")
public class ContactsAPITests extends BaseTest {

    private String tokenId;

    @BeforeMethod
    public void getToken(){
        ContactsCredentials credentials = ContactsCredentials.builder()
                .email("robinsonmartinez23@hotmail.com")
                .password("Rmm12071979!")
                .build();
        Response response = restClient.post(BASE_URL_CONTACTS, CONTACTS_LOGIN_ENDPOINT, credentials, null, null, AuthType.NO_AUTH, ContentType.JSON);
        Assert.assertEquals(response.statusCode(), 200);
        tokenId = response.jsonPath().getString("token");
        System.out.println("Contacts login JWT token ====>" + tokenId);
        ConfigManager.setProperty("contacts.token", tokenId); // Contacts has its own key, so it no longer overwrites the GoRest token
    }

    @Test
    public void getAllContactsTest(){
        restClient.get(BASE_URL_CONTACTS,CONTACTS_ENDPOINT,null, null,AuthType.BEARER_TOKEN, ContentType.JSON);

    }
}
