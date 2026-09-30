package com.qa.api.moking;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

// OAuth 2.0 token endpoint stubs for the interview lab (tasks 1 to 5).
// One endpoint (/oauth/token) that answers differently depending on the form params it receives.
public class OAuth2Mocks {

    public static void defineLoginMocks() {
        // robin (GOLD tier in the server's "database"), correct password -> 200 with a token
        // The tier is NOT in the username: the server assigns it and returns it in the response.
        stubFor(post(urlEqualTo("/oauth/token"))
                .withFormParam("grant_type", equalTo("password"))
                .withFormParam("username", equalTo("robin"))
                .withFormParam("password", equalTo("robin123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"robin-access-token\", "
                                + "\"token_type\": \"Bearer\", \"expires_in\": 3600, \"tier\": \"GOLD\", \"scope\": \"browsing\"}")));

        // maria (PLATINUM tier), correct password -> 200 with another token (more scopes)
        stubFor(post(urlEqualTo("/oauth/token"))
                .withFormParam("grant_type", equalTo("password"))
                .withFormParam("username", equalTo("maria"))
                .withFormParam("password", equalTo("maria123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"maria-access-token\", "
                                + "\"token_type\": \"Bearer\", \"expires_in\": 3600, \"tier\": \"PLATINUM\", \"scope\": \"browsing streaming\"}")));

        // FALLBACK -> any other POST to /oauth/token -> 401 invalid_grant
        // atPriority(10): the LOWER number wins. Stubs without priority are 5, so this one only answers
        // when no specific stub matches (like the default of a switch).
        stubFor(post(urlEqualTo("/oauth/token"))
                .atPriority(10)
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"invalid_grant\"}")));
    }

    public static void defineWrongLoginMocks() {
        // robin, WRONG password -> 401 invalid_grant (no token: a server never gives a token for bad credentials)
        stubFor(post(urlEqualTo("/oauth/token"))
                .withFormParam("grant_type", equalTo("password"))
                .withFormParam("username", equalTo("robin"))
                .withFormParam("password", equalTo("wrong-password"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"invalid_grant\"}")));
    }
}
