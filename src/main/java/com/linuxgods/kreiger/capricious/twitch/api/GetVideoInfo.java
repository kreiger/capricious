package com.linuxgods.kreiger.capricious.twitch.api;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class GetVideoInfo extends TwitchApiCall {

    public JsonNode call(String videoId) {
        try {
            return request("https://api.twitch.tv/kraken/videos/" + videoId + "?client_id=" + CLIENT_ID);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
