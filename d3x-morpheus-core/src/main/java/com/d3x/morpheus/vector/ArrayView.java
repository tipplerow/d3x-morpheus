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

import java.util.stream.DoubleStream;

/**
 * Wraps a bare array in a vector view.
 *
 * @author Scott Shaffer
 */
final class ArrayView implements D3xVectorView {
    private final double[] array;

    ArrayView(double[] array) {
        this.array = array;
    }

    @Override
    public int length() {
        return array.length;
    }

    @Override
    public double get(int index) {
        return array[index];
    }

    @Override
    public DoubleStream stream() {
        return DoubleStream.of(array);
    }
}
