package com.linuxgods.kreiger.capricious.gui;

import com.linuxgods.kreiger.capricious.twitch.api.Channel;
import com.linuxgods.kreiger.capricious.twitch.api.Stream;
import com.linuxgods.kreiger.capricious.twitch.api.TwitchApi;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import com.linuxgods.kreiger.capricious.twitch.chat.irc.TwitchChatClient;
import com.linuxgods.kreiger.capricious.twitch.chat.vod.TwitchVodChat;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatSelectionDialog {
    private static final String TITLE = "Select a Twitch channel";
    private static final Pattern VIDEO_PATTERN = Pattern.compile("/videos/(\\d+)");

    private static final int GAP_PIXELS = 10;
    private static final int MEDIUM_PREVIEW_IMAGE_WIDTH = 320;
    private final Stage stage = new Stage();
    private final TwitchApi twitchApi;
    private TwitchChatSource result;

    public ChatSelectionDialog(TwitchApi twitchApi, String defaultChannelName) {
        this.twitchApi = twitchApi;
        List<Stream> liveStreams = twitchApi.getLiveStreams();
        Button[] buttons = liveStreams.stream()
                .map(liveStream -> {
                    ImageView imageView = new ImageView(liveStream.getPreview().getMedium());
                    Channel channel = liveStream.getChannel();
                    String labelText = channel.getName() + "\n"
                            + channel.getStatus() + "\n"
                            + liveStream.getViewers() + " watching " + channel.getDisplayName() + "\n"
                            + " playing " + liveStream.getGame();
                    Button button = new Button(labelText, imageView);
                    button.setPadding(Insets.EMPTY);
                    button.setMaxWidth(MEDIUM_PREVIEW_IMAGE_WIDTH);
                    button.setContentDisplay(ContentDisplay.TOP);
                    button.setTextAlignment(TextAlignment.CENTER);
                    button.setOnAction(action -> setResultAndClose(new TwitchChatClient(channel)));
                    return button;
                })
                .toArray(Button[]::new);
        if (buttons.length == 0) {
            return;
        }
        stage.setTitle(TITLE);
        stage.setResizable(true);

        TilePane tilePane = new TilePane(buttons);
        tilePane.setHgap(GAP_PIXELS);
        tilePane.setVgap(GAP_PIXELS);
        tilePane.setPrefColumns(3);

        ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(true);

        Label label = new Label(TITLE + ":");
        TextField textField = new TextField(defaultChannelName);
        Button connectButton = new Button("Connect");
        connectButton.setDefaultButton(true);
        connectButton.setOnAction(action -> {
            String chatSourceName = textField.getText();

            setResultAndClose(getTwitchVodChat(chatSourceName)
                    .orElseGet(() -> getTwitchChatClient(liveStreams, chatSourceName)));
        });
        GridPane.setHgrow(textField, Priority.ALWAYS);
        GridPane.setFillWidth(textField, true);
        GridPane.setColumnSpan(scrollPane, GridPane.REMAINING);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));
        gridPane.add(label, 0, 0);
        gridPane.add(textField, 1, 0);
        gridPane.add(connectButton, 2, 0);
        gridPane.add(scrollPane, 0, 1);
        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
    }

    private void setResultAndClose(TwitchChatSource result) {
        this.result = result;
        stage.close();
    }

    private TwitchChatSource getTwitchChatClient(List<Stream> liveStreams, String chatSourceName) {
        return liveStreams.stream()
                .map(Stream::getChannel)
                .filter(channelName -> channelName.getName().equalsIgnoreCase(chatSourceName))
                .map(TwitchChatClient::new)
                .findFirst()
                .orElseGet(() -> this.twitchApi.getChannelByName(chatSourceName)
                        .map(TwitchChatClient::new)
                        .orElseThrow(() -> new RuntimeException("No channel by name " + chatSourceName)));
    }

    public Optional<TwitchChatSource> showAndWait() {
        stage.showAndWait();
        return Optional.ofNullable(result);
    }

    private Optional<TwitchChatSource> getTwitchVodChat(String videoUrl) {
        try {
            URL url = new URL(videoUrl);
            if (url.getHost().toLowerCase().endsWith(".twitch.tv")) {
                String urlPath = url.getPath();
                Matcher videoMatcher = VIDEO_PATTERN.matcher(urlPath);
                if (videoMatcher.matches()) {
                    return Optional.of(new TwitchVodChat(videoMatcher.group(1)));
                }
            }
        } catch (MalformedURLException ignored) {
        }
        return Optional.empty();
    }

}
