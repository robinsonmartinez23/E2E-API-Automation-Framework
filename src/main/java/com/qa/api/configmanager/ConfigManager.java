package com.qa.api.configmanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static Properties properties = new Properties();

    // mvn clean install -Denv=qa/stage/dev/prod/uat  --> select the environment
    // mvn clean install --> No environment selected (We need to set default env.)
    // env --> environment variable (System)
    static {
      //Get key env (-Denv) from Jenkinsfile, if you don't find it, set prod by default
      String envName = System.getProperty("env","prod");
      System.out.println("Running test on environment: "+envName);
      String fileName = "config_"+envName+".properties";
      //InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream("config/config.properties");
      InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream(fileName);
      if (inputStream != null) {
          try {
              properties.load(inputStream);
              //System.out.println("properties ---> "+properties);
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key).trim();
    }
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

}
