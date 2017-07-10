package com.linuxgods.kreiger.twitch.web;

import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class TwitchWebScraper {

    private final Function<String, String> TWITCH_CHANNEL_URL = channel -> "https://www.twitch.tv/" + channel;
    private final Pattern IMAGE_META_TAG_PATTERN = Pattern.compile("<meta content=['\"](.*?)['\"] property=['\"]og:image['\"]>");

    public Optional<Image> getChannelImage(String channel) {
        return getChannelPageLines(channel)
                .map(IMAGE_META_TAG_PATTERN::matcher)
                .filter(Matcher::find)
                .map(m -> m.group(1))
                .map(Image::new)
                .findFirst();
    }

    private Stream<String> getChannelPageLines(String channel) {
        try {
            URL channelPageUrl = new URL(TWITCH_CHANNEL_URL.apply(channel));
            URLConnection urlConnection = channelPageUrl.openConnection();
            return new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.ISO_8859_1))
                    .lines();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
