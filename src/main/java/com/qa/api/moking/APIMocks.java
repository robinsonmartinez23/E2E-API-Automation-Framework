package com.qa.api.moking;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class APIMocks {
    //*****************Create Mock/Stub for GET CALL**************//

    public static void defineGetUserMock() {
        //http://localhost:8089/api/users
        stubFor(get(urlEqualTo("/api/users"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("server-name", "bankserver")
                        .withBody("{\n"
                                + "    \"_id\": 107,\n"
                                + "    \"name\": \"Robin\",\n"
                                + "    \"age\": 47,\n"
                                + "    \"status\": \"Active\"\n"
                                + "}")
                )
        );
    }


    public static void defineGetUserMockWithJsonFile() {
        //http://localhost:8089/api/users
        stubFor(get(urlEqualTo("/api/users"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("server-name", "bankserver")
                        .withBodyFile("mockuser.json") // WireMock ALWAYS will search file name in */resourses/__file
                )
        );
    }


    public static void defineGetUserMockWithQueryParam() {
        //http://localhost:8089/api/users?name=Tom
        stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("name", equalTo("Tom"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("server-name", "bankserver")
                        .withBodyFile("mockuser.json")
                )
        );
    }




    //********************Create mock.stub for POST CALL**********//

    public static void defineCreateUserMock() {

        stubFor(post(urlEqualTo("/api/users"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("server-name", "bankserver")
                        .withBody("{\n"
                                + "    \"_id\": 107,\n"
                                + "    \"name\": \"Robin\",\n"
                                + "    \"age\": 47,\n"
                                + "    \"status\": \"Active\"\n"
                                + "}"
                        )

                )
        );
    }
}
