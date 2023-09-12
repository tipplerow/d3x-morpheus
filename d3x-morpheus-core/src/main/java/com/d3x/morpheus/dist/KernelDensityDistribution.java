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

import com.d3x.morpheus.stats.StatSummary;
import com.d3x.morpheus.util.DoubleInterval;
import com.d3x.morpheus.vector.D3xVectorView;

import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.random.RandomGenerator;

/**
 * Implements a kernel density estimator distribution.
 *
 * @author Scott Shaffer
 */
public class KernelDensityDistribution extends AbstractRealDistribution {
    /**
     * The sorted data sample.
     */
    private final double[] sample;

    /**
     * The kernel distribution type.
     */
    @Getter
    @NonNull
    private final KernelType kernelType;

    /**
     * The kernel distribution function.
     */
    @Getter
    @NonNull
    private final RealDistribution kernelFunc;

    /**
     * The smoothing bandwidth.
     */
    @Getter
    private final double bandwidth;

    /**
     * A statistical summary of the data sample.
     */
    @Getter
    @NonNull
    private final StatSummary summary;

    // Convenience factors for computing CDF and PDF...
    private final double invH;  // 1.0 / bandwidth
    private final double invN;  // 1.0 / sample.length
    private final double invNH; // 1.0 / (sample.length * bandwidth)

    // Scale factor when sampling from the kernel distribution to
    // generate random deviates...
    private final double sampleScale;

    /**
     * The default kernel function.
     */
    public static final KernelType DEFAULT_KERNEL = KernelType.EPANECHNIKOV;

    /**
     * Creates a new density estimate for a given data sample using the
     * default kernel function and bandwidth.
     *
     * @param sample the empirical data sample.
     *
     * @throws RuntimeException if the sample has fewer than two elements.
     */
    public KernelDensityDistribution(double[] sample) {
        this(sample, DEFAULT_KERNEL, Double.NaN);
    }

    /**
     * Creates a new density estimate for a given data sample using a
     * particular kernel function and the default bandwidth.
     *
     * @param sample the empirical data sample.
     * @param kernel the kernel function.
     *
     * @throws RuntimeException if the sample has fewer than two elements.
     */
    public KernelDensityDistribution(double[] sample, @NonNull KernelType kernel) {
        this(sample, kernel, Double.NaN);
    }

    /**
     * Creates a new density estimate for a given data sample using a
     * particular kernel function and bandwidth.
     *
     * @param sample    the empirical data sample.
     * @param kernel    the kernel function.
     * @param bandwidth the kernel function bandwidth.
     *
     * @throws RuntimeException unless the sample as two or more elements
     * and the bandwidth is positive.
     */
    public KernelDensityDistribution(double[] sample, @NonNull KernelType kernel, double bandwidth) {
        validateSample(sample);
        this.kernelType = kernel;
        this.kernelFunc = kernel.kernel();

        this.sample = copyAndSort(sample);
        this.summary = StatSummary.of(sample);
        this.bandwidth = resolveBandwidth(bandwidth);

        this.invH = 1.0 / this.bandwidth;
        this.invN = 1.0 / sample.length;
        this.invNH = invH * invN;
        this.sampleScale = this.bandwidth * kernelFunc.sdev();
    }

    /**
     * Returns a view of the sorted data sample.
     * @return a view of the sorted data sample.
     */
    public D3xVectorView getSample() {
        return D3xVectorView.of(sample);
    }

    @Override
    public double cdf(double x) {
        return invN * sum(x, kernelFunc::cdf);
    }

    @Override
    public double pdf(double x) {
        return invNH * sum(x, kernelFunc::pdf);
    }

    @Override
    public double quantile(double F) {
        return invertCDF(F);
    }

    @Override
    public double mean() {
        return summary.getMean();
    }

    @Override
    public double mode() {
        return Double.NaN;
    }

    @Override
    public double sdev() {
        return summary.getSD();
    }

    @Override
    public double sample(@NonNull RandomGenerator generator) {
        // Pick an observation at random, then add a sample from the kernel function
        // scaled to have standard deviation equal to the bandwidth...
        var index = generator.nextInt(sample.length);
        var delta = sampleScale * kernelFunc.sample(generator);
        return sample[index] + delta;
    }

    @Override
    public DoubleInterval support() {
        // The probability density extends to the support interval of
        // the kernel functions. Use half the width because the kernel
        // functions are centered on the observations...
        var sampleMin = summary.getMin();
        var sampleMax = summary.getMax();
        var kernelWidth = bandwidth * kernelFunc.support().getWidth();
        return DoubleInterval.closed(sampleMin - 0.5 * kernelWidth, sampleMax + 0.5 * kernelWidth);
    }

    @Override
    public double variance() {
        return summary.getVariance();
    }

    private static double[] copyAndSort(double[] sample) {
        var sorted = Arrays.copyOf(sample, sample.length);
        Arrays.sort(sorted);
        return sorted;
    }

    private static void validateSample(double[] sample) {
        if (sample.length < 2)
            throw new IllegalArgumentException("At least two observations are required.");
    }

    private double resolveBandwidth(double bandwidth) {
        if (bandwidth <= 0.0) {
            throw new IllegalArgumentException("Bandwidth must be positive.");
        }
        else if (Double.isNaN(bandwidth)) {
            return kernelType.bandwidth(summary);
        }
        else {
            return bandwidth;
        }
    }

    private double sum(double x, DoubleUnaryOperator operator) {
        return Arrays.stream(sample).parallel().map(xi -> invH * (x - xi)).map(operator).sum();
    }
}
