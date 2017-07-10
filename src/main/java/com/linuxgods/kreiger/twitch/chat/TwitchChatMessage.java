package com.linuxgods.kreiger.twitch.chat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TwitchChatMessage {

    Instant getInstant();

    Optional<String> getColor();

    String getMessage();

    <T> List<T> accept(TextOrEmoteVisitor<T> textOrEmoteVisitor);

    interface TextOrEmoteVisitor<T> {
        T visitText(String text);
        T visitEmote(String url);
    }
}
