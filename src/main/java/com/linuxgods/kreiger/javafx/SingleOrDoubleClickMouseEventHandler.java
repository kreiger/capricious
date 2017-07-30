package com.linuxgods.kreiger.javafx;

import io.netty.util.concurrent.DefaultThreadFactory;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SingleOrDoubleClickMouseEventHandler {

    private final static int MULTI_CLICK_INTERVAL_MILLIS = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
    private final static ScheduledThreadPoolExecutor timer;
    static {
        timer = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(SingleOrDoubleClickMouseEventHandler.class.getName(), true));
        timer.setRemoveOnCancelPolicy(true);
    }

    private EventHandler<? super MouseEvent> onSingleClick = singleClick -> {};
    private EventHandler<? super MouseEvent> onDoubleClick = doubleClick -> {};
    private boolean dragging;
    private Future<?> scheduledSingleClick;

    public SingleOrDoubleClickMouseEventHandler() {
    }

    public SingleOrDoubleClickMouseEventHandler(BiConsumer<EventType, EventHandler> addEventHandler) {
        addTo(addEventHandler);
    }

    public void addTo(BiConsumer<EventType, EventHandler> addEventHandler) {
        addOnDragDetected(addEventHandler, event -> {
            dragging = true;
            cancelSingleClick();
        });
        addOnMouseClicked(addEventHandler, event -> {
            if (dragging) {
                dragging = false;
                return;
            }
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            int clickCount = event.getClickCount();
            if (clickCount == 1) {
                scheduleSingleClick(event);
            } else if (clickCount == 2) {
                if (cancelSingleClick()) onDoubleClick(event);
            }
        });
    }

    public void setOnSingleClick(EventHandler<? super MouseEvent> onSingleClick) {
        requireNonNull(onSingleClick);
        this.onSingleClick = singleClick -> {
            onSingleClick.handle(singleClick);
            singleClick.consume();
        };
    }

    public void setOnDoubleClick(EventHandler<? super MouseEvent> onDoubleClick) {
        requireNonNull(onDoubleClick);
        this.onDoubleClick = doubleClick -> {
            onDoubleClick.handle(doubleClick);
            doubleClick.consume();
        };
    }

    private void scheduleSingleClick(MouseEvent event) {
        scheduledSingleClick = timer.schedule(() -> {
            scheduledSingleClick = null;
            onSingleClick(event);
        }, MULTI_CLICK_INTERVAL_MILLIS, MILLISECONDS);
    }

    private boolean cancelSingleClick() {
        Future<?> scheduledSingleClick = this.scheduledSingleClick;
        this.scheduledSingleClick = null;
        return scheduledSingleClick != null && scheduledSingleClick.cancel(false);
    }

    protected void onSingleClick(MouseEvent singleClick) {
        onSingleClick.handle(singleClick);
    }

    protected void onDoubleClick(MouseEvent doubleClick) {
        onDoubleClick.handle(doubleClick);
    }


    public static SingleOrDoubleClickMouseEventHandler on(Node node) {
        return new SingleOrDoubleClickMouseEventHandler(node::addEventHandler);
    }

    public static SingleOrDoubleClickMouseEventHandler on(Scene scene) {
        return new SingleOrDoubleClickMouseEventHandler(scene::addEventHandler);
    }

    private void addOnDragDetected(BiConsumer<EventType, EventHandler> addEventHandler, EventHandler<? super MouseEvent> dragDetectedHandler) {
        addEventHandler.accept(MouseEvent.DRAG_DETECTED, dragDetectedHandler);
    }

    private void addOnMouseClicked(BiConsumer<EventType, EventHandler> addEventHandler, EventHandler<? super MouseEvent> mouseClickedHandler) {
        addEventHandler.accept(MouseEvent.MOUSE_CLICKED, mouseClickedHandler);
    }
}
