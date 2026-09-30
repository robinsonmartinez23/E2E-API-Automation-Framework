package com.qa.api.moking.test;

import com.qa.api.base.BaseTest;
import com.qa.api.constants.AuthType;
import com.qa.api.moking.OAuth2Mocks;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

// TASK 1 - "How does OAuth 2.0 authentication work?"
// OAuth 2.0 password grant against a WireMock token endpoint: credentials in, token out.
// Here the login IS what we test, so it goes inside each @Test (stubs first, then the request).
public class OAuth2MockTest extends BaseTest {

    @Test
    public void robinLoginReturnsGoldTokenTest() {
        OAuth2Mocks.defineLoginMocks(); // 1. teach WireMock what to answer (stubs BEFORE the request)

        // 2. login: form params of the OAuth 2.0 password grant
        Map<String, String> tokenForm = new HashMap<>();
        tokenForm.put("grant_type", "password");
        tokenForm.put("username", "robin");
        tokenForm.put("password", "robin123");

        Response response = restClient.post(BASE_URL_MOCK_SERVER, "/oauth/token",
                tokenForm, AuthType.NO_AUTH, ContentType.URLENC, 200); // no token yet (NO_AUTH): we are asking for one

        // 3. validate the token response
        Assert.assertEquals(response.jsonPath().getString("token_type"), "Bearer");
        Assert.assertNotNull(response.jsonPath().getString("access_token"), "access_token is null");
        Assert.assertEquals(response.jsonPath().getInt("expires_in"), 3600);
        Assert.assertEquals(response.jsonPath().getString("tier"), "GOLD"); // the SERVER says robin is GOLD
    }

    @Test
    public void wrongPasswordReturns401Test() {
        OAuth2Mocks.defineWrongLoginMocks(); // stub: robin + wrong password -> 401 invalid_grant

        Map<String, String> tokenForm = new HashMap<>();
        tokenForm.put("grant_type", "password");
        tokenForm.put("username", "robin");
        tokenForm.put("password", "wrong-password");

        Response response = restClient.post(BASE_URL_MOCK_SERVER, "/oauth/token",
                tokenForm, AuthType.NO_AUTH, ContentType.URLENC, 401); // negative test: we EXPECT 401

        Assert.assertEquals(response.jsonPath().getString("error"), "invalid_grant");
    }

    @Test
    public void mariaLoginReturnsPlatinumTokenTest() {
        OAuth2Mocks.defineLoginMocks();

        Map<String, String> tokenForm = new HashMap<>();
        tokenForm.put("grant_type", "password");
        tokenForm.put("username", "maria");
        tokenForm.put("password", "maria123");

        Response response = restClient.post(BASE_URL_MOCK_SERVER, "/oauth/token",
                tokenForm, AuthType.NO_AUTH, ContentType.URLENC, 200);

        // each user gets ITS OWN token, tier and scopes
        Assert.assertEquals(response.jsonPath().getString("access_token"), "maria-access-token");
        Assert.assertEquals(response.jsonPath().getString("tier"), "PLATINUM"); // the SERVER says maria is PLATINUM
        Assert.assertEquals(response.jsonPath().getString("scope"), "browsing streaming");
    }

    @Test
    public void unknownUserReturns401Test() {
        OAuth2Mocks.defineLoginMocks(); // no specific stub matches -> the FALLBACK (atPriority 10) answers

        Map<String, String> tokenForm = new HashMap<>();
        tokenForm.put("grant_type", "password");
        tokenForm.put("username", "unknown.user");
        tokenForm.put("password", "whatever");

        Response response = restClient.post(BASE_URL_MOCK_SERVER, "/oauth/token",
                tokenForm, AuthType.NO_AUTH, ContentType.URLENC, 401);

        Assert.assertEquals(response.jsonPath().getString("error"), "invalid_grant");
    }
}
