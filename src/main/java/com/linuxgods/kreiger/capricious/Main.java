package com.linuxgods.kreiger.capricious;

import com.linuxgods.kreiger.capricious.gui.ChatGui;
import com.linuxgods.kreiger.capricious.gui.ChatSelectionDialog;
import com.linuxgods.kreiger.capricious.twitch.api.TwitchApi;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatSource;
import com.linuxgods.kreiger.util.Configuration;
import com.linuxgods.kreiger.util.ConfigurationPropertiesFile;
import io.reactivex.disposables.Disposable;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Main extends Application {

    public static final String NAME = "Capricious";

    private static final Configuration CONFIGURATION = ConfigurationPropertiesFile.of(NAME);
    private static final Configuration.Key<String> CHANNEL = Configuration.Key.of("channel", "GamesDoneQuick");

    private final TwitchApi twitchApi = new TwitchApi("075ua3y1u8jb52lxf145zz5ctxkkz2");
    private final MessagePredicateFactory messagePredicateFactory = new MessagePredicateFactory();

    public static void main(String[] args) {
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
        ChatGui chatGui = new ChatGui(twitchChatSource);

        Disposable disposable = connect(twitchChatSource, chatGui::append);
        chatGui.showAndWait();
        disposable.dispose();
        return channelName;
    }

    private Optional<TwitchChatSource> askForTwitchChannel(String defaultChatName) {
        return new ChatSelectionDialog(twitchApi, defaultChatName).showAndWait();
    }

    private void saveChannel(String channel) {
        CONFIGURATION.setString(CHANNEL, channel);
        CONFIGURATION.save();
    }

    private Disposable connect(TwitchChatSource chatSource, Consumer<TwitchChatMessage> twitchChatMessageConsumer) {
        Predicate<TwitchChatMessage> messagePredicate = messagePredicateFactory.get();

        return chatSource.getObservable()
                .filter(messagePredicate::test)
                .subscribe(twitchChatMessageConsumer::accept);
    }

}
