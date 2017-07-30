package com.linuxgods.kreiger.capricious.twitch.chat.irc;

import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.twitch.TwitchListener;

import javax.swing.text.BadLocationException;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class TwitchChatClient implements TwitchChatSource {
    private static final String SERVER_NAME = "irc.chat.twitch.tv";
    private static final int SERVER_SECURE_PORT = 443;

    private final Consumer<String> log;
    private final String channel;
    private Client ircClient;

    public TwitchChatClient(Consumer<String> log, String channel) {
        this.log = log;
        this.channel = channel;
    }

    @Override
    public void consumeChatMessages(Consumer<TwitchChatMessage> consumer) {
        String nick = createAnonymousNickname();
        ircClient = Client.builder()
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
                            consumer.accept(new IrcTwitchChatMessage(event));
                        }
                    });
                })
                .build();
        ircClient.addChannel("#" + channel.toLowerCase());
    }

    @Override
    public void shutdown() {
        ircClient.shutdown();
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

}
