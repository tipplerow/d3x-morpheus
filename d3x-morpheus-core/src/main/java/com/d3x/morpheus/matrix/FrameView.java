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

import com.d3x.morpheus.frame.DataFrame;

import lombok.NonNull;

/**
 * Wraps a DataFrame in a matrix view.
 *
 * @author Scott Shaffer
 */
final class FrameView implements D3xMatrixView {
    @NonNull private final DataFrame<?,?> frame;

    FrameView(@NonNull DataFrame<?,?> frame) {
        this.frame = frame;
    }

    @Override
    public boolean isEmpty() {
        return nrow() == 0;
    }

    @Override
    public int nrow() {
        return frame.rowCount();
    }

    @Override
    public int ncol() {
        return frame.colCount();
    }

    @Override
    public double get(int row, int col) {
        return frame.getDoubleAt(row, col);
    }
}
