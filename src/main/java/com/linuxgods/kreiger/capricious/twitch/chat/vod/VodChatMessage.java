package com.linuxgods.kreiger.capricious.twitch.chat.vod;

import com.fasterxml.jackson.databind.JsonNode;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import javafx.util.Pair;

import java.time.Instant;
import java.util.*;

class VodChatMessage extends TwitchChatMessage {

    private VodChatMessage(Instant instant, String color, String message, List<Emote> emotes) {
        super(instant, color, message, emotes);
    }

    static Optional<VodChatMessage> createFromJson(JsonNode json) {
        JsonNode messageData = json.get("attributes");
        if (messageData.get("deleted").asBoolean()) {
            return Optional.empty();
        }

        String message = messageData.get("message").asText();
        Instant instant = Instant.ofEpochMilli(messageData.get("timestamp").asLong());
        String color = messageData.get("color").isNull() ? null : messageData.get("color").asText();
        List<Emote> emotes = getEmotes(messageData, message);

        return Optional.of(new VodChatMessage(instant, color, message, emotes));
    }

    private static List<Emote> getEmotes(JsonNode messageData, String message) {
        List<Emote> emotes = new ArrayList<>();
        JsonNode tags = messageData.get("tags");
        tags.get("emotes").fields().forEachRemaining(e -> convertToEmoteAndAdd(e, message, emotes));
        emotes.sort(Comparator.comparingInt(Emote::getStartIndex));
        return emotes;
    }

    private static void convertToEmoteAndAdd(Map.Entry<String, JsonNode> e, String message, List<Emote> emotes) {
        int id = Integer.parseInt(e.getKey());
        List<Pair<Integer, Integer>> indices = new ArrayList<>();
        e.getValue().forEach(l -> {
            Iterator<JsonNode> elements = l.elements();
            int firstIndex = elements.next().intValue();
            int lastIndex = elements.next().intValue();
            indices.add(new Pair<>(firstIndex, lastIndex));
        });

        indices.forEach(i -> emotes.add(new Emote(id, i.getKey(), message.substring(i.getKey(), i.getValue() + 1))));
    }
}
