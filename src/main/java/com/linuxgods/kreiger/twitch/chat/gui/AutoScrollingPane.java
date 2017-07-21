package com.linuxgods.kreiger.twitch.chat.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER;

public class AutoScrollingPane extends ScrollPane {
    private static final Duration SCROLL_DURATION = Duration.millis(1000);
    private static final double EPSILON = 0.001;
    private final BooleanProperty scrollingPaused = new ReadOnlyBooleanWrapper();
    private final Timeline scrollToBottomAnimation = new Timeline(new KeyFrame(SCROLL_DURATION, new KeyValue(vvalueProperty(), getVmax() - EPSILON)));
    private boolean layoutIsBeingUpdated;

    public AutoScrollingPane(Region region) {
        super(region);

        setFitToWidth(true);

        pauseScrollOnSingleClick();
        pauseScrollOnSpacePress();
        toggleScrollPausedWhenScrolling();
        showScrollBarWhenPaused();
        keepScrollingToBottomWhUpdatingLayout();
    }

    private void keepScrollingToBottomWhUpdatingLayout() {
        getContent().layoutBoundsProperty().addListener(contentLayoutBoundsInvalidated -> {
            layoutIsBeingUpdated = true;
        });
        vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (layoutIsBeingUpdated) {
                layoutIsBeingUpdated = false;
                scrollToBottomUnlessPaused();
            }
        });
        jumpToBottom();
    }

    private void showScrollBarWhenPaused() {
        setHbarPolicy(NEVER);
        setVbarPolicy(NEVER);

        scrollingPaused.addListener((observable, oldValue, paused) -> {
            setVbarPolicy(paused ? ALWAYS : NEVER);
        });
    }

    private void pauseScrollOnSingleClick() {
        SingleOrDoubleClickMouseEventHandler
                .on(this)
                .setOnSingleClick(event -> toggleScrollingPausedXorJumpToBottom());
    }

    private void pauseScrollOnSpacePress() {
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                toggleScrollingPausedXorJumpToBottom();
            }
        });
    }

    public void toggleScrollingPausedXorJumpToBottom() {
        toggleScrollingPaused();
        if (!isScrollingPaused()) {
            jumpToBottom();
        }
        scrollToBottomAnimation.pause();
    }

    private void jumpToBottom() {
        setVvalue(getVmax());
    }

    private void toggleScrollPausedWhenScrolling() {
        vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (layoutIsBeingUpdated) {
                return;
            }
            boolean scrollingUp = newValue.doubleValue() < oldValue.doubleValue() - EPSILON;
            if (scrollingUp) {
                setScrollingPaused(true);
                scrollToBottomAnimation.pause();
                return;
            }
            boolean atBottom = newValue.doubleValue() > getVmax() - EPSILON;
            if (atBottom) {
                setScrollingPaused(false);
                scrollToBottomAnimation.pause();
            }
        });
    }

    private void scrollToBottomUnlessPaused() {
        if (!isScrollingPaused()) {
            scrollToBottomAnimation.playFromStart();
        }
    }

    private void toggleScrollingPaused() {
        setScrollingPaused(!isScrollingPaused());
    }

    public void setScrollingPaused(boolean paused) {
        scrollingPaused.set(paused);
    }

    public boolean isScrollingPaused() {
        return scrollingPaused.get();
    }

    public ObservableBooleanValue scrollingPausedProperty() {
        return scrollingPaused;
    }

    public final StackPane getViewPort() {
        return (StackPane) getContent().getParent().getParent();
    }

}
