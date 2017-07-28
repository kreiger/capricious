package com.linuxgods.kreiger.capricious.twitch.chat.gui;

import com.linuxgods.kreiger.util.ExpiringQueue;
import javafx.scene.Node;
import javafx.scene.text.TextFlow;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ExpiringTextFlow extends TextFlow {
    private final int KEEP_MINIMUM = 100;
    private final int KEEP_MAXIMUM = 10000;
    private final Duration expiration = Duration.of(1, ChronoUnit.MINUTES);
    private boolean expiringEnabled = true;

    private final ExpiringQueue<List<Node>> expiringQueue = new ExpiringQueue<List<Node>>(expiration) {
        @Override
        public void onRemovedExpired(List<Node> expiredNodes) {
            getChildren().removeAll(expiredNodes);
        }
    };

    public void append(List<Node> nodes) {
        removeExpired();
        expiringQueue.add(nodes);
        getChildren().addAll(nodes);
    }

    private void removeExpired() {
        if (!expiringEnabled && expiringQueue.size() < KEEP_MAXIMUM) {
            return;
        }
        if (expiringQueue.size() < KEEP_MINIMUM) {
            return;
        }
        expiringQueue.removeExpired();
    }

    public void setExpiringEnabled(boolean expiringEnabled) {
        this.expiringEnabled = expiringEnabled;
    }

}
