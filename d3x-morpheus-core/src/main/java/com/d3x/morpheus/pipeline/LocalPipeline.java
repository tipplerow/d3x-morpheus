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
package com.d3x.morpheus.pipeline;

import java.util.function.DoubleUnaryOperator;

import com.d3x.morpheus.vector.DataVector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Implements local, length-preserving data pipelines.
 *
 * @author Scott Shaffer
 */
@AllArgsConstructor(staticName = "of")
public final class LocalPipeline implements DataPipeline {
    /**
     * The local transformation applied to each element.
     */
    @Getter @NonNull
    private final DoubleUnaryOperator operator;

    @Override
    public <K> DataVector<K> apply(DataVector<K> vector) {
        for (K key : vector.collectKeys())
            vector.setElement(key, operator.applyAsDouble(vector.getElement(key)));

        return vector;
    }

    @Override
    public boolean isLengthPreserving() {
        return true;
    }

    @Override
    public boolean isLocal() {
        return true;
    }
}
