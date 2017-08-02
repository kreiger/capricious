package com.linuxgods.kreiger.capricious.gui;

import com.linuxgods.kreiger.capricious.twitch.api.Channel;
import com.linuxgods.kreiger.capricious.twitch.api.Stream;
import com.linuxgods.kreiger.capricious.twitch.api.TwitchApi;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import com.linuxgods.kreiger.capricious.twitch.chat.irc.TwitchChatClient;
import com.linuxgods.kreiger.capricious.twitch.chat.vod.TwitchVodChat;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
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
    private static final Pattern VIDEO_PATTERN = Pattern.compile("/videos/(\\d+)");

    private static final int MEDIUM_PREVIEW_IMAGE_WIDTH = 320;

    @FXML
    private Stage stage;
    @FXML
    private TilePane tilePane;
    @FXML
    private TextField textField;

    private final TwitchApi twitchApi;
    private final List<Stream> liveStreams;
    private TwitchChatSource result;


    public ChatSelectionDialog(TwitchApi twitchApi, String defaultChannelName) {
        this.twitchApi = twitchApi;
        liveStreams = twitchApi.getLiveStreams();
        if (liveStreams.isEmpty()) {
            return;
        }

        FXml.init(this);
        textField.setText(defaultChannelName);
        tilePane.getChildren().addAll(createLiveStreamsButtons());
    }

    private Button[] createLiveStreamsButtons() {
        return liveStreams.stream()
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
    }

    @FXML
    private void setResultFromTextField() {
        String chatSourceName = textField.getText();

        setResultAndClose(getTwitchVodChat(chatSourceName)
                .orElseGet(() -> getTwitchChatClient(liveStreams, chatSourceName)));
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
