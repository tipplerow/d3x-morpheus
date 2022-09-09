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

/**
 * Implements a moving average as a linear time-series filter.
 *
 * @author Scott Shaffer
 */
public final class MovingAverageFilter extends AbstractFilter {
    private final int window;
    private final double coeff;

    /**
     * The time-series name for string encoding.
     */
    public static final String NAME = "ma";

    /**
     * Creates a linear filter with arbitrary coefficients.
     *
     * @param window the moving-average window length.
     *
     * @throws RuntimeException unless the coefficients are valid.
     */
    public MovingAverageFilter(int window) {
        validateWindow(window);
        this.window = window;
        this.coeff  = 1.0 / window;
    }

    @Override
    public String encodeArgs() {
        return Integer.toString(window);
    }

    @Override
    public double getCoefficient(int lag) {
        validateLag(lag);
        return coeff;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getWindowLength() {
        return window;
    }
}
