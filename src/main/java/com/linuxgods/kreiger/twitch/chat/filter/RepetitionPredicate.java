package com.linuxgods.kreiger.twitch.chat.filter;

import com.linuxgods.kreiger.util.SuffixArray;

import java.util.function.Predicate;

import static one.util.streamex.DoubleCollector.summarizing;

public class RepetitionPredicate implements Predicate<String> {

    private static final double REPETITION_LIMIT = 0.15;
    private final int minCheckedLength;

    public RepetitionPredicate(int minCheckedLength) {
        this.minCheckedLength = minCheckedLength;
    }

    @Override
    public boolean test(String input) {
        String trimmedInput = input.replaceAll("\\s+", "");
        if (trimmedInput.length() < minCheckedLength) {
            return true;
        }
        String lowerCaseTrimmedInput = trimmedInput.toLowerCase();

        int length = lowerCaseTrimmedInput.length();
        double repetition = getRepetition(lowerCaseTrimmedInput);
        if (repetition < REPETITION_LIMIT) {
            return true;
        }
        System.err.println("Ignoring repeated "+repetition+" of "+ length +": "+input);
        return false;
    }

    static double getRepetition(String input) {
        if (input.length() < 2) {
            return 0;
        }
        SuffixArray.CommonPrefixes commonPrefixes = new SuffixArray(input)
                .getCommonPrefixes();
        double repetition = commonPrefixes.stream()
                .mapToDouble(SuffixArray.Suffix.Prefix::length)
                .collect(summarizing())
                .getAverage();
        return repetition / commonPrefixes.length();
    }

}
