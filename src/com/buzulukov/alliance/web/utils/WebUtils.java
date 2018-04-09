package com.buzulukov.alliance.web.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class WebUtils {

    private WebUtils() {
        // Nothing here, just to prevent class instancing.
    }

    public static String getResponse(String methodUrl, String... params) {
        String url = getUrl(methodUrl, params);
        return getResponse(url);
    }

    public static String getResponse(String url) {
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();

        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod("GET");
            //connection.setConnectTimeout(10);
            //connection.setReadTimeout(10);

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;

                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    // Logging.
                    System.out.println(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Connection failed: " +
                        connection.getResponseCode() + ", " +
                        connection.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response.toString();
    }

    public static String getUrl(String methodUrl, String... params) {
        StringBuilder url = new StringBuilder(methodUrl + "?");

        if (params.length != 0) {
            for (int i = 0; i < params.length - 1; ++i) {
                url.append(params[i]);
                url.append("&");
            }
            url.append(params[params.length - 1]);
        }
        return url.toString();
    }

}
