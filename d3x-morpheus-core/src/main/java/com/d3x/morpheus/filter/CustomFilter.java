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

import com.d3x.morpheus.vector.D3xVectorView;

import lombok.NonNull;

/**
 * Implements a linear time-series filter with arbitrary coefficients
 * provided by the user.
 *
 * @author Scott Shaffer
 */
public class CustomFilter extends AbstractFilter {
    private final D3xVectorView coefficients;

    /**
     * The time-series name for string encoding.
     */
    public static final String NAME = "filter";

    /**
     * Creates a linear filter with arbitrary coefficients.
     *
     * @param coefficients the filter coefficients.
     *
     * @throws RuntimeException unless the coefficients are valid.
     */
    public CustomFilter(@NonNull D3xVectorView coefficients) {
        this.coefficients = coefficients;
        validateCoefficients(coefficients);
    }

    @Override
    public String encodeArgs() {
        StringBuilder builder = new StringBuilder();
        builder.append(coefficients.get(0));

        for (int index = 1; index < coefficients.length(); ++index) {
            builder.append(FilterParser.ARG_DELIM);
            builder.append(" ");
            builder.append(coefficients.get(index));
        }

        return builder.toString();
    }

    @Override
    public D3xVectorView getCoefficients() {
        return coefficients;
    }

    @Override
    public double getCoefficient(int lag) {
        return coefficients.get(lag);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getWindowLength() {
        return coefficients.length();
    }
}
