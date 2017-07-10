package com.linuxgods.kreiger.twitch.chat.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER;

public class AutoScrollingPane extends ScrollPane {
    private BooleanProperty scrollPaused = new ReadOnlyBooleanWrapper();

    public AutoScrollingPane(Region region) {
        super(region);
        region.heightProperty().addListener((observable, oldValue, height) -> {
            scrollToBottomUnlessPaused();
        });

        setFitToWidth(true);
        setHbarPolicy(NEVER);
        setVbarPolicy(NEVER);

        pauseScrollOnSingleClick();
        pauseScrollWhenScrollingBack();
        unpauseScrollWhenAtBottom();
        hideScrollBarWhenPaused();

        scrollToBottomUnlessPaused();
    }

    private void hideScrollBarWhenPaused() {
        scrollPaused.addListener((observable, oldValue, paused) -> {
            setVbarPolicy(paused ? ALWAYS : NEVER);
        });
    }

    private void pauseScrollOnSingleClick() {
        setOnMouseClicked(new SingleOrDoubleClickMouseEventHandler() {
            public void singleClick(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    toggleScrollPaused();
                }
            }
        });
    }

    private void pauseScrollWhenScrollingBack() {
        addEventFilter(ScrollEvent.SCROLL, event -> {
            boolean scrollingBack = event.getDeltaY() > 0 || event.getTextDeltaY() > 0;
            if (scrollingBack) {
                setScrollPaused(true);
            }
        });
    }

    private void unpauseScrollWhenAtBottom() {
        vvalueProperty().addListener((observable, oldValue, vValue) -> {
            if (vValue.doubleValue() == getVmax()) {
                setScrollPaused(false);
            }
        });
    }

    private void scrollToBottomUnlessPaused() {
        scrollUnlessPausedTo(getVmax());
    }

    private void scrollUnlessPausedTo(double v) {
        if (!isScrollPaused()) {
            setVvalue(v);
        }
    }

    public void toggleScrollPaused() {
        setScrollPaused(!isScrollPaused());
    }

    public void setScrollPaused(boolean paused) {
        scrollPaused.set(paused);
    }

    public boolean isScrollPaused() {
        return scrollPaused.get();
    }

    public ObservableBooleanValue scrollPausedProperty() {
        return scrollPaused;
    }
}
