package com.linuxgods.kreiger.twitch.chat.filter;

import info.debatty.java.stringsimilarity.Levenshtein;

import java.util.function.BiPredicate;

public class LevenshteinDuplicateStringBiPredicate implements BiPredicate<String, String> {
    private final double duplicateSimilarityThreshold;

    private Levenshtein levenshtein = new Levenshtein();

    public LevenshteinDuplicateStringBiPredicate(double duplicateSimilarityThreshold) {
        this.duplicateSimilarityThreshold = duplicateSimilarityThreshold;
    }

    @Override
    public boolean test(String previous, String potentialDuplicate) {
        return isDuplicate(previous, potentialDuplicate);
    }

    private boolean isDuplicate(String previous, String potentialDuplicate) {
        int shortestStringLength = Math.min(previous.length(), potentialDuplicate.length());
        int longestStringLength = Math.max(previous.length(), potentialDuplicate.length());
        boolean longestIsMoreThanDoubleTheLengthOfTheShortest = longestStringLength > shortestStringLength * 2;
        if (longestIsMoreThanDoubleTheLengthOfTheShortest) {
            return false;
        }

        double distance = levenshtein.distance(previous.toLowerCase(), potentialDuplicate.toLowerCase());
        double similarity = (((double) longestStringLength) - distance) / (double)longestStringLength;

        boolean duplicate = similarity > duplicateSimilarityThreshold;
        return duplicate;
    }

}
