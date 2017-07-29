package com.linuxgods.kreiger.capricious;

import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.FewDuplicatesPredicate;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.LevenshteinDuplicateBiPredicate;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.RepetitionPredicate;
import com.linuxgods.kreiger.capricious.twitch.chat.gui.TwitchChatGui;
import com.linuxgods.kreiger.capricious.twitch.chat.io.SimpleStdErrAndFileLogger;
import com.linuxgods.kreiger.capricious.twitch.chat.irc.TwitchChatClient;
import com.linuxgods.kreiger.capricious.twitch.web.TwitchWebScraper;
import com.linuxgods.kreiger.util.Configuration;
import com.linuxgods.kreiger.util.ConfigurationPropertiesFile;
import javafx.application.Application;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import net.harawata.appdirs.AppDirsFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.time.temporal.ChronoUnit.MINUTES;

public class Main extends Application {

    private static final String NAME = "Capricious";

    private static final Configuration CONFIGURATION = ConfigurationPropertiesFile.of(NAME);
    private static final Configuration.Key<String> CHANNEL = Configuration.Key.of("channel", "GamesDoneQuick");

    private static final Duration DUPLICATE_EXPIRATION = Duration.of(1, MINUTES);
    private static final int DUPLICATE_ACCEPTANCE_RATE = 10;
    private static final BiPredicate<List<Integer>, List<Integer>> DUPLICATE_MESSAGE = new LevenshteinDuplicateBiPredicate<>(0.75);
    private static final Predicate<TwitchChatMessage> FEW_DUPLICATES_PREDICATE = new FewDuplicatesPredicate(DUPLICATE_ACCEPTANCE_RATE, DUPLICATE_EXPIRATION, DUPLICATE_MESSAGE);

    private static final int REPETITION_MIN_CHECKED_LENGTH = 16;
    private static final double REPETITION_LIMIT = 0.4;
    private static final int MIN_REPEATED_SUBSTRING_LENGTH = 3;
    private static final Predicate<TwitchChatMessage> REPETITION_PREDICATE = new RepetitionPredicate(REPETITION_MIN_CHECKED_LENGTH, REPETITION_LIMIT, MIN_REPEATED_SUBSTRING_LENGTH);

    private final Predicate<TwitchChatMessage> messagePredicate = FEW_DUPLICATES_PREDICATE.and(REPETITION_PREDICATE);

    public static void main(String[] args) throws InterruptedException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStageIgnored) throws Exception {
        String previousChannel = CONFIGURATION.getString(CHANNEL);

        askForTwitchChannel(previousChannel).ifPresent(channel -> {
            saveChannel(channel);
            TwitchChatGui twitchChatGui = new TwitchChatGui(channel, new TwitchWebScraper());

            connectToTwitchChannel(channel, twitchChatGui::append);
        });
    }

    private void saveChannel(String channel) {
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
        SimpleStdErrAndFileLogger log = new SimpleStdErrAndFileLogger(getLogDirectory(), channel);
        TwitchChatSource chatSource = new TwitchChatClient(log, channel);

        chatSource.consumeChatMessages(twitchChatMessage -> {
            log.setLogToStdErr(false);
            if (messagePredicate.test(twitchChatMessage)) {
                twitchChatMessageConsumer.accept(twitchChatMessage);
            }
        });
    }

    private static Path getLogDirectory() {
        return Paths.get(AppDirsFactory.getInstance().getUserLogDir(NAME, null, null));
    }

}
