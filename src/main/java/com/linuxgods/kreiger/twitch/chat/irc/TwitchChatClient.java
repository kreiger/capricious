package com.linuxgods.kreiger.twitch.chat.irc;

import com.linuxgods.kreiger.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.twitch.chat.TwitchChatSource;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.twitch.TwitchListener;
import org.kitteh.irc.client.library.feature.twitch.messagetag.Emotes;

import javax.swing.text.BadLocationException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class TwitchChatClient implements TwitchChatSource {
    private static final String SERVER_NAME = "irc.chat.twitch.tv";
    private static final int SERVER_SECURE_PORT = 443;

    private final Consumer<String> log;
    private final String channel;

    public TwitchChatClient(Consumer<String> log, String channel) {
        this.log = log;
        this.channel = channel;
    }

    @Override
    public void consumeChatMessages(Consumer<TwitchChatMessage> consumer) {
        String nick = createAnonymousNickname();
        Client ircClient = Client.builder()
                .serverHost(SERVER_NAME)
                .serverPort(SERVER_SECURE_PORT)
                .nick(nick)
                .user(nick)
                .realName(":")
                .listenInput(line -> log(" > " + line))
                .listenOutput(line -> log(" < " + line))
                .afterBuildConsumer(client -> {
                    EventManager eventManager = client.getEventManager();
                    eventManager.registerEventListener(new TwitchListener(client));
                    eventManager.registerEventListener(new Object() {
                        @Handler
                        public void onChannelMessage(ChannelMessageEvent event) throws BadLocationException {
                            consumer.accept(new Message(event));
                        }
                    });
                })
                .build();
        ircClient.addChannel("#" + channel.toLowerCase());
    }

    private String createAnonymousNickname() {
        Random random = new Random();
        return "justinfan" + IntStream.range(0, 14)
                .map(i -> random.nextInt(10))
                .mapToObj(Integer::toString)
                .collect(joining());
    }

    private void log(String l) {
        log.accept(ZonedDateTime.now() + l);
    }

    public static class Message implements TwitchChatMessage {
        private static final String EMOTE_URL_TEMPLATE = "http://static-cdn.jtvnw.net/emoticons/v1/%s/1.0";

        private final Instant instant;
        private final List<Emotes.Emote> emotes;
        private final String color;
        private final String message;

        public Message(ChannelMessageEvent event) {
            this.instant = Instant.now();
            ServerMessage serverMessage = event.getOriginalMessages().get(0);
            emotes = serverMessage
                    .getTag("emotes", Emotes.class)
                    .map(e -> e.getEmotes().stream()
                            .sorted(Comparator.comparing(Emotes.Emote::getFirstIndex))
                            .collect(toList())
                    )
                    .orElseGet(Collections::emptyList);

            color = serverMessage.getTag(org.kitteh.irc.client.library.feature.twitch.messagetag.Color.NAME, org.kitteh.irc.client.library.feature.twitch.messagetag.Color.class)
                    .flatMap(org.kitteh.irc.client.library.feature.twitch.messagetag.Color::getValue)
                    .orElse(null);
            message = event.getMessage();
        }

        @Override
        public Instant getInstant() {
            return instant;
        }

        public Optional<String> getColor() {
            return Optional.ofNullable(color);
        }

        public String getMessage() {
            return message;
        }

        public <T> List<T> accept(TextOrEmoteVisitor<T> textOrEmoteVisitor) {
            List<T> nodes = new ArrayList<>();
            int i = 0;
            for (Emotes.Emote emote : emotes) {
                String prefix = message.substring(i, emote.getFirstIndex());
                if (!prefix.isEmpty()) {
                    nodes.add(textOrEmoteVisitor.visitText(prefix));
                }
                String imageUrl = String.format(EMOTE_URL_TEMPLATE, emote.getId());
                nodes.add(textOrEmoteVisitor.visitEmote(imageUrl));
                i = emote.getLastIndex() + 1;
            }
            nodes.add(textOrEmoteVisitor.visitText(message.substring(i, message.length()) + "\n"));
            return nodes;
        }

    }
}
