package com.linuxgods.kreiger.capricious.twitch.chat.irc;

import com.linuxgods.kreiger.capricious.Main;
import com.linuxgods.kreiger.capricious.twitch.api.Channel;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import com.linuxgods.kreiger.capricious.twitch.chat.io.SimpleStdErrAndFileLogger;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import net.engio.mbassy.listener.Handler;
import net.harawata.appdirs.AppDirsFactory;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.feature.EventManager;
import org.kitteh.irc.client.library.feature.twitch.TwitchListener;

import javax.swing.text.BadLocationException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class TwitchChatClient implements TwitchChatSource {
    private static final String SERVER_NAME = "irc.chat.twitch.tv";
    private static final int SERVER_SECURE_PORT = 443;

    private final SimpleStdErrAndFileLogger log;
    private final Channel channel;

    public TwitchChatClient(Channel channel) {
        this.log = new SimpleStdErrAndFileLogger(getLogDirectory(), channel.getName());
        this.channel = channel;
    }

    @Override
    public Observable<TwitchChatMessage> getObservable() {
        PublishSubject<TwitchChatMessage> publishSubject = PublishSubject.create();
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
                            log.setLogToStdErr(false);
                            publishSubject.onNext(new IrcTwitchChatMessage(event));
                        }
                    });
                })
                .build();
        ircClient.addChannel("#" + getName().toLowerCase());
        return publishSubject
                .doOnDispose(ircClient::shutdown);
    }

    @Override
    public String getName() {
        return channel.getName();
    }

    @Override
    public Optional<String> getLogo() {
        return Optional.of(channel.getLogo());
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

    private static Path getLogDirectory() {
        return Paths.get(AppDirsFactory.getInstance().getUserLogDir(Main.NAME, null, null));
    }


}
