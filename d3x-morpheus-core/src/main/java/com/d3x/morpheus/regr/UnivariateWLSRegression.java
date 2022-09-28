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
package com.d3x.morpheus.regr;

import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVectorView;

import lombok.NonNull;

/**
 * Fits the model {@code y = a + bx} by minimizing the weighted mean
 * square error.
 *
 * @author Scott Shaffer
 */
public final class UnivariateWLSRegression extends UnivariateRegression {
    private double sumX;
    private double sumY;
    private double sumXY;
    private double sumXX;
    private double sumWT;

    /**
     * Creates an empty regression model with the default capacity.
     */
    public UnivariateWLSRegression() {
        super();
    }

    /**
     * Creates an empty regression model with a given capacity.
     *
     * @param capacity the initial capacity of the model (the number of
     *                 observations that may be added before the internal
     *                 storage needs to be resized).
     */
    public UnivariateWLSRegression(int capacity) {
        super(capacity);
    }

    /**
     * Creates a regression model populated with observations.
     *
     * @param x the independent variables.
     * @param y the dependent observations.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    public UnivariateWLSRegression(@NonNull D3xVectorView x,
                                   @NonNull D3xVectorView y) {
        super(x, y);
    }

    /**
     * Creates a regression model populated with observations.
     *
     * @param x  the independent variables.
     * @param y  the dependent observations.
     * @param wt the observation weights.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    public UnivariateWLSRegression(@NonNull D3xVectorView x,
                                   @NonNull D3xVectorView y,
                                   @NonNull D3xVectorView wt) {
        super(x, y, wt);
    }

    /**
     * Fits a regression model to a set of equally weighted observations.
     *
     * @param x the independent variables.
     * @param y the dependent observations.
     *
     * @return the fitted regression model.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    public static UnivariateWLSRegression fit(@NonNull D3xVectorView x,
                                              @NonNull D3xVectorView y) {
        var model = new UnivariateWLSRegression(x, y);
        model.fit();
        return model;
    }

    /**
     * Creates a regression model populated with observations.
     *
     * @param x  the independent variables.
     * @param y  the dependent observations.
     * @param wt the observation weights.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    public static UnivariateWLSRegression fit(@NonNull D3xVectorView x,
                                              @NonNull D3xVectorView y,
                                              @NonNull D3xVectorView wt) {
        var model = new UnivariateWLSRegression(x, y, wt);
        model.fit();
        return model;
    }

    @Override
    public void fit() {
        if (numObs < 2)
            throw new MorpheusException("At least two observations are required.");

        var denom = sumWT * sumXX - sumX * sumX;

        slope = (sumWT * sumXY - sumX * sumY) / denom;
        intercept = (sumXX * sumY - sumX * sumXY) / denom;
    }

    @Override
    protected void update(double x, double y, double wt) {
        sumX += wt * x;
        sumY += wt * y;
        sumXX += wt * x * x;
        sumXY += wt * x * y;
        sumWT += wt;
    }
}
