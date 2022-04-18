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
 * Provides a read-only view of a matrix row, with an optional selection
 * of a subvector within the row.
 *
 * @author Scott Shaffer
 */
final class RowView implements D3xVectorView {
    @NonNull
    private final D3xMatrixView matrix;

    private final int rowIndex;
    private final int colOffset;
    private final int rowLength;

    RowView(@NonNull D3xMatrixView matrix, int rowIndex) {
        this(matrix, rowIndex, 0, matrix.ncol());
    }

    RowView(@NonNull D3xMatrixView matrix, int rowIndex, int colOffset, int rowLength) {
        matrix.validateRowIndex(rowIndex);
        matrix.validateColumnIndex(colOffset);

        if (rowLength < 0 || rowLength > matrix.ncol())
            throw new IllegalArgumentException("Invalid row length.");

        this.matrix = matrix;
        this.rowIndex = rowIndex;
        this.rowLength = rowLength;
        this.colOffset = colOffset;
    }

    @Override
    public int length() {
        return rowLength;
    }

    @Override
    public double get(int index) {
        return matrix.get(rowIndex, index + colOffset);
    }
}
