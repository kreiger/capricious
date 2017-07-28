package com.linuxgods.kreiger.capricious.twitch.chat.filter;

import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.util.ExpiringQueue;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FewDuplicatesPredicate implements Predicate<TwitchChatMessage> {
    private final int duplicateAcceptanceRate;

    private final ExpiringQueue<DuplicateCounted<TwitchChatMessage>> previousSeen;
    private final BiPredicate<List<Integer>, List<Integer>> duplicate;

    public FewDuplicatesPredicate(int duplicateAcceptanceRate, Duration duplicateExpiration, BiPredicate<List<Integer>, List<Integer>> duplicate) {
        this.duplicate = duplicate;
        previousSeen = new ExpiringQueue<>(duplicateExpiration);
        this.duplicateAcceptanceRate = duplicateAcceptanceRate;
    }

    @Override
    public boolean test(TwitchChatMessage potentialDuplicate) {
        previousSeen.removeExpired();
        Optional<DuplicateCounted<TwitchChatMessage>> duplicate = findPreviousDuplicated(potentialDuplicate);
        previousSeen.add(new DuplicateCounted<>(potentialDuplicate));
        return !duplicate
                .map(previousMessage -> ignoreDuplicate(previousMessage, potentialDuplicate))
                .orElse(false);
    }

    private Optional<DuplicateCounted<TwitchChatMessage>> findPreviousDuplicated(TwitchChatMessage potentialDuplicate) {
        return previousSeen.stream()
                .filter(previousDuplicated -> duplicate.test(previousDuplicated.get().toInts(), potentialDuplicate.toInts()))
                .findFirst();
    }

    private boolean ignoreDuplicate(DuplicateCounted<TwitchChatMessage> previousDuplicated, TwitchChatMessage potentialDuplicate) {
        int duplicateCount = previousDuplicated.incrementDuplicateCount();
        boolean ignoreDuplicate = duplicateCount % duplicateAcceptanceRate != 0;
        String duplicateLogMessagePrefix = ignoreDuplicate ? "Ignoring" : "Accepting";
        System.err.println(duplicateLogMessagePrefix + (" duplicate " + duplicateCount + ": " + previousDuplicated + " | " + potentialDuplicate));
        return ignoreDuplicate;
    }

    private static class DuplicateCounted<T> implements Supplier<T> {
        private T message;
        private int duplicateCount;

        DuplicateCounted(T message) {
            this.message = message;
        }

        int incrementDuplicateCount() {
            return ++duplicateCount;
        }

        public String toString() {
            return message.toString();
        }

        @Override
        public T get() {
            return message;
        }
    }
}

