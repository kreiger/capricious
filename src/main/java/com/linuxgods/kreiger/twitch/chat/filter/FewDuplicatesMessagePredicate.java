package com.linuxgods.kreiger.twitch.chat.filter;

import com.linuxgods.kreiger.util.ExpiringQueue;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class FewDuplicatesMessagePredicate implements Predicate<String> {
    private final int duplicateAcceptanceRate;

    private final ExpiringQueue<PreviousMessage> previousMessages;
    private final BiPredicate<String, String> duplicateString;

    public FewDuplicatesMessagePredicate(int duplicateAcceptanceRate, Duration duplicateExpiration, BiPredicate<String, String> duplicateString) {
        this.duplicateString = duplicateString;
        previousMessages = new ExpiringQueue<>(duplicateExpiration);
        this.duplicateAcceptanceRate = duplicateAcceptanceRate;
    }

    @Override
    public boolean test(String potentialDuplicate) {
        previousMessages.removeExpired();
        Optional<PreviousMessage> duplicate = findPreviousMessageDuplicate(potentialDuplicate);
        previousMessages.add(new PreviousMessage(potentialDuplicate));
        return !duplicate
                .map(previousMessage -> ignoreDuplicate(previousMessage, potentialDuplicate))
                .orElse(false);
    }

    private Optional<PreviousMessage> findPreviousMessageDuplicate(String potentialDuplicate) {
        return previousMessages.stream()
                .filter(previousMessage -> duplicateString.test(previousMessage.toString(), potentialDuplicate))
                .findFirst();
    }

    private boolean ignoreDuplicate(PreviousMessage previousMessage, String potentialDuplicate) {
        int duplicateCount = previousMessage.incrementDuplicateCount();
        boolean ignoreDuplicate = duplicateCount % duplicateAcceptanceRate != 0;
        String duplicateLogMessagePrefix = ignoreDuplicate ? "Ignoring" : "Accepting";
        System.err.println(duplicateLogMessagePrefix + (" duplicate " + duplicateCount + ": " + previousMessage + " | " + potentialDuplicate));
        return ignoreDuplicate;
    }

    private static class PreviousMessage {
        private String message;
        private int duplicateCount;

        PreviousMessage(String message) {
            this.message = message;
        }

        int incrementDuplicateCount() {
            return ++duplicateCount;
        }

        public String toString() {
            return message;
        }
    }
}

