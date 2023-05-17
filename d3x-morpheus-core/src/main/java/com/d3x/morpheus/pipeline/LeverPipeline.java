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
/*
 * Copyright 2018-2023, Talos Trading - All Rights Reserved
 *
 * Licensed under a proprietary end-user agreement issued by D3X Systems.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.d3xsystems.com/static/eula/quanthub-eula.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.pipeline;

import com.d3x.morpheus.stats.SumAbs;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.DataVector;

import lombok.Getter;

/**
 * Implements a non-local, size-preserving pipeline that rescales the
 * elements of a vector to a target leverage.
 *
 * @author Scott Shaffer
 */
public final class LeverPipeline extends AbstractDataPipeline {
    /**
     * The target leverage.
     */
    @Getter
    private final double leverage;

    /**
     * Creates a new non-local, size-preserving pipeline that rescales
     * the elements of a vector to a target <em>leverage</em>: the sum
     * of the absolute values in the vector.
     *
     * @param leverage the target leverage.
     *
     * @throws RuntimeException unless the target leverage is positive.
     */
    public LeverPipeline(double leverage) {
        if (DoubleComparator.DEFAULT.isPositive(leverage))
            this.leverage = leverage;
        else
            throw new MorpheusException("Target leverage must be positive.");
    }

    @Override
    public <K> DataVector<K> apply(DataVector<K> vector) {
        double norm1 = vector.norm1();

        if (DoubleComparator.DEFAULT.isPositive(norm1))
            return DataPipeline.multiply(leverage / norm1).apply(vector);
        else
            throw new MorpheusException("Cannot apply target leverage to a vector with zero norm.");
    }

    @Override
    public D3xVector apply(D3xVector vector) {
        double norm1 = new SumAbs().compute(vector);

        if (DoubleComparator.DEFAULT.isPositive(norm1))
            return DataPipeline.multiply(leverage / norm1).apply(vector);
        else
            throw new MorpheusException("Cannot apply target leverage to a vector with zero norm.");
    }

    @Override
    public String encode() {
        return "lever(" + leverage + ")";
    }

    @Override
    public boolean isSizePreserving() {
        return true;
    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
