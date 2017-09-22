package com.linuxgods.kreiger.capricious.gui;

import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import com.linuxgods.kreiger.javafx.AutoScrollingPane;
import com.linuxgods.kreiger.javafx.ExpiringTextFlow;
import com.linuxgods.kreiger.javafx.MaximizedFullscreenStage;
import com.linuxgods.kreiger.javafx.SingleOrDoubleClickMouseEventHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import one.util.streamex.StreamEx;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javafx.scene.layout.BackgroundPosition.CENTER;
import static javafx.scene.layout.BackgroundRepeat.NO_REPEAT;

public class ChatGui {
    private static final int FONT_SIZE = 32;
    private final TwitchChatSource twitchChatSource;

    @FXML
    private MaximizedFullscreenStage maximizedFullscreenStage;

    @FXML
    private ExpiringTextFlow textFlow;

    @FXML
    private AutoScrollingPane autoScrollingPane;

    public ChatGui(TwitchChatSource twitchChatSource) {
        this.twitchChatSource = twitchChatSource;
        FXml.init(this);
        maximizedFullscreenStage.setTitle(twitchChatSource.getName());
        twitchChatSource.getLogo().ifPresent(s -> maximizedFullscreenStage.getIcons().add(new Image(s)));

        createContextMenu(twitchChatSource);
        setTitleAndBackgroundAndStopExpiringTextsWhenScrollingIsPaused();
        fullScreenOnDoubleClick();
    }

    private void createContextMenu(TwitchChatSource twitchChatSource) {
        autoScrollingPane.setContextMenu(new ContextMenu(createOpenInBrowserMenuItem(twitchChatSource)));
    }

    private MenuItem createOpenInBrowserMenuItem(TwitchChatSource twitchChatSource) {
        MenuItem openInBrowser = new MenuItem("Open in browser");
        openInBrowser.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(twitchChatSource.getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return openInBrowser;
    }


    private void setTitleAndBackgroundAndStopExpiringTextsWhenScrollingIsPaused() {
        BackgroundSize backgroundSize = new BackgroundSize(BackgroundSize.AUTO, 0.4, true, true, false, false);
        Background pauseBackground = new Background(new BackgroundImage(new Image(getClass().getResourceAsStream("pause.png")), NO_REPEAT, NO_REPEAT, CENTER, backgroundSize));
        autoScrollingPane.scrollingPausedProperty().addListener((observable, oldValue, scrollPaused) -> {
            Platform.runLater(() -> maximizedFullscreenStage.setTitle(scrollPaused ? twitchChatSource.getName() + " (Paused)" : twitchChatSource.getName()));
            textFlow.setExpiringEnabled(!scrollPaused);
            autoScrollingPane.getViewPort().setBackground(scrollPaused ? pauseBackground : null);
        });
    }

    private void fullScreenOnDoubleClick() {
        SingleOrDoubleClickMouseEventHandler
                .on(maximizedFullscreenStage.getScene())
                .setOnDoubleClick(event -> maximizedFullscreenStage.toggleMaximized());
    }

    public void append(TwitchChatMessage message) {
        List<Node> chatMessageTextsAndImages = createChatMessageTextsAndImages(message);
        Platform.runLater(() -> textFlow.append(chatMessageTextsAndImages));
    }

    private List<Node> createChatMessageTextsAndImages(TwitchChatMessage twitchChatMessage) {
        return StreamEx.of(twitchChatMessage.stream())
                .append(new TwitchChatMessage.Text(twitchChatMessage.toString().length(), "\n"))
                .map(part -> part.accept(new NodeFactory(twitchChatMessage)))
                .collect(toList());
    }

    public void showAndWait() {
        maximizedFullscreenStage.showAndWait();
    }

    private static class NodeFactory implements TwitchChatMessage.Visitor<Node> {
        private final TwitchChatMessage twitchChatMessage;

        NodeFactory(TwitchChatMessage twitchChatMessage) {
            this.twitchChatMessage = twitchChatMessage;
        }

        @Override
        public Node visitText(TwitchChatMessage.Text message) {
            return createText(message);
        }

        private Node createText(TwitchChatMessage.Text message) {
            Text text = new Text(message.toString());
            text.setFont(new Font(FONT_SIZE));
            twitchChatMessage.getColor().map(Color::web).ifPresent(text::setFill);
            return text;
        }

        @Override
        public Node visitEmote(TwitchChatMessage.Emote emote) {
            return createImage(emote.getUrl());
        }

        private Node createImage(String url) {
            ImageView imageView = new ImageView(url);
            imageView.setCache(true);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(FONT_SIZE);
            return imageView;
        }
    }
}
