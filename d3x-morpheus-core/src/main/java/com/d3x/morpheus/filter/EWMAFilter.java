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
package com.d3x.morpheus.filter;

import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;

import lombok.Getter;

/**
 * Implements an exponentially-weighted moving average as a linear
 * time-series filter.
 *
 * @author Scott Shaffer
 */
public class EWMAFilter extends CustomFilter {
    /**
     * The half-life for the exponential decay of the observation
     * weights.
     */
    @Getter
    private final double halfLife;

    private static final double LOG_ONE_HALF = Math.log(0.5);

    /**
     * The time-series name for string encoding.
     */
    public static final String NAME = "ewma";

    /**
     * Creates and validates an exponentially-weighted moving average
     * filter.
     *
     * @param halfLife the half-life for the exponential decay of the
     *                 observation weights.
     *
     * @param window   the length of the observation window.
     *
     * @throws RuntimeException unless the EWMA parameters are valid.
     */
    public EWMAFilter(double halfLife, int window) {
        super(computeWeights(halfLife, window));
        this.halfLife = halfLife;
    }

    /**
     * Computes the observation weights for a moving average.
     *
     * @param halfLife the half-life of the moving average.
     * @param window   the number of observations in the averaging period.
     *
     * @return the observation weights for the specified parameters.
     */
    public static D3xVector computeWeights(double halfLife, int window) {
        validateWindow(window);
        validateHalfLife(halfLife);
        var weights = D3xVector.dense(window);

        for (int lag = 0; lag < window; ++lag)
            weights.set(lag, computeWeight(halfLife, lag));

        weights.normalize();
        return weights;
    }

    private static double computeWeight(double halfLife, int lagIndex) {
        return Math.exp(lagIndex * LOG_ONE_HALF / halfLife);
    }

    /**
     * Ensures that a half-life is positive.
     *
     * @param halfLife the half-life to validate.
     *
     * @throws RuntimeException unless the half-life is positive.
     */
    public static void validateHalfLife(double halfLife) {
        if (DoubleComparator.DEFAULT.isNonPositive(halfLife))
            throw new MorpheusException("Half-life must be positive.");
    }

    @Override
    public String encodeArgs() {
        return String.format("%s%c %s", halfLife, FilterParser.ARG_DELIM, getWindowLength());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
