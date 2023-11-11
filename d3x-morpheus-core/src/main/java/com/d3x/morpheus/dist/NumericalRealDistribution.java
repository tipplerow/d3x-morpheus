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

import com.d3x.morpheus.util.LazyValue;

import lombok.Getter;

/**
 * Represents a continuous probability distribution where the probability
 * density is known analytically but the cumulative distribution must be
 * computed numerically.
 *
 * @author Scott Shaffer
 */
public abstract class NumericalRealDistribution extends AbstractRealDistribution {
    /**
     * The numerical integration step size, in units of the standard deviation.
     */
    @Getter
    private final double unitStep;

    /**
     * The threshold for the cumulative distribution function.  (The CDF is
     * assumed to be zero everywhere it is below this threshold.)
     */
    @Getter
    private final double threshold;

    // The CDF must be created on demand, rather than in the constructor, because
    // the PDF must be evaluated when computing the CDF and the parameters for the
    // PDF may not be initialized in the constructor of this base class...
    private final LazyValue<NumericalCDF> cdf = LazyValue.of(this::createCDF);

    // The default step size and threshold for the CDF...
    private static final double UNIT_STEP_DEFAULT = 1.0E-02;
    private static final double THRESHOLD_DEFAULT = 1.0E-06;

    /**
     * Initializes this numerical distribution with the default step size and
     * CDF threshold.
     */
    protected NumericalRealDistribution() {
        this(UNIT_STEP_DEFAULT, THRESHOLD_DEFAULT);
    }

    /**
     * Initializes this numerical distribution with a given step size.
     *
     * @param unitStep the numerical integration step size, in units of the
     *                 standard deviation of the distribution.
     *
     * @param threshold the threshold for the cumulative distribution function.
     */
    protected NumericalRealDistribution(double unitStep, double threshold) {
        this.unitStep = unitStep;
        this.threshold = threshold;
    }

    @Override
    public double cdf(double x) {
        return cdf.get().applyAsDouble(x);
    }

    private NumericalCDF createCDF() {
        return NumericalCDF.create(this, unitStep, threshold);
    }
}
