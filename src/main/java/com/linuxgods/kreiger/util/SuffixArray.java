package com.linuxgods.kreiger.util;

import one.util.streamex.StreamEx;

import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public class SuffixArray<T extends Comparable> {

    private final List<? extends T> elements;
    private final List<Suffix> suffixes;

    public SuffixArray(List<? extends T> elements) {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Empty list.");
        }
        this.elements = elements;
        this.suffixes = IntStream.range(0, elements.size())
                .mapToObj(Suffix::new)
                .sorted()
                .collect(toList());
    }

    public List<Suffix> getSuffixes() {
        return unmodifiableList(suffixes);
    }

    public CommonPrefixes getCommonPrefixes() {
        if (elements.size() < 2) {
            throw new IllegalArgumentException("size < 2");
        }
        return new CommonPrefixes();
    }

    public class Suffix implements Comparable<Suffix> {
        private final int index;
        private final int size;

        private Suffix(int index) {
            this.index = index;
            this.size = elements.size() - index;
        }

        @Override
        public int compareTo(Suffix that) {
            int maxLength = getCommonPrefixMaxLength(that);
            for (int i = 0; i < maxLength; i++) {
                int comparison = this.get(i).compareTo(that.get(i));
                if (comparison != 0) {
                    return comparison;
                }
            }
            return Integer.compare(this.size, that.size);
        }

        private int getCommonPrefixMaxLength(Suffix that) {
            return min(this.size, that.size);
        }

        public int size() {
            return size;
        }

        public T get(int index) {
            return elements.get(this.index+index);
        }

        private CommonPrefix getCommonPrefix(Suffix that) {
            int maxLength = getCommonPrefixMaxLength(that);
            for (int i = 0; i < maxLength; i++) {
                int comparison = this.get(i).compareTo(that.get(i));
                if (comparison != 0) {
                    return new CommonPrefix(i, this, that);
                }
            }
            return new CommonPrefix(maxLength, this, that);
        }

        @Override
        public String toString() {
            return elements.subList(index, elements.size()).toString();
        }

        public int getIndex() {
            return index;
        }

        public class CommonPrefix {
            private final int size;
            private final List<Suffix> suffixes;

            private CommonPrefix(int size, Suffix... suffixes) {
                this.size = size;
                this.suffixes = asList(suffixes);
            }

            public int size() {
                return size;
            }

            @Override
            public String toString() {
                return elements.subList(index, index+ size).toString();
            }

            public List<Suffix> getSuffixes() {
                return suffixes;
            }
        }
    }

    public class CommonPrefixes {

        public StreamEx<Suffix.CommonPrefix> stream() {
            return StreamEx.of(suffixes)
                    .pairMap(Suffix::getCommonPrefix);
        }

        public int size() {
            return elements.size() - 1;
        }

    }

}
