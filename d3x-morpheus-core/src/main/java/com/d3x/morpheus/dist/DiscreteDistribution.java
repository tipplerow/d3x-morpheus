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

import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * Represents a univariate probability distribution over the integers.
 *
 * @author Scott Shaffer
 */
public interface DiscreteDistribution {
    /**
     * Computes the cumulative distribution function at a point.  For
     * a random variable {@code X} drawn from this distribution, this
     * method returns the probability {@code P(X <= k)}.
     *
     * @param k the point at which the CDF is evaluated.
     *
     * @return the probability that a random variable drawn from this
     * distribution is less than or equal to {@code k}.
     */
    double cdf(int k);

    /**
     * Computes the cumulative distribution function over a range.
     *
     * @param range the range over which the CDF is evaluated.
     *
     * @return the probability that a random variable drawn from this
     * distribution lies within the input range.
     */
    double cdf(IntSupport range);

    /**
     * Computes the probability mass function at a point.
     *
     * @param k the point at which the PMF is evaluated.
     *
     * @return the probability mass at {@code k}.
     */
    double pmf(int k);

    /**
     * Returns the mean value of this distribution.
     * @return the mean value of this distribution.
     */
    double mean();

    /**
     * Returns the standard deviation of this distribution.
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
    int sample(RandomGenerator generator);

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
    int[] sample(RandomGenerator generator, int count);

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
    IntStream stream(RandomGenerator generator, int count);

    /**
     * Returns the range of non-zero probability mass.
     * @return the range of non-zero probability mass.
     */
    IntSupport support();

    /**
     * Returns the variance of this distribution.
     * @return the variance of this distribution.
     */
    double variance();
}
