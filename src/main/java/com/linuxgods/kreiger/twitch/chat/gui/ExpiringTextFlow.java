package com.linuxgods.kreiger.twitch.chat.gui;

import com.linuxgods.kreiger.util.ExpiringQueue;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.text.TextFlow;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ExpiringTextFlow extends TextFlow {
    private final int KEEP_MINIMUM = 100;
    private final int KEEP_MAXIMUM = 10000;

    private boolean expiringEnabled;
    private final ExpiringQueue<List<Node>> expiringQueue = new ExpiringQueue<List<Node>>(Duration.of(10, ChronoUnit.SECONDS)) {
        @Override
        public void onRemovedExpired(List<Node> expiredNodes) {
            getChildren().removeAll(expiredNodes);
        }
    };

    public void append(List<Node> nodes) {
        Platform.runLater(() -> {
            removeExpired();
            expiringQueue.add(nodes);
            getChildren().addAll(nodes);
        });
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
