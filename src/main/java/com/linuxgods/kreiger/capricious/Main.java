package com.linuxgods.kreiger.capricious;

import com.linuxgods.kreiger.capricious.gui.ChatGui;
import com.linuxgods.kreiger.capricious.gui.ChatSelectionDialog;
import com.linuxgods.kreiger.capricious.twitch.api.TwitchApi;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.FewDuplicatesPredicate;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.LevenshteinDuplicateBiPredicate;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.RepetitionPredicate;
import com.linuxgods.kreiger.util.Configuration;
import com.linuxgods.kreiger.util.ConfigurationPropertiesFile;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.time.temporal.ChronoUnit.MINUTES;

public class Main extends Application {

    public static final String NAME = "Capricious";

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

    private final TwitchApi twitchApi = new TwitchApi("075ua3y1u8jb52lxf145zz5ctxkkz2");
    private final Predicate<TwitchChatMessage> messagePredicate = FEW_DUPLICATES_PREDICATE.and(REPETITION_PREDICATE);

    public static void main(String[] args) throws InterruptedException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStageIgnored) throws Exception {
        String chatSourceName = CONFIGURATION.getString(CHANNEL);

        while (true) {
            Optional<TwitchChatSource> twitchChatSource = askForTwitchChannel(chatSourceName);
            if (!twitchChatSource.isPresent()) {
                System.exit(0);
            }
            chatSourceName = connect(twitchChatSource.get());
        }
    }

    private String connect(TwitchChatSource twitchChatSource) {
        String channelName = twitchChatSource.getName();
        saveChannel(channelName);
        String logoUrl = twitchChatSource.getLogo().orElse(null);
        ChatGui chatGui = new ChatGui(channelName, logoUrl);

        connectToTwitchChannel(twitchChatSource, chatGui::append);
        chatGui.showAndWait();
        twitchChatSource.shutdown();
        return channelName;
    }

    private Optional<TwitchChatSource> askForTwitchChannel(String defaultChatName) {
        return new ChatSelectionDialog(twitchApi, defaultChatName).showAndWait();
    }

    private void saveChannel(String channel) {
        CONFIGURATION.setString(CHANNEL, channel);
        CONFIGURATION.save();
    }

    private TwitchChatSource connectToTwitchChannel(TwitchChatSource chatSource, Consumer<TwitchChatMessage> twitchChatMessageConsumer) {
        chatSource.consumeChatMessages(twitchChatMessage -> {
            if (messagePredicate.test(twitchChatMessage)) {
                twitchChatMessageConsumer.accept(twitchChatMessage);
            }
        });
        return chatSource;
    }

}
