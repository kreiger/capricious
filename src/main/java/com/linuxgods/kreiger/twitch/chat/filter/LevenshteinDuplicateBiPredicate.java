package com.linuxgods.kreiger.twitch.chat.filter;

import com.linuxgods.kreiger.util.org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;
import java.util.function.BiPredicate;

public class LevenshteinDuplicateBiPredicate<T> implements BiPredicate<List<T>, List<T>> {
    private final double duplicateSimilarityThreshold;

    public LevenshteinDuplicateBiPredicate(double duplicateSimilarityThreshold) {
        this.duplicateSimilarityThreshold = duplicateSimilarityThreshold;
    }

    @Override
    public boolean test(List previous, List potentialDuplicate) {
        return isDuplicate(previous, potentialDuplicate);
    }

    private boolean isDuplicate(List previous, List potentialDuplicate) {
        int longestStringLength = Math.max(previous.size(), potentialDuplicate.size());
        int distanceThreshold = longestStringLength - (int)(duplicateSimilarityThreshold * longestStringLength);

        int distance = LevenshteinDistance.limitedCompare(previous, potentialDuplicate, distanceThreshold);

        return tooSimilar(distanceThreshold, distance);
    }

    private boolean tooSimilar(int distanceThreshold, int distance) {
        return -1 != distance && distance < distanceThreshold;
    }

}
