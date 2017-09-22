package com.linuxgods.kreiger.capricious.twitch.chat;

import io.reactivex.Observable;

import java.net.URI;
import java.util.Optional;

public interface TwitchChatSource {
    Observable<TwitchChatMessage> getObservable();

    URI getURI();

    String getName();

    Optional<String> getLogo();
}
