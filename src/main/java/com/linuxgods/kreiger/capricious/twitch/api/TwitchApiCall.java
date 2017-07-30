package com.linuxgods.kreiger.capricious.twitch.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

abstract class TwitchApiCall {
    static final String CLIENT_ID = "isaxc3wjcarzh4vgvz11cslcthw0gw"; //TODO: Register our own ID

    private static ObjectMapper mapper = new ObjectMapper();

    JsonNode request(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String jsonString = reader.readLine();
        reader.close();

        return mapper.readTree(jsonString);
    }
}
