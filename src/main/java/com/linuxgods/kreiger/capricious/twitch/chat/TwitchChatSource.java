package com.linuxgods.kreiger.capricious.twitch.chat;

import io.reactivex.Observable;

import java.util.Optional;

public interface TwitchChatSource {
    Observable<TwitchChatMessage> getObservable();

    String getName();

    Optional<String> getLogo();
}
