package com.linuxgods.kreiger.capricious.gui;

import com.linuxgods.kreiger.capricious.twitch.api.Channel;
import com.linuxgods.kreiger.capricious.twitch.api.Stream;
import com.linuxgods.kreiger.capricious.twitch.api.TwitchApi;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class ChatSelectionDialog {
    private static final String TITLE = "Select a Twitch channel";
    private static final int GAP_PIXELS = 10;
    private static final int MEDIUM_PREVIEW_IMAGE_WIDTH = 320;
    private final Stage stage = new Stage();
    private Channel result;

    public ChatSelectionDialog(TwitchApi twitchApi, String defaultChannelName) {
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
                    button.setOnAction(action -> {
                        result = channel;
                        stage.close();
                    });
                    return button;
                })
                .toArray(Button[]::new);
        if (buttons.length == 0) {
            return;
        }
        stage.setTitle(TITLE);
        stage.setResizable(true);
        FlowPane flowPane = new FlowPane(buttons);
        flowPane.setHgap(GAP_PIXELS);
        flowPane.setVgap(GAP_PIXELS);
        ScrollPane scrollPane = new ScrollPane(flowPane);

        scrollPane.setPrefViewportWidth(MEDIUM_PREVIEW_IMAGE_WIDTH * 3 + 2 * GAP_PIXELS);
        scrollPane.setFitToWidth(true);

        Label label = new Label(TITLE + ":");
        TextField textField = new TextField(defaultChannelName);
        Button connectButton = new Button("Connect");
        connectButton.setDefaultButton(true);
        connectButton.setOnAction(action -> {
            String channelName = textField.getText();
            result = liveStreams.stream()
                    .map(Stream::getChannel)
                    .filter(channel -> channel.getName().equalsIgnoreCase(channelName))
                    .findFirst()
                    .orElseGet(() -> twitchApi.getChannelByName(channelName)
                            .orElseThrow(() -> new RuntimeException("No channel by name " + channelName)));
            stage.close();
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

    public Optional<Channel> showAndWait() {
        stage.showAndWait();
        return Optional.ofNullable(result);
    }
}
