package org.example.helpers;

import java.net.HttpURLConnection;
import java.net.URL;

public class URLTester {

    public static boolean isUrlResponding(URL u) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            return responseCode >= 200 && responseCode < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
