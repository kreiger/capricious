package com.linuxgods.kreiger.capricious.twitch.chat.filter;

import com.linuxgods.kreiger.capricious.twitch.chat.TwitchChatMessage;
import com.linuxgods.kreiger.util.SuffixArray;

import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;

public class NonRepetitivePredicate implements Predicate<TwitchChatMessage> {

    private final int minRepeatedSubstringLength;
    private final double repetitionLimit;
    private final int minCheckedLength;

    public NonRepetitivePredicate(int minCheckedLength, double repetitionLimit, int minRepeatedSubstringLength) {
        this.minCheckedLength = minCheckedLength;
        this.repetitionLimit = repetitionLimit;
        this.minRepeatedSubstringLength = minRepeatedSubstringLength;
    }

    @Override
    public boolean test(TwitchChatMessage input) {
        List<Integer> messageInts = input.toInts();
        if (messageInts.size() < minCheckedLength) {
            return true;
        }
        double repetition = getRepetition(messageInts);
        if (repetition < repetitionLimit) {
            return true;
        }
        System.err.println("Ignoring repeated " + ((int) (repetition * 100)) + "%: " + input);
        return false;
    }

    private double getRepetition(List<Integer> messageInts) {
        BitSet duplicatedCharacters = new SuffixArray<>(messageInts)
                .getCommonPrefixes().stream()
                .filter(prefix -> prefix.size() >= minRepeatedSubstringLength)
                .collect(() -> new BitSet(messageInts.size()), (bitSet, prefix) -> {
                    SuffixArray<Integer>.Suffix suffix = prefix.getSuffixes().get(0);
                    int from = suffix.getIndex();
                    int to = from + prefix.size();
                    bitSet.set(from, to);
                }, BitSet::or);

        return ((double) duplicatedCharacters.cardinality()) / messageInts.size();
    }

}
