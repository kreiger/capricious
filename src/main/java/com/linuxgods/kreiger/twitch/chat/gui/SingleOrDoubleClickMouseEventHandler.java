package com.linuxgods.kreiger.twitch.chat.gui;

import io.netty.util.concurrent.DefaultThreadFactory;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class SingleOrDoubleClickMouseEventHandler implements EventHandler<MouseEvent> {
    private final static int MULTI_CLICK_INTERVAL_MILLIS = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
    private final static ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory(SingleOrDoubleClickMouseEventHandler.class.getName(), true));
    private Future<?> scheduledSingleClick;

    @Override
    public synchronized void handle(MouseEvent event) {
        switch (event.getClickCount()) {
            case 1:
                scheduleSingleClick(event);
                return;
            case 2:
                if (cancelSingleClick()) doubleClick(event);
                break;
        }
    }

    private void scheduleSingleClick(MouseEvent event) {
        scheduledSingleClick = timer.schedule(() -> {
            scheduledSingleClick = null;
            singleClick(event);
        }, MULTI_CLICK_INTERVAL_MILLIS, MILLISECONDS);
    }

    private boolean cancelSingleClick() {
        Future<?> scheduledSingleClick = this.scheduledSingleClick;
        this.scheduledSingleClick = null;
        return scheduledSingleClick != null && scheduledSingleClick.cancel(false);
    }

    public void singleClick(MouseEvent event) {
    }

    public void doubleClick(MouseEvent event) {
    }
}
