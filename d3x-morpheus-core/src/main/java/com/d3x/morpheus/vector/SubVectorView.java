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

import com.d3x.morpheus.util.MorpheusException;

import lombok.NonNull;

/**
 * Provides a sub-vector view of another vector.
 *
 * @author Scott Shaffer
 */
final class SubVectorView implements D3xVectorView {
    private final int start;
    private final int length;

    @NonNull
    private final D3xVectorView parent;

    SubVectorView(@NonNull D3xVectorView parent, int start, int length) {
        this.parent = parent;
        this.start = start;
        this.length = length;
        validate();
    }

    private void validate() {
        if (start < 0)
            throw new MorpheusException("Initial index must be non-negative.");

        if (start >= parent.length())
            throw new MorpheusException("Initial index must be less than the parent length.");

        if (length < 0)
            throw new MorpheusException("Subvector length must be non-negative.");

        if (length > parent.length() - start)
            throw new MorpheusException("Subvector length exceeds the allowable length.");
    }

    @Override
    public double get(int index) {
        return parent.get(index + start);
    }

    @Override
    public int length() {
        return length;
    }
}
