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

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

/**
 * @author Scott Shaffer
 */
final class ListVector implements D3xVector {
    @NonNull private final List<Double> list;

    ListVector(@NonNull List<Double> list) {
        this.list = list;
    }

    @Override
    public int length() {
        return list.size();
    }

    @Override
    public double get(int index) {
        return list.get(index);
    }

    @Override
    public void set(int index, double value) {
        list.set(index, value);
    }

    @Override
    public D3xVector like(int length) {
        return new ListVector(new ArrayList<>(list));
    }
}
