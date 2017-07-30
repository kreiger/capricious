package com.linuxgods.kreiger.javafx;

import javafx.scene.input.KeyEvent;

import static javafx.scene.input.KeyCode.ENTER;

public class MaximizedFullscreenStage extends javafx.stage.Stage {

    public MaximizedFullscreenStage() {

        maximizedProperty().addListener((observable, oldValue, maximized) -> setFullScreen(maximized));
        fullScreenProperty().addListener((observable, oldValue, fullScreen) -> setMaximized(fullScreen));

        setEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == ENTER && event.isAltDown()) {
                toggleMaximized();
                event.consume();
            }
        });
    }

    public void toggleMaximized() {
        setMaximized(!isMaximized());
    }
}
