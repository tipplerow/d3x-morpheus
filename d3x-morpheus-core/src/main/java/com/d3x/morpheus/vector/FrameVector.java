/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
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

import com.d3x.morpheus.frame.DataFrameVector;
import com.d3x.morpheus.util.MorpheusException;

import lombok.NonNull;

/**
 * Wraps a row or column of a numeric DataFrame in a D3xVector.
 *
 * @author Scott Shaffer
 */
final class FrameVector implements D3xVector {
    @NonNull
    private final DataFrameVector<?,?,?,?,?> vector;

    private FrameVector(@NonNull DataFrameVector<?,?,?,?,?> vector) {
        validateVector(vector);
        this.vector = vector;
    }

    private static void validateVector(DataFrameVector<?,?,?,?,?> vector) {
        if (!vector.isNumeric())
            throw new MorpheusException("Non-numeric data frame vector.");
    }

    /**
     * Presents a numeric data frame row or column as a D3xVector.
     *
     * @param vector the numeric row or column from a data frame.
     *
     * @return a D3xVector adapter for the specified row or column.
     */
    static FrameVector wrap(DataFrameVector<?,?,?,?,?> vector) {
        return new FrameVector(vector);
    }

    @Override
    public double get(int index) {
        return vector.getDoubleAt(index);
    }

    @Override
    public int length() {
        return vector.size();
    }

    @Override
    public D3xVector like(int length) {
        return D3xVector.dense(length);
    }

    @Override
    public void set(int index, double value) {
        vector.setDoubleAt(index, value);
    }
}
