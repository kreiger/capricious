package com.linuxgods.kreiger.capricious;

import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.FewDuplicatesPredicate;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.LevenshteinDuplicateBiPredicate;
import com.linuxgods.kreiger.capricious.twitch.chat.filter.NonRepetitivePredicate;

import java.time.Duration;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.time.temporal.ChronoUnit.MINUTES;

public class MessagePredicateFactory implements Supplier<Predicate<TwitchChatMessage>> {
    private static final Duration DUPLICATE_EXPIRATION = Duration.of(1, MINUTES);
    private static final int DUPLICATE_ACCEPTANCE_RATE = 10;

    private static final int REPETITION_MIN_CHECKED_LENGTH = 16;
    private static final double REPETITION_LIMIT = 0.4;
    private static final int MIN_REPEATED_SUBSTRING_LENGTH = 3;
    @Override
    public Predicate<TwitchChatMessage> get() {
        final BiPredicate<List<Integer>, List<Integer>> duplicateMessagePredicate = new LevenshteinDuplicateBiPredicate<>(0.75);
        final Predicate<TwitchChatMessage> fewDuplicatesPredicate = new FewDuplicatesPredicate(DUPLICATE_ACCEPTANCE_RATE, DUPLICATE_EXPIRATION, duplicateMessagePredicate);
        final Predicate<TwitchChatMessage> nonRepetitivePredicate = new NonRepetitivePredicate(REPETITION_MIN_CHECKED_LENGTH, REPETITION_LIMIT, MIN_REPEATED_SUBSTRING_LENGTH);

        return fewDuplicatesPredicate.and(nonRepetitivePredicate);
    }
}
