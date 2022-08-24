/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.stats;

import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

/**
 * An interface to an object that provides summary statistics on itself.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public interface Stats<T> {

    /**
     * Returns the number of non null values
     * @return      the number of non null values
     */
    T count();

    /**
     * Returns the minimum value for this entity
     * @return      the minimum value
     * @see <a href="https://en.wikipedia.org/wiki/Sample_maximum_and_minimum">Wikipedia</a>
     */
    T min();

    /**
     * Returns the maximum value for this entity
     * @return      the maximum value
     * @see <a href="https://en.wikipedia.org/wiki/Sample_maximum_and_minimum">Wikipedia</a>
     */
    T max();

    /**
     * Returns the arithmetic mean value for this entity
     * @return      the mean value
     * @see <a href="http://en.wikipedia.org/wiki/Arithmetic_mean">Wikipedia</a>
     */
    T mean();

    /**
     * Returns the mean absolute value for this entity
     * @return      the mean value
     * @see <a href="http://en.wikipedia.org/wiki/Arithmetic_mean">Wikipedia</a>
     */
    T meanAbs();

    /**
     * Returns the median value for this entity
     * @return      the median value
     * @see <a href="http://en.wikipedia.org/wiki/Median">Wikipedia</a>
     */
    T median();

    /**
     * Returns the Mean Absolute Deviation for this entity
     * @return  the Mean Absolute Deviation
     * @see <a href="https://en.wikipedia.org/wiki/Average_absolute_deviation">Wikipedia</a>
     */
    T mad();

    /**
     * Returns the sample standard deviation for this entity
     * @return      the sample standard deviation
     * @see <a href="http://en.wikipedia.org/wiki/Standard_deviation">Wikipedia</a>
     */
    T stdDev();

    /**
     * Returns the Standard Error of the Mean for this entity
     * @return  the Standard Error of the Mean
     * @see <a href="https://en.wikipedia.org/wiki/Standard_error">Wikipedia</a>
     */
    T sem();

    /**
     * Returns the sum of values for this entity
     * @return      the sum
     * @see <a href="http://en.wikipedia.org/wiki/Sum">Wikipedia</a>
     */
    T sum();

    /**
     * Returns the sum of the absolute values for this entity
     * @return      the sum of absolute values
     * @see <a href="http://en.wikipedia.org/wiki/Sum">Wikipedia</a>
     */
    T sumAbs();

    /**
     * Returns the sum of the logs for this entity
     * @return      the sum of logs
     * @see <a href="http://en.wikipedia.org/wiki/Sum">Wikipedia</a>
     */
    T sumLogs();

    /**
     * Returns the sum of the squares for this entity
     * @return      the sum of squares
     * @see <a href="http://en.wikipedia.org/wiki/Sum">Wikipedia</a>
     */
    T sumSquares();

    /**
     * Returns the sample variance for this entity
     * @return      the sample variance
     * @see <a href="http://en.wikipedia.org/wiki/Variance">Wikipedia</a>
     */
    T variance();

    /**
     * Returns the Kurtosis for this entity
     * @return      the kurtosis
     * @see <a href="http://en.wikipedia.org/wiki/Kurtosis">Wikipedia</a>
     */
    T kurtosis();

    /**
     * Returns the skewness for this entity
     * @return      the skewness
     * @see <a href="http://en.wikipedia.org/wiki/Skewness">Wikipedia</a>
     */
    T skew();

    /**
     * Returns the geometric mean for this entity
     * @return  the geometric mean, NaN if the product of the available values is less than or equal to 0.
     * @see <a href="http://en.wikipedia.org/wiki/Geometric_mean">Wikipedia</a>
     */
    T geoMean();

    /**
     * Returns the product for this entity
     * @return  the geometric mean, NaN if the product of the available values is less than or equal to 0.
     * @see <a href="http://en.wikipedia.org/wiki/Geometric_mean">Wikipedia</a>
     */
    T product();

    /**
     * Returns the auto correlation for this entity
     * @param lag   the number of periods to lag
     * @return      the auto correlation statistic
     */
    T autocorr(int lag);

    /**
     * Returns an estimate for the nth percentile for this entity
     * @param nth   the requested percentile (scaled from 0 - 100)
     * @return      estimate for the nth percentile of row or column
     * @see <a href="http://en.wikipedia.org/wiki/Percentile">Wikipedia</a>
     */
    T percentile(double nth);


    /**
     * Returns summary stats for a sample
     * @param sample    the sample reference
     * @return          the summary stats
     */
    static Stats<Double> of(Sample sample) {
        return new Basic(stat -> {
            var size = sample.size();
            for (int i=0; i<size; ++i) {
                var value = sample.getDoubleAt(i);
                if (!Double.isNaN(value)) {
                    stat.add(value);
                }
            }
            return stat.getValue();
        });
    }


    /**
     * Returns summary stats for a supplier of double streams
     * @param supplier  the stream supplier
     * @return          the summary stats
     */
    static Stats<Double> of(Supplier<DoubleStream> supplier) {
        return new Basic(stat -> {
            var doubles = supplier.get();
            doubles.forEach(v -> {
                if (!Double.isNaN(v)) {
                    stat.add(v);
                }
            });
            return stat.getValue();
        });
    }


    /**
     * A convenience interface for Stats implementations
     */
    @lombok.AllArgsConstructor()
    class Basic implements Stats<Double> {

        @lombok.NonNull
        private ToDoubleFunction<Statistic1> compute;

        @Override
        public Double count() {
            return compute.applyAsDouble(new Count());
        }
        @Override
        public Double min() {
            return compute.applyAsDouble(new Min());
        }
        @Override
        public Double max() {
            return compute.applyAsDouble(new Max());
        }
        @Override
        public Double mean() {
            return compute.applyAsDouble(new Mean());
        }
        @Override
        public Double meanAbs() {
            return compute.applyAsDouble(new MeanAbs());
        }
        @Override
        public Double median() {
            return compute.applyAsDouble(new Median());
        }
        @Override
        public Double mad() {
            return compute.applyAsDouble(new MeanAbsDev());
        }
        @Override
        public Double stdDev() {
            return compute.applyAsDouble(new StdDev(true));
        }
        @Override
        public Double sem() {
            return compute.applyAsDouble(new StdErrorMean());
        }
        @Override
        public Double sum() {
            return compute.applyAsDouble(new Sum());
        }
        @Override
        public Double sumAbs() {
            return compute.applyAsDouble(new SumAbs());
        }
        @Override
        public Double sumLogs() {
            return compute.applyAsDouble(new SumLogs());
        }
        @Override
        public Double sumSquares() {
            return compute.applyAsDouble(new SumSquares());
        }
        @Override
        public Double variance() {
            return compute.applyAsDouble(new Variance(true));
        }
        @Override
        public Double kurtosis() {
            return compute.applyAsDouble(new Kurtosis());
        }
        @Override
        public Double skew() {
            return compute.applyAsDouble(new Skew());
        }
        @Override
        public Double geoMean() {
            return compute.applyAsDouble(new GeoMean());
        }
        @Override
        public Double product() {
            return compute.applyAsDouble(new Product());
        }
        @Override
        public Double autocorr(int lag) {
            return compute.applyAsDouble(new AutoCorrelation(lag));
        }
        @Override
        public Double percentile(double nth) {
            return compute.applyAsDouble(new Percentile(nth));
        }
    }

}
