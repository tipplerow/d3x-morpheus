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
package com.d3x.morpheus.matrix;

import com.d3x.morpheus.vector.D3xVectorView;

import lombok.NonNull;

/**
 * Provides a read-only view of a matrix column, with an optional selection
 * of a subvector within the column.
 *
 * @author Scott Shaffer
 */
final class ColumnView implements D3xVectorView {
    @NonNull
    private final D3xMatrixView matrix;

    private final int colIndex;
    private final int colLength;
    private final int rowOffset;

    ColumnView(@NonNull D3xMatrixView matrix, int colIndex) {
        this(matrix, colIndex, 0, matrix.nrow());
    }

    ColumnView(@NonNull D3xMatrixView matrix, int colIndex, int rowOffset, int colLength) {
        matrix.validateRowIndex(rowOffset);
        matrix.validateColumnIndex(colIndex);

        if (colLength < 0 || colLength > matrix.nrow())
            throw new IllegalArgumentException("Invalid column length.");

        this.matrix = matrix;
        this.colIndex = colIndex;
        this.colLength = colLength;
        this.rowOffset = rowOffset;
    }

    @Override
    public int length() {
        return colLength;
    }

    @Override
    public double get(int index) {
        return matrix.get(index + rowOffset, colIndex);
    }
}
