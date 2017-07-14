package com.linuxgods.kreiger.twitch.chat.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER;

public class AutoScrollingPane extends ScrollPane {
    private BooleanProperty scrollingPaused = new ReadOnlyBooleanWrapper();
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
        getContent().layoutBoundsProperty().addListener(contentLayboutBoundsInvalidated -> {
            layoutIsBeingUpdated = true;
        });
        vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (layoutIsBeingUpdated) {
                layoutIsBeingUpdated = false;
                scrollToBottomUnlessPaused();
            }
        });
        scrollToBottomUnlessPaused();
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
                .setOnSingleClick(event -> toggleScrollingPaused());
    }

    private void pauseScrollOnSpacePress() {
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                toggleScrollingPaused();
            }
        });
    }

    private void toggleScrollPausedWhenScrolling() {
        vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (!layoutIsBeingUpdated) {
                setScrollingPaused(newValue.doubleValue() < getVmax());
            }
        });
    }

    private void scrollToBottomUnlessPaused() {
        if (!getScrollingPaused()) {
            setVvalue(getVmax());
        }
    }

    public void toggleScrollingPaused() {
        setScrollingPaused(!getScrollingPaused());
        scrollToBottomUnlessPaused();
    }

    public void setScrollingPaused(boolean paused) {
        scrollingPaused.set(paused);
    }

    public boolean getScrollingPaused() {
        return scrollingPaused.get();
    }

    public ObservableBooleanValue scrollingPausedProperty() {
        return scrollingPaused;
    }

    public final StackPane getViewPort() { return (StackPane) getContent().getParent().getParent(); }

}
