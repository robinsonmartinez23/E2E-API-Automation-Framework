package com.qa.api.client;


import com.aventstack.chaintest.plugins.ChainTestListener;
import com.qa.api.configmanager.ConfigManager;
import com.qa.api.constants.AppConstants;
import com.qa.api.constants.AuthType;
import com.qa.api.exceptions.APIException;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.io.File;
import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.expect;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

public class RestClient {
    // Define Response Specifications
    private final ResponseSpecification responseSpec200 = expect().statusCode(200);
    private final ResponseSpecification responseSpec201 = expect().statusCode(201);
    private final ResponseSpecification responseSpec204 = expect().statusCode(204);
    private final ResponseSpecification responseSpec400 = expect().statusCode(400);
    private final ResponseSpecification responseSpec404 = expect().statusCode(404);

    private final ResponseSpecification responseSpec200or201 = expect().statusCode(anyOf(equalTo(200),equalTo(201)));
    private final ResponseSpecification responseSpec200or404 = expect().statusCode(anyOf(equalTo(200),equalTo(404)));

    // ===================== TIMEOUTS =====================
    // Without timeouts, a request that gets no answer can hang forever (see "NO RESPONSE?" below).
    // With timeouts, the hang becomes a clear exception that tells us WHERE it got stuck:
    // - Connect timeout: can't reach the server (network: firewall, proxy, VPN) -> "connect timed out"
    // - Read timeout: connected, but the server never answered                  -> "Read timed out"
    // - "http.connection.timeout": name reserved by Apache HttpClient -> max time to connect to the server.
    // - "http.socket.timeout": name reserved by Apache HttpClient -> max time to wait for a response once connected.
    // - These names must be written EXACTLY: a typo is silently ignored by Apache (no error, no timeout).
    // ====================================================
    private static final RestAssuredConfig TIMEOUT_CONFIG = RestAssured.config()    // Creates a new object with default configuration
                                                                                    // without new keyword (new RestAssuredConfig)
            .httpClient(HttpClientConfig.httpClientConfig() // httpClient returns the object edited with the new HTTP client section;
                                                            // HttpClientConfig.httpClientConfig() creates that section (default values)
                    .setParam(AppConstants.HTTP_CONNECTION_TIMEOUT, AppConstants.CONNECT_TIMEOUT_MS) // edit the section: 10 s to connect
                    .setParam(AppConstants.HTTP_SOCKET_TIMEOUT, AppConstants.READ_TIMEOUT_MS)); // edit the section: 30 s to wait for the response

    // ===================== PREVIOUS setup() (for reference) =====================
    // This is the original setup(), BEFORE adding timeouts.
    // NOTE: This setup does NOT handle timeouts: RestAssured.given() uses the default config,
    // so a request that gets no answer can hang forever. Kept commented out for learning purposes.
    // ============================================================================
    // private RequestSpecification setup(String baseUrl, AuthType authType, ContentType contentType) {
    //     ChainTestListener.log("API base url : "+ baseUrl);   // ChainTestListener log
    //     ChainTestListener.log("Auth Type : "+ authType.toString());   // ChainTestListener log
    //     RequestSpecification request = RestAssured.given() // start a new request
    //             .log().all()               // print the full request in the console
    //             .baseUri(baseUrl)          // base URL from the .properties file
    //             .contentType(contentType)  // format of what we send
    //             .accept(contentType);      // format we expect back
    // 
    //     switch (authType){
    //         case BEARER_TOKEN:
    //             request.header("Authorization", "Bearer "+ ConfigManager.getProperty("bearertoken"));
    //             break;
    //         case BASIC_AUTH:
    //             request.header("Authorization", "Basic " + generateBasicAuthToken());
    //             break;
    //         case API_KEY:
    //             request.header("x-api-key", ConfigManager.getProperty("apikey"));
    //             break;
    //         case NO_AUTH:
    //             System.out.println("Auth is not required");
    //             break;
    //         default:
    //             System.out.println("This AuthType is not supported, please check the AuthType enum");
    //             throw new APIException("===Invalid AuthType===");
    //     }
    //     return request;
    // }
    // ============================================================================


    private RequestSpecification setup(String baseUrl, AuthType authType, ContentType contentType) {
        ChainTestListener.log("API base url : "+ baseUrl);   // ChainTestListener log
        ChainTestListener.log("Auth Type : "+ authType.toString());   // ChainTestListener log
        RequestSpecification request = RestAssured.given() // start a new request
                .config(TIMEOUT_CONFIG)    // apply the timeouts (10 s connect / 30 s response)
                .log().all()               // print the full request in the console
                .baseUri(baseUrl)          // base URL from the .properties file
                .contentType(contentType)  // format of what we send
                .accept(contentType);      // format we expect back

        // ===================== AUTHENTICATION =====================
        // Authentication = Prove to the server who you are.
        // The test only says WHICH type it needs: AuthType.BEARER_TOKEN, AuthType.BASIC_AUTH, AuthType.API_KEY, etc;
        // the credentials come from the .properties file of the selected environment (ConfigManager).
        // If authentication fails, the server returns 401 Unauthorized.

        switch (authType){
            // BEARER TOKEN
            // The client sends a token it already has: "Authorization: Bearer <token>".
            // "Bearer" means "whoever carries this token gets access", like a concert ticket.
            // The token proves you logged in before, so no username/password is sent.
            // OAuth 2.0 is NOT a separate case: OAuth 2.0 is how you GET a token
            // (see the OAuth2 post() method, used by Amadeus). Once you have it, you send it here as a Bearer token.
            case BEARER_TOKEN:
                request.header("Authorization", "Bearer " + getBearerToken(baseUrl)); // each API gets ITS OWN token
                break;
            case BASIC_AUTH:
                request.header("Authorization", "Basic " + generateBasicAuthToken());
                break;
            case API_KEY:
                request.header("x-api-key", ConfigManager.getProperty("apikey"));
                break;
            case NO_AUTH:
                System.out.println("Auth is not required");
                break;
            default:
                System.out.println("This AuthType is not supported, please check the AuthType enum");
                throw new APIException("===Invalid AuthType===");
        }
        return request;
    }

    // ===================== ONE BEARER TOKEN PER API =====================
    // Each API has its own token, stored under its own key, so tests never overwrite each other's token.
    // Before: all APIs shared "bearertoken" -> if Contacts logged in first, GoRest sent the Contacts token -> 401.
    // - gorest.token   -> static token from the .properties file
    // - contacts.token -> set at runtime by ContactsAPITests (login)
    // - amadeus.token  -> set at runtime by AmadeusAPITest (OAuth 2.0)
    private String getBearerToken(String baseUrl) {
        if (baseUrl.equals(ConfigManager.getProperty("baseurl.gorest"))) {
            return ConfigManager.getProperty("gorest.token");
        }
        if (baseUrl.equals(ConfigManager.getProperty("baseurl.contacts"))) {
            return ConfigManager.getProperty("contacts.token");
        }
        if (baseUrl.equals(ConfigManager.getProperty("baseurl.oauth2Amadeus"))) {
            return ConfigManager.getProperty("amadeus.token");
        }
        throw new APIException("===No Bearer token configured for base URL: " + baseUrl + "===");
    }

    private String generateBasicAuthToken() {
        String credentials = ConfigManager.getProperty("basicauthusername") + ":" + ConfigManager.getProperty("basicauthpassword");
        //admin:admin --> "YWRtaW46YWRtaW4=" (base64 encoded value)
        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
        System.out.println("It is the encoded basic auth token ---> "+ basicAuth);
        return basicAuth;
    }

    private void applyParams(RequestSpecification request, Map<String, String> queryParams, Map<String,String> pathParams){
        ChainTestListener.log("Query Params : "+ queryParams); // ChainTestListener log
        ChainTestListener.log("Path Params : "+ pathParams); // ChainTestListener log
        if(queryParams != null){
            request.queryParams(queryParams);
        }
        if(pathParams != null){
            request.pathParams(pathParams);
        }
    }

    // ============ NO RESPONSE? THINK OF A PHONE CALL ============
    // If a request gets no HTTP status at all (not even a 404), check the exception:
    //
    // 1. "The phone number doesn't exist"      -> UnknownHostException (DNS)
    //    Fails right away. The name can't be found:
    //    wrong URL in the .properties file, no VPN, or the server no longer exists (e.g. Amadeus).
    //
    // 2. "Phone exists but is busy"             -> ConnectException: Connection refused
    //    Fails right away. The server is there, but the service is down.
    //
    // 3. "Phone exists but is not ringing"      -> SocketTimeoutException: connect timed out
    //    Waits. Something blocks the way: firewall, proxy or VPN.
    //
    // 4. "Someone picks up but says nothing"    -> SocketTimeoutException: Read timed out
    //    Waits. The server got the request but never answered.
    //    Without a timeout, the test can wait forever.
    //
    // First step: try the same request in Postman.
    // If it works there, the problem is in our code, not the server.
    // ============================================================

    //***********Get response from the API***************

    /**
     * Sends a GET request to a specified endpoint with given parameters and authentication type.
     *
     * @param baseUrl      The base URL for the API.
     * @param endPoint     The specific endpoint to which the GET request will be sent.
     * @param queryParams  A map of query parameters to be sent with the request.
     * @param pathParams   A map of path parameters to be sent with the request.
     * @param authType     The type of authentication to be used for the request.
     * @param contentType  The content type for the request.
     * @return The response received from the API call.
     */
    @Step("Calling GET api with base url: {0}")
    public Response get(String baseUrl, String endPoint,
                        Map<String, String> queryParams,
                        Map<String,String> pathParams,
                        AuthType authType,
                        ContentType contentType){
        RequestSpecification request = setup(baseUrl, authType, contentType);
        applyParams(request, queryParams, pathParams);
        Response response = request.get(endPoint).then().spec(responseSpec200or404).extract().response();
        response.prettyPrint();
        return response;
    }

    //****************Post*****************

    /**
     * Sends a POST request to a specified endpoint with the given parameters, request body, and authentication type.
     *
     * @param <T>          The type of the request body (file, pojo, etc.).
     * @param baseUrl      The base URL for the API.
     * @param endPoint     The specific endpoint to which the POST request will be sent.
     * @param body         The body of the request.
     * @param queryParams  A map of query parameters to be included in the request.
     * @param pathParams   A map of path parameters to be included in the request.
     * @param authType     The authentication type to be used for the request.
     * @param contentType  The content type of the request.
     * @return The response received from the API call.
     */
    public <T> Response post(String baseUrl, String endPoint,
                             T body,
                             Map<String, String> queryParams,
                             Map<String,String> pathParams,
                             AuthType authType,
                             ContentType contentType){
        RequestSpecification request = setup(baseUrl, authType, contentType);
        applyParams(request, queryParams, pathParams);
        Response response = request.body(body).post(endPoint).then().spec(responseSpec200or201).extract().response();
        response.prettyPrint();
        return response;
    }

    /**
     * Sends a POST request to a specified endpoint with the given file as the request body,
     * along with optional query parameters, path parameters, and authentication type.
     *
     * @param baseUrl      The base URL for the API.
     * @param endPoint     The specific endpoint to which the POST request will be sent.
     * @param file         The file to be sent as the request body.
     * @param queryParams  A map of query parameters to be included in the request.
     * @param pathParams   A map of path parameters to be included in the request.
     * @param authType     The authentication type to be used for the request.
     * @param contentType  The content type of the request.
     * @return The response received from the API call.
     */
    public Response post(String baseUrl, String endPoint,
                         File file,
                         Map<String, String> queryParams,
                         Map<String,String> pathParams,
                         AuthType authType,
                         ContentType contentType){
        RequestSpecification request = setup(baseUrl, authType, contentType);
        applyParams(request, queryParams, pathParams);
        Response response = request.body(file).post(endPoint).then().spec(responseSpec200or201).extract().response();
        response.prettyPrint();
        return response;
    }

    /**
     * Sends a POST request to the specified API endpoint using client credentials and
     * a grant type typically, for OAuth2 authentication. The request includes form parameters
     * and a defined content type.
     *
     * @param baseUrl      The base URL for the API.
     * @param endPoint     The specific endpoint to which the POST request is sent.
     * @param clientId     The client ID required for the request authentication.
     * @param clientSecret The client secret required for the request authentication.
     * @param grantType    The type of grant (e.g., client credentials) used in the request.
     * @param contentType  The content type of the request (e.g., application/json).
     * @return The response received from the API call encapsulated in a Response object.
     */
    public Response post(String baseUrl, String endPoint,
                             String clientId, String clientSecret, String grantType,
                             ContentType contentType){
        Response response = RestAssured.given()
                .contentType(contentType)
                .formParam("grant_type", grantType)
                .formParam("client_id", clientId)
                .formParam("client_secret", clientSecret)
                .when()
                .post(baseUrl+endPoint);
        response.prettyPrint();
        return response;

    }

    //***************PUT*****************

    /**
     * Sends a PUT request to a specified endpoint with the given parameters, request body, and authentication type.
     *
     * @param <T>          The type of the request body (e.g., POJO, JSON, etc.).
     * @param baseUrl      The base URL for the API.
     * @param endPoint     The specific endpoint to which the PUT request will be sent.
     * @param body         The body of the PUT request.
     * @param queryParams  A map of query parameters to be included in the request.
     * @param pathParams   A map of path parameters to be included in the request.
     * @param authType     The authentication type to be used for the request.
     * @param contentType  The content type of the request.
     * @return The response received from the API call.
     */
    public <T> Response put(String baseUrl, String endPoint,
                            T body,
                            Map<String, String> queryParams,
                            Map<String,String> pathParams,
                            AuthType authType,
                            ContentType contentType){
        RequestSpecification request = setup(baseUrl, authType, contentType);
        applyParams(request, queryParams, pathParams);
        Response response = request.body(body).put(endPoint).then().spec(responseSpec200).extract().response();
        response.prettyPrint();
        return response;
    }

    //*******************PATCH*******************

    /**
     * Sends a PATCH request to a specified endpoint with the given parameters, request body,
     * and authentication type.
     *
     * @param <T>          The type of the request body (e.g., POJO, JSON, etc.).
     * @param baseUrl      The base URL for the API.
     * @param endPoint     The specific endpoint to which the PATCH request will be sent.
     * @param body         The body of the PATCH request.
     * @param queryParams  A map of query parameters to be included in the request.
     * @param pathParams   A map of path parameters to be included in the request.
     * @param authType     The authentication type to be used for the request.
     * @param contentType  The content type of the request.
     * @return The response received from the API call.
     */
    public <T> Response patch(String baseUrl, String endPoint,
                              T body,
                              Map<String, String> queryParams,
                              Map<String,String> pathParams,
                              AuthType authType,
                              ContentType contentType){
        RequestSpecification request = setup(baseUrl, authType, contentType);
        applyParams(request, queryParams, pathParams);
        Response response = request.body(body).patch(endPoint).then().spec(responseSpec200).extract().response();
        response.prettyPrint();
        return response;
    }

    //********************DELETE*********************

    public Response delete(String baseUrl, String endPoint,
                           Map<String, String> queryParams,
                           Map<String,String> pathParams,
                           AuthType authType,
                           ContentType contentType){
        RequestSpecification request = setup(baseUrl, authType, contentType);
        applyParams(request, queryParams, pathParams);
        Response response = request.delete(endPoint).then().spec(responseSpec204).extract().response();
        response.prettyPrint();
        return response;
    }
}
