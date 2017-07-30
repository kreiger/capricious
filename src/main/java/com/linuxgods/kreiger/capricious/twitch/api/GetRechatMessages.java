package com.linuxgods.kreiger.capricious.twitch.api;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

public class GetRechatMessages extends TwitchApiCall {
    public Optional<JsonNode> call(String videoId, long offsetSeconds) {
        try {
            JsonNode json = request("https://rechat.twitch.tv/rechat-messages?offset_seconds=" + offsetSeconds + "&video_id=v" + videoId);
            JsonNode dataJson = json.get("data");
            return Optional.of(dataJson);
        } catch (FileNotFoundException e) {
            return Optional.empty(); // No chat at all in vod
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
