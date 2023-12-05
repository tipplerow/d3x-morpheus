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
package com.d3x.morpheus.stats;

import com.d3x.morpheus.vector.D3xVectorView;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.Arrays;

/**
 * Collects summary statistics for a data sample.
 *
 * @author Scott Shaffer
 */
@Value
@Builder(access = AccessLevel.PRIVATE)
public class StatSummary {
    /**
     * The sample size.
     */
    int count;

    /**
     * The minimum sample value.
     */
    double min;

    /**
     * The first quartile value.
     */
    double Q1;

    /**
     * The sample mean.
     */
    double mean;

    /**
     * The sample median.
     */
    double median;

    /**
     * The third quartile value.
     */
    double Q3;

    /**
     * The maximum sample value.
     */
    double max;

    /**
     * The sample variance.
     */
    double variance;

    /**
     * Creates a summary for a given sample.
     *
     * @param sample the empirical data sample.
     *
     * @return a summary for the given sample.
     */
    public static StatSummary of(int... sample) {
        return of(Arrays.stream(sample).asDoubleStream().toArray());
    }

    /**
     * Creates a summary for a given sample.
     *
     * @param sample the empirical data sample.
     *
     * @return a summary for the given sample.
     */
    public static StatSummary of(double... sample) {
        var count = sample.length;

        var min = Statistic1.compute(Min::new, sample);
        var max = Statistic1.compute(Max::new, sample);
        var mean = Statistic1.compute(Mean::new, sample);
        var variance = Statistic1.compute(Variance::new, sample);

        var percentile = new Percentile();
        percentile.setData(sample);

        var Q1 = percentile.evaluate(25.0);
        var Q3 = percentile.evaluate(75.0);
        var median = percentile.evaluate(50.0);

        return builder()
                .Q1(Q1)
                .Q3(Q3)
                .min(min)
                .max(max)
                .mean(mean)
                .count(count)
                .median(median)
                .variance(variance)
                .build();
    }

    /**
     * Creates a summary for a given sample.
     *
     * @param sample the empirical data sample.
     *
     * @return a summary for the given sample.
     */
    public static StatSummary of(@NonNull D3xVectorView sample) {
        return of(sample.toArray());
    }

    /**
     * Returns the interquartile range for the sample.
     * @return the interquartile range for the sample.
     */
    public double getIQR() {
        return Q3 - Q1;
    }

    /**
     * Returns the sample standard deviation.
     * @return the sample standard deviation.
     */
    public double getSD() {
        return Math.sqrt(variance);
    }
}
