/*
 * Copyright (C) 2014-2022 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.DataVector;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Ranks data values onto a continuous interval.
 *
 * @author Scott Shaffer
 */
public final class RankPipeline implements DataPipeline {
    /**
     * The minimum rank.
     */
    @Getter
    private final double lower;

    /**
     * The maximum rank.
     */
    @Getter
    private final double upper;

    /**
     * Creates a new ranking pipeline for a given interval.
     *
     * @param lower the minimum rank.
     * @param upper the maximum rank.
     *
     * @throws RuntimeException unless the maximum rank is greater than
     * the minimum.
     */
    public RankPipeline(double lower, double upper) {
        if (upper <= lower)
            throw new MorpheusException("Invalid rank interval: [%f, %f].", lower, upper);

        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public <K> DataVector<K> apply(DataVector<K> vector) {
        var records = new ArrayList<Record<K>>(vector.length());

        for (var element : vector.collectElements())
            records.add(new Record<>(element.getKey(), element.getValue()));

        computeRanks(records);

        for (var record : records)
            vector.setElement(record.key, record.score);

        return vector;
    }

    @Override
    public D3xVector apply(D3xVector vector) {
        var records = new ArrayList<Record<Integer>>(vector.length());

        for (var index = 0; index < vector.length(); ++index)
            records.add(new Record<>(index, vector.get(index)));

        computeRanks(records);

        for (var record : records)
            vector.set(record.key, record.score);

        return vector;
    }

    @Override
    public String encode() {
        return String.format("rank(%f, %f)", lower, upper);
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isSizePreserving() {
        return true;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Record<K> implements Comparable<Record<K>> {
        private final K key;
        private final double value;

        private int rank = 0;
        private double score = Double.NaN;

        @Override
        public int compareTo(Record<K> that) {
            return Double.compare(this.value, that.value);
        }
    }

    private <K> void computeRanks(List<Record<K>> records) {
        records.sort(null);

        var minRank = 1;
        var maxRank = 1;

        if (!records.isEmpty()) {
            var record = records.get(0);

            if (!Double.isNaN(record.value))
                record.rank = minRank;
        }

        for (var index = 1; index < records.size(); ++index) {
            var curr = records.get(index);
            var prev = records.get(index - 1);

            if (Double.isNaN(curr.value)) {
                // NaNs compare as greater than all other floating point
                // values, even positive infinity.  If this value is NaN,
                // then all subsequent values must be NaN, so stop...
                break;
            }
            else if (DoubleComparator.DEFAULT.equals(curr.value, prev.value)) {
                // This record is tied with the previous one, so it must
                // have the same rank...
                curr.rank = prev.rank;
            }
            else {
                curr.rank = prev.rank + 1;
                maxRank = curr.rank;
            }
        }

        if (maxRank == minRank) {
            // All records were tied, score them at the midpoint of the
            // ranking interval...
            var average = 0.5 * (lower + upper);

            for (var record : records)
                if (!Double.isNaN(record.value))
                    record.score = average;
        }
        else {
            // Rescale the ranks from the interval [minRank, maxRank] to
            // the ranking interval [lower, upper]...
            for (var record : records)
                if (!Double.isNaN(record.value))
                    record.score = lower + (upper - lower) * (record.rank - minRank) / (maxRank - minRank);
        }
    }
}
