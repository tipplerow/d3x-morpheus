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
package com.d3x.morpheus.vector;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.MorpheusException;

import lombok.Getter;
import lombok.NonNull;

/**
 * Collects common keys from DataVectors and merges them into a DataFrame.
 *
 * @author Scott Shaffer
 */
public final class DataVectorMerge<R extends Comparable<R>, C> {
    /**
     * Whether the merged row keys are the union ({@code all == true})
     * or intersection ({@code all == false}) of the individual column
     * row keys.
     */
    @Getter
    private final boolean all;

    /**
     * The row key comparator.
     */
    @Getter @NonNull
    private final Comparator<R> rowComp;

    private final Set<R> rowKeys;
    private final Map<C, DataVectorView<R>> columns = new LinkedHashMap<>();

    /**
     * Creates a merge operator using the default row key comparator.
     *
     * @param all whether the merged row keys are the union ({@code true})
     *            or intersection ({@code false)} of the individual column
     *            row keys.
     */
    public DataVectorMerge(boolean all) {
        this(all, Comparable::compareTo);
    }

    /**
     * Creates a merge operator using a custom row key comparator.
     *
     * @param all whether the merged row keys are the union ({@code true})
     *            or intersection ({@code false)} of the individual column
     *            row keys.
     *
     * @param rowComp the row key comparator.
     */
    public DataVectorMerge(boolean all, @NonNull Comparator<R> rowComp) {
        this.all = all;
        this.rowComp = rowComp;
        this.rowKeys = new TreeSet<>(rowComp);
    }

    /**
     * Adds a column to this merge operator.
     *
     * @param colKey    the column key to assign.
     * @param colVector the column vector to add.
     *
     * @return this merge operator, for operator chaining.
     *
     * @throws RuntimeException unless the column key is unique with
     * respect to the existing column keys.
     */
    public DataVectorMerge<R, C> addColumn(@NonNull C colKey, @NonNull DataVectorView<R> colVector) {
        if (columns.isEmpty()) {
            columns.put(colKey, colVector);
            rowKeys.addAll(colVector.collectKeys());
        }
        else if (columns.containsKey(colKey)) {
            throw new MorpheusException("Duplicate column key: [%s].", colKey);
        }
        else {
            columns.put(colKey, colVector);

            if (all)
                rowKeys.addAll(colVector.collectKeys());
            else
                rowKeys.retainAll(colVector.collectKeys());
        }

        return this;
    }

    /**
     * Executes the merge operation.
     *
     * @return a new DataFrame containing the merged column data.
     */
    public DataFrame<R, C> merge() {
        var colKeys = columns.keySet();
        var dataFrame = DataFrame.ofDoubles(rowKeys, colKeys);

        for (var entry : columns.entrySet()) {
            var colKey = entry.getKey();
            var vector = entry.getValue();

            for (var rowKey : rowKeys)
                dataFrame.setDouble(rowKey, colKey, vector.getElement(rowKey));
        }

        return dataFrame;
    }
}
