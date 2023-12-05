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

import lombok.Getter;
import lombok.NonNull;

import static org.apache.commons.math3.special.Beta.regularizedBeta;
import static org.apache.commons.math3.util.CombinatoricsUtils.factorialLog;

import java.util.random.RandomGenerator;

/**
 * Implements the binomial probability distribution.
 *
 * @author Scott Shaffer
 */
public class BinomialDistribution extends AbstractDiscreteDistribution {
    /**
     * The number of trials.
     */
    @Getter
    private final int n;

    /**
     * The probability of success on each trial.
     */
    @Getter
    private final double p;

    /**
     * The probability of failure on each trial.
     */
    @Getter
    private final double q;

    private final double mean;
    private final double sdev;
    private final IntSupport support;

    /**
     * Constructs a binomial distribution.
     *
     * @param n the number of trials.
     * @param p the probability of success on each trial.
     */
    public BinomialDistribution(int n, double p) {
        validateN(n);
        validateP(p);

        this.n = n;
        this.p = p;
        this.q = 1.0 - p;

        this.mean = n * p;
        this.sdev = Math.sqrt(n * p * q);
        this.support = IntSupport.over(0, n);
    }

    /**
     * Computes the cumulative distribution function for a binomial distribution.
     *
     * @param k the number of successes.
     * @param n the number of trials.
     * @param p the probability of success on each trial.
     *
     * @return the cumulative distribution function for the input parameters.
     */
    public static double cdf(int k, int n, double p) {
        validateN(n);
        validateP(p);

        if (k < 0) {
            return 0.0;
        }
        else if (k <= n) {
            return regularizedBeta(1.0 - p, n - k, k + 1);
        }
        else {
            return 1.0;
        }
    }

    /**
     * Computes the probability mass function for a binomial distribution.
     *
     * @param k the number of successes.
     * @param n the number of trials.
     * @param p the probability of success on each trial.
     *
     * @return the probability mass function for the input parameters.
     */
    public static double pmf(int k, int n, double p) {
        validateN(n);
        validateP(p);

        if (k < 0 || k > n) {
            return 0.0;
        }
        else {
            var logP = k * Math.log(p) + (n - k) * Math.log(1.0 - p);
            var logC = factorialLog(n) - factorialLog(k) - factorialLog(n - k);
            return Math.exp(logP + logC);
        }
    }

    /**
     * Conducts a hypothesis test using this distribution as the null.
     *
     * @param actual the actual number of successful trials.
     *
     * @return the probability with which the null hypothesis (that this
     * distribution is the true distribution) is rejected.
     */
    public double test(int actual) {
        if (actual < 0 || actual > n)
            throw new IllegalArgumentException("Invalid number of successful trials.");

        var expected = (int) Math.round(n * p);
        var deviation = Math.abs(actual - expected);

        if (deviation == 0)
            return 0.0;

        var k1 = Math.max(0, expected - deviation + 1);
        var k2 = Math.min(n, expected + deviation - 1);

        return cdf(IntSupport.over(k1, k2));
    }

    @Override
    public double cdf(int k) {
        return cdf(k, n, p);
    }

    @Override
    public double pmf(int k) {
        return pmf(k, n, p);
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double sdev() {
        return sdev;
    }

    @Override
    public int sample(@NonNull RandomGenerator generator) {
        var sum = 0;

        for (var i = 0; i < n; ++i)
            if (generator.nextDouble() < p)
                ++sum;

        return sum;
    }

    @Override
    public IntSupport support() {
        return support;
    }

    private static void validateN(int n) {
        if (n < 0)
            throw new IllegalArgumentException("Number of trials must be non-negative.");
    }

    private static void validateP(double p) {
        if (p < 0.0 || p > 1.0)
            throw new IllegalArgumentException("Probability must be between 0 and 1.");
    }
}
