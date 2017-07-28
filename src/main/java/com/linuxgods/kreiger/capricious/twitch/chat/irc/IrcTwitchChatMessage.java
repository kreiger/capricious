package com.linuxgods.kreiger.capricious.twitch.chat.irc;

import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import org.kitteh.irc.client.library.element.MessageTag;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.feature.twitch.messagetag.Emotes;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

class IrcTwitchChatMessage extends TwitchChatMessage {

    IrcTwitchChatMessage(ChannelMessageEvent event) {
        super(instant(event), color(event), event.getMessage(), emotes(event));
    }

    private static Instant instant(ChannelMessageEvent event) {
        return event.getOriginalMessages().get(0)
                .getTag("sent-ts")
                .flatMap(MessageTag::getValue)
                .map(Long::parseLong)
                .map(Instant::ofEpochMilli)
                .orElseGet(Instant::now);
    }

    private static List<Emote> emotes(ChannelMessageEvent event) {
        return event.getOriginalMessages().get(0)
                .getTag("emotes", Emotes.class)
                .map(e -> e.getEmotes().stream()
                        .sorted(comparing(Emotes.Emote::getFirstIndex))
                        .map(emote -> {
                            String emoteSubstring = event.getMessage().substring(emote.getFirstIndex(), emote.getLastIndex() + 1);
                            return new Emote(emote.getId(), emote.getFirstIndex(), emoteSubstring);
                        })
                        .collect(Collectors.toList())
                )
                .orElseGet(Collections::emptyList);
    }

    private static String color(ChannelMessageEvent event) {
        return event.getOriginalMessages().get(0).getTag(org.kitteh.irc.client.library.feature.twitch.messagetag.Color.NAME, org.kitteh.irc.client.library.feature.twitch.messagetag.Color.class)
                .flatMap(org.kitteh.irc.client.library.feature.twitch.messagetag.Color::getValue)
                .orElse(null);
    }
}
