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

/**
 * Wraps a bare array in a vector view.
 *
 * @author Scott Shaffer
 */
final class ArrayView implements D3xMatrixView {
    private final double[][] array;

    ArrayView(double[][] array) {
        this.array = array;
    }

    @Override
    public boolean isEmpty() {
        return array.length == 0;
    }

    @Override
    public int nrow() {
        return array.length;
    }

    @Override
    public int ncol() {
        return array.length > 0 ? array[0].length : 0;
    }

    @Override
    public double get(int row, int col) {
        return array[row][col];
    }
}
