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

import com.d3x.morpheus.root.UnivariateRootFinder;
import com.d3x.morpheus.util.DoubleInterval;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;

import com.d3x.morpheus.stats.Median;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVectorView;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.DoubleUnaryOperator;

/**
 * Fits the model {@code y = a + bx} by minimizing the median absolute error.
 *
 * @author Scott Shaffer
 */
public final class UnivariateMADRegression extends UnivariateRegression {
    // Maximum number of bisection iterations...
    private static final int MAX_ITER = 1000;

    // Relative convergence tolerance for the bisection search...
    private static final double RELATIVE_TOLERANCE = 1.0E-06;

    /**
     * Creates an empty regression model with the default capacity.
     */
    public UnivariateMADRegression() {
        super();
    }

    /**
     * Creates an empty regression model with a given capacity.
     *
     * @param capacity the initial capacity of the model (the number of
     *                 observations that may be added before the internal
     *                 storage needs to be resized).
     */
    public UnivariateMADRegression(int capacity) {
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
    public UnivariateMADRegression(@NonNull D3xVectorView x,
                                   @NonNull D3xVectorView y) {
        super(x, y);
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
    public static UnivariateMADRegression fit(@NonNull D3xVectorView x,
                                              @NonNull D3xVectorView y) {
        var model = new UnivariateMADRegression(x, y);
        model.fit();
        return model;
    }

    @Override
    public void add(double x, double y) {
        super.add(x, y, 1.0);
    }

    @Override
    public void add(double x, double y, double wt) {
        if (DoubleComparator.DEFAULT.equals(wt, 1.0))
            super.add(x, y, 1.0);
        else
            throw new MorpheusException("Weighted regressions are not implemented.");
    }

    @Override
    public void add(@NonNull D3xVectorView vecX,
                    @NonNull D3xVectorView vecY,
                    @NonNull D3xVectorView vecW) {
        throw new MorpheusException("Weighted regressions are not implemented.");
    }

    @Override
    public void fit() {
        if (numObs < 2)
            throw new MorpheusException("At least two observations are required.");

        // Following "Numerical Recipes" Section 15.7, the slope
        // coefficient "b" satisfies the following equation:
        //
        // 0 = sum{xi * sgn(yi - b * xi - a)}
        //
        // where the intercept "a" is equal to the median of the
        // vector expression (y - b * x).
        //
        // We employ a bisection search to solve for "b" starting
        // with the OLS coefficient as the initial guess...
        var vecX = D3xVectorView.of(dataX);
        var vecY = D3xVectorView.of(dataY);
        var ols = UnivariateWLSRegression.fit(vecX, vecY);
        var olsB = ols.getSlope();

        // Bracket the root of the function in an interval containing
        // the OLS coefficient...
        var function = new FunctionB(vecX, vecY);
        var interval = bracketOLS(function, olsB);

        var absTol = RELATIVE_TOLERANCE * Math.abs(olsB);
        var solver = new BisectionSolver(absTol);

        slope = solver.solve(MAX_ITER, function, interval.getLower(), interval.getUpper());
        intercept = function.trialA;
    }

    private DoubleInterval bracketOLS(FunctionB function, double olsB) {
        var delta = 0.5 * Math.max(1.0, Math.abs(olsB));
        var interval = DoubleInterval.closed(olsB - delta, olsB + delta);
        return UnivariateRootFinder.bracket(function, interval);
    }

    @Override
    protected void update(double x, double y, double wt) {
        // Parameters estimations are not incremental, nothing to update...
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class FunctionB implements DoubleUnaryOperator, UnivariateFunction {
        private final D3xVectorView x;
        private final D3xVectorView y;
        private double trialA;

        @Override
        public double applyAsDouble(double trialB) {
            return value(trialB);
        }

        @Override
        public double value(double trialB) {
            // Following Numerical Recipes, Section 15.7, when coefficient "b"
            // is at its optimal value, coefficient "a" is equal to the median
            // of (y - b * x)...
            var function = 0.0;
            var yMinusBX = D3xVectorView.applyEBE(x, y, (xi, yi) -> yi - trialB * xi);
            trialA = new Median().compute(yMinusBX);

            for (var index = 0; index < x.length(); ++index) {
                var comp = DoubleComparator.DEFAULT.compare(yMinusBX.get(index), trialA);

                if (comp < 0)
                    function -= x.get(index);
                else if (comp > 0)
                    function += x.get(index);
            }

            return function;
        }
    }
}
