package com.linuxgods.kreiger.twitch.chat;

import java.util.function.Consumer;

public interface TwitchChatSource {
    void consumeChatMessages(Consumer<TwitchChatMessage> consumer);
}
