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
package com.d3x.morpheus.dist;

import com.d3x.morpheus.util.DoubleInterval;
import com.d3x.morpheus.util.PointXY;

import lombok.NonNull;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Produces a cumulative distribution function (CDF) by integrating a
 * known probability density function (PDF) numerically, tabulating the
 * integral on a grid, and interpolating the tabulated integral values.
 *
 * @author Scott Shaffer
 */
public final class NumericalCDF implements DoubleUnaryOperator {
    private final double minX;
    private final double maxX;
    private final PolynomialSplineFunction spline;

    // Valid ranges for the unit step size and the CDF tail threshold...
    private static final DoubleInterval UNIT_STEP_RANGE = DoubleInterval.closed(1.0E-04, 0.1);
    private static final DoubleInterval THRESHOLD_RANGE = DoubleInterval.closed(1.0E-08, 0.01);

    private NumericalCDF(double[] cdfX, double[] cdfY) {
        this.minX = cdfX[0];
        this.maxX = cdfX[cdfX.length - 1];
        var inter = new SplineInterpolator();
        this.spline = inter.interpolate(cdfX, cdfY);
    }

    /**
     * Creates a new cumulative distribution function for an existing
     * distribution.
     *
     * @param dist a distribution whose PDF is known explicitly.
     *
     * @param unitStep the step size for the numerical integration <em>in units
     *                 of the standard deviation</em>. The number of grid points
     *                 will be approximately {@code (int) (10.0 / unitStep)}.
     *
     * @param threshold the threshold for the cumulative distribution function.
     *
     * @return a numerical CDF computed from the PDF of the input distribution.
     */
    public static NumericalCDF create(@NonNull RealDistribution dist, double unitStep, double threshold) {
        var estimator = new Estimator(dist, unitStep, threshold);
        return estimator.estimate();
    }

    @Override
    public double applyAsDouble(double x) {
        if (x <= minX) {
            return 0.0;
        }
        else if (x < maxX) {
            return spline.value(x);
        }
        else {
            return 1.0;
        }
    }

    private static final class Estimator {
        private final double step;
        private final double lower;
        private final double upper;
        private final double median;
        private final double threshold;
        private final RealDistribution dist;
        private final List<PointXY> points = new ArrayList<>();

        private Estimator(RealDistribution dist, double unitStep, double threshold) {
            if (!UNIT_STEP_RANGE.contains(unitStep))
                throw new IllegalArgumentException("Invalid step size.");

            if (!THRESHOLD_RANGE.contains(threshold))
                throw new IllegalArgumentException("Invalid CDF threshold.");

            this.dist = dist;
            this.threshold = threshold;

            var sdev = dist.sdev();
            var support = dist.support();

            this.step = unitStep * sdev;
            this.median = dist.median();
            this.lower = Math.max(support.getLower(), median - 20.0 * sdev);
            this.upper = Math.min(support.getUpper(), median + 20.0 * sdev);
        }

        private NumericalCDF estimate() {
            points.add(PointXY.of(median, 0.5));
            integrateForward();
            integrateBackward();
            PointXY.sortX(points);
            return new NumericalCDF(PointXY.toArrayX(points), PointXY.toArrayY(points));
        }

        private void integrateForward() {
            var x0 = median;
            var cdf0 = 0.5;

            while (true) {
                var x1 = x0 + step;

                if (x1 > upper) {
                    points.add(PointXY.of(upper, 1.0));
                    break;
                }

                var cdf1 = cdf0 + simpson(x0, x1);
                points.add(PointXY.of(x1, cdf1));

                if (cdf1 >= 1.0 - threshold) {
                    points.add(PointXY.of(x1 + step, 1.0));
                    break;
                }

                x0 = x1;
                cdf0 = cdf1;
            }
        }

        private void integrateBackward() {
            var x1 = median;
            var cdf1 = 0.5;

            while (true) {
                var x0 = x1 - step;

                if (x0 < lower) {
                    points.add(PointXY.of(lower, 0.0));
                    break;
                }

                var cdf0 = cdf1 - simpson(x0, x1);
                points.add(PointXY.of(x0, cdf0));

                if (cdf0 <= threshold) {
                    points.add(PointXY.of(x0 - step, 0.0));
                    break;
                }

                x1 = x0;
                cdf1 = cdf0;
            }
        }

        private double simpson(double x0, double x1) {
            var xm = 0.5 * (x0 + x1);
            var p0 = dist.pdf(x0);
            var pm = dist.pdf(xm);
            var p1 = dist.pdf(x1);
            return (x1 - x0) * (p0 + 4.0 * pm + p1) / 6.0;
        }
    }
}
