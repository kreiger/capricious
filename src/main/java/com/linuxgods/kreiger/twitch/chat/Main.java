package com.linuxgods.kreiger.twitch.chat;

import com.linuxgods.kreiger.twitch.chat.filter.FewDuplicatesMessagePredicate;
import com.linuxgods.kreiger.twitch.chat.filter.LevenshteinDuplicateStringBiPredicate;
import com.linuxgods.kreiger.twitch.chat.gui.TwitchChatGui;
import com.linuxgods.kreiger.twitch.chat.io.SimpleStdErrAndFileLogger;
import com.linuxgods.kreiger.twitch.chat.irc.TwitchChatClient;
import com.linuxgods.kreiger.twitch.web.TwitchWebScraper;
import com.linuxgods.kreiger.util.Configuration;
import com.linuxgods.kreiger.util.ConfigurationPropertiesFile;
import javafx.application.Application;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.time.temporal.ChronoUnit.MINUTES;

public class Main extends Application {

    public static final String NAME = "TwitchChatFilterViewer";
    private static final Configuration CONFIGURATION = ConfigurationPropertiesFile.of(NAME);
    private static final Configuration.Key<String> CHANNEL = Configuration.Key.of("channel", "GamesDoneQuick");

    private static final BiPredicate<String, String> DUPLICATE_STRING = new LevenshteinDuplicateStringBiPredicate(0.75);
    private static final Duration DUPLICATE_EXPIRATION = Duration.of(1, MINUTES);
    private static final int DUPLICATE_ACCEPTANCE_RATE = 10;

    private Predicate<String> acceptableMessagePredicate = new FewDuplicatesMessagePredicate(DUPLICATE_ACCEPTANCE_RATE, DUPLICATE_EXPIRATION, DUPLICATE_STRING);

    public static void main(String[] args) throws InterruptedException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStageIgnored) throws Exception {
        String previousChannel = CONFIGURATION.getString(CHANNEL);

        askForTwitchChannel(previousChannel).ifPresent(channel -> {
            setAndSaveChannel(channel);
            TwitchChatGui twitchChatGui = new TwitchChatGui(channel, new TwitchWebScraper());

            connectToTwitchChannel(channel, twitchChatGui::append);
        });
    }

    private void setAndSaveChannel(String channel) {
        CONFIGURATION.setString(CHANNEL, channel);
        CONFIGURATION.save();
    }


    private Optional<String> askForTwitchChannel(String defaultTwitchChannel) {
        TextInputDialog textInputDialog = new TextInputDialog(defaultTwitchChannel);
        textInputDialog.setTitle("Twitch Channel");
        textInputDialog.setHeaderText("Select a Twitch Channel");
        textInputDialog.setContentText("Select a Twitch Channel");
        return textInputDialog.showAndWait();
    }

    private void connectToTwitchChannel(String channel, Consumer<TwitchChatMessage> twitchChatMessageConsumer) {
        SimpleStdErrAndFileLogger log = new SimpleStdErrAndFileLogger(channel);
        TwitchChatSource chatSource = new TwitchChatClient(log, channel);

        chatSource.consumeChatMessages(twitchChatMessage -> {
            log.setLogToStdErr(false);
            if (acceptableMessagePredicate.test(twitchChatMessage.getMessage())) {
                twitchChatMessageConsumer.accept(twitchChatMessage);
            }
        });
    }

}
