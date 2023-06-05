/*
 * Copyright (C) 2014-2023 Talos Trading - All Rights Reserved
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

import com.d3x.morpheus.stats.Percentile;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.DataVector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Implements a non-local, size-preserving pipeline that pulls outliers
 * into a location defined by a quantile value.
 *
 * @author Scott Shaffer
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrimPipeline extends AbstractDataPipeline {
    /**
     * The fractional quantile value that defines the lower and upper
     * bounds for the elements.
     */
    private final double quantile;

    /**
     * Returns a non-local, size-preserving pipeline that pulls outliers
     * into a location defined by a quantile value.  With a quantile value
     * of {@code 0.05}, for example, elements below the 5th percentile will
     * be raised to the 5th percentile and those above the 95th percentile
     * will be lowered to the 95th percentile.
     *
     * @param quantile the (fractional) quantile value that defines the
     *                 lower and upper bounds for the elements.
     *
     * @return a trimming pipeline for the specified quantile.
     *
     * @throws RuntimeException unless the quantile is within the valid range
     * {@code [0.0, 0.5]}.
     */
    public static DataPipeline of(double quantile) {
        var comparator = DoubleComparator.DEFAULT;

        if (comparator.isNegative(quantile))
            throw new MorpheusException("Quantile must be non-negative.");

        if (comparator.isZero(quantile))
            return DataPipeline.identity;

        if (comparator.compare(quantile, 0.5) > 0)
            throw new MorpheusException("Quantile must not exceed one-half.");

        return new TrimPipeline(quantile);
    }

    @Override
    public <K> DataVector<K> apply(DataVector<K> vector) {
        // Percentile takes fractional (quantile) values...
        double lower = new Percentile(quantile).compute(vector);
        double upper = new Percentile(1.0 - quantile).compute(vector);

        return DataPipeline.bound(lower, upper).apply(vector);
    }

    @Override
    public D3xVector apply(D3xVector vector) {
        // Percentile takes fractional (quantile) values...
        double lower = new Percentile(quantile).compute(vector);
        double upper = new Percentile(1.0 - quantile).compute(vector);

        return DataPipeline.bound(lower, upper).apply(vector);
    }

    @Override
    public String encode() {
        return "trim(" + quantile + ")";
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
