package com.linuxgods.kreiger.capricious.twitch.chat.vod;

import com.fasterxml.jackson.databind.JsonNode;
import com.linuxgods.kreiger.capricious.twitch.api.GetRechatMessages;
import com.linuxgods.kreiger.capricious.twitch.api.GetVideoInfo;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TwitchVodChat implements TwitchChatSource {
    private static final int MESSAGE_FETCH_INTERVAL = 30;
    private static final int POLL_INTERVAL = 250;
    private final String videoId;
    private long offsetSeconds = 0;
    private boolean stopChatWhenEmpty = false;

    public TwitchVodChat(String videoId) {
        this.videoId = videoId;
    }

    @Override
    public Observable<TwitchChatMessage> getObservable() {
        PublishSubject<TwitchChatMessage> publishSubject = PublishSubject.create();
        ConcurrentLinkedQueue<TwitchChatMessage> messages = new ConcurrentLinkedQueue<>();
        VideoInfo videoInfo = getVideoInfo();
        Instant start = Instant.now();

        messages.addAll(getMessages());

        Timer t = new Timer(POLL_INTERVAL, e -> {
            final Duration playTime = Duration.between(start, Instant.now());
            while (messages.peek() != null && videoInfo.getStartInstant().plus(playTime).isAfter(messages.peek().getInstant())) {
                publishSubject.onNext(messages.poll());
            }
            if (stopChatWhenEmpty & messages.isEmpty()) {
                ((Timer) e.getSource()).stop();
            } else if (playTime.getSeconds() > offsetSeconds + MESSAGE_FETCH_INTERVAL / 2) {
                offsetSeconds += MESSAGE_FETCH_INTERVAL;
                if (offsetSeconds > videoInfo.getLength()) {
                    stopChatWhenEmpty = true;
                } else {
                    scheduleMessageFetch(messages);
                }
            }
        });
        t.setRepeats(true);
        t.start();
        return publishSubject
                .doOnDispose(t::stop);
    }

    private VideoInfo getVideoInfo() {
        return VideoInfo.fromJson(new GetVideoInfo().call(videoId));
    }

    private LinkedList<TwitchChatMessage> getMessages() {
        LinkedList<TwitchChatMessage> list = new LinkedList<>();
        Optional<JsonNode> messages = new GetRechatMessages().call(videoId, offsetSeconds);
        messages.ifPresent(m ->
                m.forEach(c -> VodChatMessage.createFromJson(c).ifPresent(list::add)));
        return list;
    }

    private void scheduleMessageFetch(ConcurrentLinkedQueue<TwitchChatMessage> messages) {
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                messages.addAll(getMessages());
                return null;
            }
        }.execute();
    }

    @Override
    public String getName() {
        return "https://www.twitch.tv/videos/"+videoId;
    }

    @Override
    public Optional<String> getLogo() {
        return Optional.empty();
    }
}
