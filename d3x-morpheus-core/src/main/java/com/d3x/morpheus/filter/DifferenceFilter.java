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
package com.d3x.morpheus.filter;

import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVectorView;

import lombok.Getter;

/**
 * Provides finite difference filters of order 1 and 2.
 *
 * @author Scott Shaffer
 */
public class DifferenceFilter extends CustomFilter {
    /**
     * The order of the finite difference.
     */
    @Getter
    private final int order;

    private DifferenceFilter(int order) {
        super(resolveCoefficients(order));
        this.order = order;
    }

    private static D3xVectorView resolveCoefficients(int order) {
        switch (order) {
            case 1:
                return D3xVectorView.of(1.0, -1.0);

            case 2:
                return D3xVectorView.of(1.0, -2.0, 1.0);

            default:
                throw new MorpheusException("Unsupported difference order: [%d].", order);
        }
    }

    /**
     * The time-series name for string encoding.
     */
    public static final String NAME = "diff";

    /**
     * The first-order difference filter: {@code y[i] = x[i] - x[i - 1]}.
     */
    public static DifferenceFilter FIRST = new DifferenceFilter(1);

    /**
     * The second-order difference filter: {@code y[i] = x[i] - 2 * x[i - 1] + x[i - 2].}
     */
    public static DifferenceFilter SECOND = new DifferenceFilter(2);

    /**
     * Returns the finite-difference filter of a given order.
     *
     * @param order the order of the finite difference.
     *
     * @throws RuntimeException unless the order is valid.
     */
    public static DifferenceFilter of(int order) {
        switch (order) {
            case 1:
                return FIRST;

            case 2:
                return SECOND;

            default:
                throw new MorpheusException("Unsupported difference order: [%d].", order);
        }
    }

    @Override
    public String encodeArgs() {
        return Integer.toString(order);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
