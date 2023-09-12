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
package com.d3x.morpheus.root;

import com.d3x.morpheus.util.DoubleInterval;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import org.apache.commons.math3.analysis.solvers.BrentSolver;

import java.util.function.DoubleUnaryOperator;

/**
 * Applies Brent's method for univariate root finding.
 *
 * @author Scott Shaffer
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrentRootFinder extends UnivariateRootFinder {
    private final BrentSolver solver;
    private static final int MAX_SOLVE_ITER = 1000;

    /**
     * Creates a new Brent solver with the default accuracy ({@code 1.0E-06}).
     */
    public BrentRootFinder() {
        this(new BrentSolver());
    }

    /**
     * Creates a new Brent solver with a given accuracy.
     *
     * @param absoluteAccuracy the absolute accuracy required in the solution.
     */
    public BrentRootFinder(double absoluteAccuracy) {
        this(new BrentSolver(absoluteAccuracy));
    }

    /**
     * Creates a new Brent solver with a given accuracy.
     *
     * @param absoluteAccuracy the absolute accuracy required in the solution.
     * @param relativeAccuracy the relative accuracy required in the solution.
     */
    public BrentRootFinder(double absoluteAccuracy, double relativeAccuracy) {
        this(new BrentSolver(absoluteAccuracy, relativeAccuracy));
    }

    @Override
    public double solve(@NonNull DoubleUnaryOperator function, @NonNull DoubleInterval interval, double initial) {
        if (interval.contains(initial)) {
            return solver.solve(MAX_SOLVE_ITER, function::applyAsDouble, interval.getLower(), interval.getUpper(), initial);
        }
        else {
            throw new IllegalArgumentException("Initial guess must lie within the bounding interval.");
        }
    }
}
