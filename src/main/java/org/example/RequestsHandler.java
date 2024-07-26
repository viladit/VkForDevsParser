package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestsHandler {

    public static String getMethodInfo(String methodName, String token) throws IOException {
        String urlTemplate = "https://api.vk.com/method/documentation.getPage?access_token=";
        String version = "5.131";
        String language = "ru";
        URL url = new URL(urlTemplate + token + "&v=" + version + "&lang=" + language +"&page=%2Fmethod%2F" + methodName);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            return null;
        }
    }
}
