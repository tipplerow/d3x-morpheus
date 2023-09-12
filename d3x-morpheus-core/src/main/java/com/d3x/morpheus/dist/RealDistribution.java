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

import lombok.NonNull;

import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;

/**
 * Represents a univariate probability distribution over the real numbers.
 *
 * @author Scott Shaffer
 */
public interface RealDistribution {
    /**
     * Computes the cumulative distribution function at a point.  For
     * a random variable {@code X} drawn from this distribution, this
     * method returns the probability {@code P(X <= x)}.
     *
     * @param x the point at which the CDF is evaluated.
     *
     * @return the probability that a random variable drawn from this
     * distribution is less than or equal to {@code x}.
     */
    double cdf(double x);

    /**
     * Computes the probability density function at a point.
     *
     * @param x the point at which the PDF is evaluated.
     *
     * @return the probability density at {@code x}.
     */
    double pdf(double x);

    /**
     * Computes the quantile (inverse CDF) function.
     *
     * <p>The quantile function {@code Q(F)} is the point which the
     * cumulative distribution function takes value {@code F}.
     *
     * @param F the cumulative probability.
     *
     * @return the quantile (inverse CDF) function evaluated at the
     * given cumulative probability.
     *
     * @throws RuntimeException unless F is a fractional value.
     */
    double quantile(double F);

    /**
     * Returns the mean value of this distribution.
     * @return the mean value of this distribution.
     */
    double mean();

    /**
     * Returns the mode of this distribution (NaN if not unimodal).
     * @return the mode of this distribution (NaN if not unimodal).
     */
    double mode();

    /**
     * Returns the standard deviation this distribution.
     * @return the standard deviation of this distribution.
     */
    double sdev();

    /**
     * Samples from this distribution.
     *
     * @param generator the source of uniform random deviates.
     *
     * @return the next value from this distribution.
     */
    double sample(RandomGenerator generator);

    /**
     * Returns the probability distribution that describes the sum of
     * independent and identically distributed random variables drawn
     * from this distribution.
     *
     * @param count the number of random variables in the sum.
     *
     * @return the sum distribution.
     */
    RealDistribution sum(int count);

    /**
     * Returns the range of non-zero probability density.
     * @return the range of non-zero probability density.
     */
    DoubleInterval support();

    /**
     * Computes the cumulative distribution function for a range.
     *
     * @param interval the observation interval.
     *
     * @return the probability that a random variable drawn from this
     * distribution lies in the open range {@code (lower, upper]}.
     */
    default double cdf(DoubleInterval interval) {
        return cdf(interval.getUpper()) - cdf(interval.getLower());
    }

    /**
     * Returns the interquartile range of this distribution.
     * @return the interquartile range of this distribution.
     */
    default double IQR() {
        return quantile(0.75) - quantile(0.25);
    }

    /**
     * Returns the median value of this distribution.
     * @return the median value of this distribution.
     */
    default double median() {
        return quantile(0.5);
    }

    /**
     * Samples from this unimodal distribution using the rejection method.
     *
     * @param generator the source of uniform random deviates.
     *
     * @return the next value from this distribution computed via the
     * rejection method.
     *
     * @throws RuntimeException unless this is a unimodal distribution
     * over a finite support interval.
     */
    default double reject(@NonNull RandomGenerator generator) {
        // Let the trial distribution be uniform over the finite interval
        // of support [L, U] for this distribution.  The likelihood ratio
        // bound "M" must be chosen so that f(x) <= M * g(x) for all "x"
        // in [L, U], where f(x) is the density for this distribution and
        // g(x) = 1 / (U - L) is the density for the trial distribution.
        // The minimum value of "M" so satisfy the requirement can be
        // computed at the modal position: f(mode) = M / (U - L), so then
        // M = (U - L) * f(mode).
        if (!support().isFinite())
            throw new UnsupportedOperationException("Support interval must be finite for rejection sampling.");

        if (!Double.isFinite(mode()))
            throw new UnsupportedOperationException("Distribution is not unimodal.");

        var boundM = support().getWidth() * pdf(mode());
        var trialDist = new UniformDistribution(support());
        return reject(generator, trialDist, boundM);
    }

    /**
     * Samples from this distribution using the rejection method.
     *
     * @param generator the source of uniform random deviates.
     * @param trialDist the trial distribution.
     * @param boundM    the "big M" likelihood-ratio bound.
     *
     * @return the next value from this distribution computed via the
     * rejection method.
     */
    default double reject(@NonNull RandomGenerator generator,
                          @NonNull RealDistribution trialDist,
                          double boundM) {
        if (!trialDist.support().contains(this.support()))
            throw new IllegalArgumentException("Trial distribution does not span this distribution.");

        if (boundM <= 1.0)
            throw new IllegalArgumentException("Likelihood ratio bound must be positive.");

        // The rejection method of John von Neumann for sampling from
        // this distribution X with density function "f" using the trial
        // distribution Y with density function "g"
        //
        // (1) Draw a sample "y" from the trial distribution Y.
        // (2) Draw a sample "u" from the uniform distribution Unif(0, 1).
        // (3) If u < f(y) / (M * g(y)), accept y as a sample drawn from f.
        // (4) Else reject y and return to step (1).
        //
        // On average, a value is accepted after M steps.
        var maxIter = (int) (1000 * boundM);

        for (var iter = 0; iter < maxIter; ++iter) {
            var unif = generator.nextDouble();
            var trial = trialDist.sample(generator);
            var fy = pdf(trial);
            var gy = trialDist.pdf(trial);

            if (boundM * gy * unif < fy)
                return trial;
        }

        throw new IllegalStateException("Rejection sampling failed.");
    }

    /**
     * Samples from this distribution.
     *
     * @param generator the source of uniform random deviates.
     * @param count     the number of samples to generate.
     *
     * @return the next {@code count} values from this distribution as
     * an array.
     *
     * @throws RuntimeException if the count is negative.
     */
    default double[] sample(@NonNull RandomGenerator generator, int count) {
        var samples = new double[count];

        for (int index = 0; index < count; ++index)
            samples[index] = sample(generator);

        return samples;

    }

    /**
     * Samples from this distribution.
     *
     * @param generator the source of uniform random deviates.
     * @param count     the number of samples to generate.
     *
     * @return the next {@code count} values from this distribution as
     * a stream.
     *
     * @throws RuntimeException if the count is negative.
     */
    default DoubleStream stream(@NonNull RandomGenerator generator, int count) {
        return DoubleStream.generate(() -> sample(generator)).limit(count);
    }

    /**
     * Samples from this distribution using the transformation method:
     * the sample is the quantile function evaluated at a point sampled
     * from the uniform distribution on {@code [0, 1]}.
     *
     * @param generator the source of uniform random deviates.
     *
     * @return the next value from this distribution computed via the
     * transformation method.
     */
    default double transform(@NonNull RandomGenerator generator) {
        return quantile(generator.nextDouble());
    }

    /**
     * Returns the variance of this distribution.
     * @return the variance of this distribution.
     */
    default double variance() {
        var sdev = sdev();
        return sdev * sdev;
    }
}
