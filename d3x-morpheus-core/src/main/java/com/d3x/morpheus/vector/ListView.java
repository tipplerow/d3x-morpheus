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

import lombok.NonNull;

import java.util.List;

/**
 * Wraps a Double list in a vector view.
 *
 * @author Scott Shaffer
 */
final class ListView implements D3xVectorView {
    @NonNull private final List<Double> list;

    ListView(@NonNull List<Double> list) {
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
}
