package com.linuxgods.kreiger.capricious.twitch.chat.vod;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

class VideoInfo {
    private final String name;
    private final long length;
    private final Instant startInstant;

    static VideoInfo fromJson(JsonNode json) {
        String recordedAtString = json.get("recorded_at").asText();
        return new VideoInfo(json.get("title").asText(), json.get("length").asLong(), Instant.parse(recordedAtString));
    }

    private VideoInfo(String name, long length, Instant startInstant) {
        this.name = name;
        this.length = length;
        this.startInstant = startInstant;
    }

    String getName() {
        return name;
    }

    long getLength() {
        return length;
    }

    Instant getStartInstant() {
        return startInstant;
    }
}
