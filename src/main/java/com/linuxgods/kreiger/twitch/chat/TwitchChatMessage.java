package com.linuxgods.kreiger.twitch.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class TwitchChatMessage {
    private final Instant instant;
    private final String color;
    private final String message;
    private final List<Part> parts;
    private final List<Integer> ints;

    public TwitchChatMessage(Instant instant, String color, String message, List<Emote> emotes) {
        this.instant = instant;
        this.color = color;
        this.message = message;
        this.parts = createParts(message, emotes);
        ints = createInts();
    }

    public Instant getInstant() {
        return instant;
    }

    public Optional<String> getColor() {
        return Optional.ofNullable(color);
    }

    public int length() {
        return message.length();
    }

    public String toString() {
        return message;
    }

    public Stream<Part> stream() {
        return parts.stream();
    }

    public List<Integer> toInts() {
        return ints;
    }

    private List<Part> createParts(String message, List<Emote> emotes) {
        List<Part> parts = new ArrayList<>();
        int i = 0;
        for (Emote emote : emotes) {
            String prefix = message.substring(i, emote.getStartIndex());
            if (!prefix.isEmpty()) {
                parts.add(new Text(i, prefix));
            }
            parts.add(emote);
            i = emote.getEndIndex();
        }
        if (i < message.length()) {
            parts.add(new Text(i, message.substring(i, message.length())));
        }
        return unmodifiableList(parts);
    }

    private List<Integer> createInts() {
        TwitchChatMessage.Visitor<IntStream> messageToInts = new TwitchChatMessage.Visitor<IntStream>() {
            public IntStream visitText(TwitchChatMessage.Text text) {
                return text.toString()
                        .codePoints()
                        .filter(c -> c != ' ')
                        .map(Character::toLowerCase);
            }
            public IntStream visitEmote(TwitchChatMessage.Emote emote) {
                return IntStream.of(-emote.getId());
            }
        };
        return stream()
                .flatMapToInt(part -> part.accept(messageToInts))
                .boxed()
                .collect(toList());
    }

    public interface Visitor<T> {
        T visitText(Text text);
        T visitEmote(Emote emote);
    }

    public interface Part {
        int getStartIndex();
        int length();
        default int getEndIndex() {
            return getStartIndex()+length();
        }
        <T> T accept(Visitor<T> visitor);
    }

    public static class Text implements Part {
        private final int startIndex;
        private final String text;

        public Text(int startIndex, String text) {
            this.startIndex = startIndex;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public int getStartIndex() {
            return startIndex;
        }

        @Override
        public int length() {
            return text.length();
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitText(this);
        }
    }

    public static class Emote implements Part {
        private final static String EMOTE_URL_TEMPLATE = "http://static-cdn.jtvnw.net/emoticons/v1/%s/1.0";
        private final int id;
        private final int startIndex;
        private final String substring;

        public Emote(int id, int startIndex, String substring) {
            this.id = id;
            this.startIndex = startIndex;
            this.substring = substring;
        }

        public int getId() {
            return id;
        }

        @Override
        public int getStartIndex() {
            return startIndex;
        }

        public String getUrl() {
            return String.format(EMOTE_URL_TEMPLATE, getId());
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitEmote(this);
        }

        @Override
        public String toString() {
            return substring;
        }

        @Override
        public int length() {
            return substring.length();
        }
    }
}
