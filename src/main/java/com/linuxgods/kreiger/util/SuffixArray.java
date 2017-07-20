package com.linuxgods.kreiger.util;

import one.util.streamex.StreamEx;

import java.util.stream.IntStream;

import static java.lang.Math.min;

public class SuffixArray {

    private final String string;
    private final Suffix[] suffixes;

    public SuffixArray(String string) {
        if (string.isEmpty()) {
            throw new IllegalArgumentException("Empty string.");
        }
        this.string = string;
        this.suffixes = IntStream.range(0, string.length())
                .mapToObj(Suffix::new)
                .sorted()
                .toArray(Suffix[]::new);
    }

    public CommonPrefixes getCommonPrefixes() {
        if (string.length() < 2) {
            throw new IllegalArgumentException("String length < 2");
        }
        return new CommonPrefixes();
    }

    public class Suffix implements CharSequence, Comparable<Suffix> {
        private final int index;
        private final int length;

        private Suffix(int index) {
            this.index = index;
            this.length = string.length() - index;
        }

        @Override
        public int compareTo(Suffix that) {
            int maxLength = getCommonPrefixMaxLength(that);
            for (int i = 0; i < maxLength; i++) {
                int charComparison = Character.compare(this.charAt(i), that.charAt(i));
                if (charComparison != 0) {
                    return charComparison;
                }
            }
            return Integer.compare(this.length, that.length);
        }

        private int getCommonPrefixMaxLength(Suffix that) {
            return min(this.length, that.length);
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public char charAt(int index) {
            return string.charAt(this.index+index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return string.subSequence(index+start, index+end);
        }

        private Prefix getCommonPrefix(Suffix that) {
            int maxLength = getCommonPrefixMaxLength(that);
            for (int i = 0; i < maxLength; i++) {
                int charComparison = Character.compare(this.charAt(i), that.charAt(i));
                if (charComparison != 0) {
                    return new Prefix(i);
                }
            }
            return new Prefix(maxLength);
        }

        @Override
        public String toString() {
            return string.substring(index);
        }

        public class Prefix {
            private final int length;
            private Prefix(int length) {
                this.length = length;
            }

            public int length() {
                return length;
            }

            @Override
            public String toString() {
                return string.substring(index, index+length);
            }
        }
    }

    public class CommonPrefixes {

        public StreamEx<Suffix.Prefix> stream() {
            return StreamEx.of(suffixes)
                    .pairMap(Suffix::getCommonPrefix);
        }

        public int length() {
            return string.length() - 1;
        }

    }

}
