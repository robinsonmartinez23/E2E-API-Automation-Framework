package com.qa.api.constants;

public class AppConstants {
    public static final String BASE_URL = "https://reqres.in/api/";
    public static final String USER_ID = "2";
    public static final String USER_NAME = "morpheus";
    public static final String USER_EMAIL = "<EMAIL>";
    public static final String USER_PHONE = "024-648-3804";
    public static final String USER_JOB = "zion resident";
    public static final String USER_STATUS = "active";
    public static final String USER_AVATAR = "https://s3.amazonaws.com/uifaces/faces/twitter/marcoramires/128.jpg";
    public static final String USER_PASSWORD = "<PASSWORD>";

    public static final String CREATE_USER_SHEET = "createuser";

    // Timeouts (milliseconds) used by RestClient for every request
    public static final int CONNECT_TIMEOUT_MS = 10000; // 10 seconds to open the connection (network)
    public static final int READ_TIMEOUT_MS = 30000;    // 30 seconds to wait for the response (server)
}
