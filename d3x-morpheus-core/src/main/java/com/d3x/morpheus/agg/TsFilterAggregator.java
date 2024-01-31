/*
 * Copyright 2018-2024, Talos Trading - All Rights Reserved
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
package com.d3x.morpheus.agg;

import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVectorView;

import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

/**
 * Defines one-sided, backward-looking linear convolution filters for
 * univariate time series.
 *
 * <p><b>Filter coefficients:</b> Time-series filters provide a vector
 * of filter coefficients {@code c[0], c[1], ..., c[W - 1]} ordered by
 * time lag, where {@code W} is the window length of the filter.  Given
 * a time series {@code x[0], x[1], ..., x[n]}, the filtered values are
 * {@code y[i] = c[0] * x[i] + c[1] * x[i-1] + ... + c[W-1] * x[i-W+1]}
 * for {@code W - 1 <= i <= n} and {@code y[i] = Double.NaN} for the
 * first {@code 0 <= i < W - 1}.</p>
 *
 * <p>As an example, to implement an exponentially-weighted moving average
 * with a half-life of one day and a lookback window of four days, the
 * filter coefficients should be {@code [1.0, 0.5, 0.25, 0.125]}.</p>
 *
 * @author Scott Shaffer
 */
public abstract class TsFilterAggregator extends TimeSeriesAggregator {
    /**
     * The filter coefficients ordered by time lag.
     */
    @Getter
    protected final D3xVectorView coefficients;

    /**
     * Whether to renormalize the filter coefficients in the presence of
     * missing values.
     */
    @Getter
    protected final boolean renormalize;

    // The sum of the filter coefficients...
    private final double coefficientSum;

    /**
     * Creates a new filter aggregator using the specified parameters.
     *
     * @param aggType      the enumerated aggregation type.
     * @param nanPolicy    the missing value policy to apply.
     * @param coefficients the filter coefficients ordered by time lag.
     * @param renormalize  whether to renormalize the filter coefficients
     *                     in the presence of missing values.
     */
    protected TsFilterAggregator(@NonNull AggregatorType aggType,
                                 @NonNull NanPolicy nanPolicy,
                                 @NonNull D3xVectorView coefficients,
                                 boolean renormalize) {
        super(aggType, nanPolicy, coefficients.length());
        this.renormalize = renormalize;
        this.coefficients = coefficients;
        this.coefficientSum = coefficients.sum();
    }

    @Override
    public double apply(@NonNull D3xVectorView series) {
        validate(series);

        var obsCount = 0;    // Number of terms included in the filter sum
        var filterSum = 0.0; // The sum of filter coefficients times series values
        var coeffExcl = 0.0; // Sum of all excluded filter coefficients, used to renormalize

        var minObs = nanPolicy.getMinObs();
        var failOnNaN = nanPolicy.getType().equals(NanPolicy.Type.NAN);

        // The time-series element index is chronologically increasing,
        // while the filter coefficients are indexed by the lag, which
        // is defined in decreasing chronological order...
        var coeffIndex = coefficients.length() - 1;
        var seriesIndex = 0;

        while (seriesIndex < series.length()) {
            var coeff = coefficients.get(coeffIndex);
            var value = nanPolicy.apply(series.get(seriesIndex));

            if (Double.isFinite(value)) {
                ++obsCount;
                filterSum += coeff * value;
            }
            else if (failOnNaN) {
                // One missing value makes the entire result missing...
                return Double.NaN;
            }
            else {
                // Exclude the missing value...
                coeffExcl += coeff;
            }

            --coeffIndex;
            ++seriesIndex;
        }

        if (obsCount < minObs) {
            return Double.NaN;
        }
        else if (renormalize) {
            // Renormalized filter coefficients...
            return filterSum * coefficientSum / (coefficientSum - coeffExcl);
        }
        else {
            return filterSum;
        }
    }

    @Override
    public TsFilterAggregator validate() {
        super.validate();

        if (coefficients.length() < 1)
            throw new MorpheusException("At least one coefficient is required.");

        if (renormalize && !DoubleComparator.DEFAULT.isPositive(coefficientSum))
            throw new MorpheusException("Renormalization requires a positive coefficient sum.");

        return this;
    }

    @Override
    protected void writeBody(@NonNull JsonWriter writer) throws IOException {
        super.writeBody(writer);
        writer.name(AggregatorJson.RENORMALIZE);
        writer.value(renormalize);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && equalsAgg((TsFilterAggregator) other);
    }

    private boolean equalsAgg(TsFilterAggregator that) {
        return this.renormalize == that.renormalize && this.coefficients.equalsView(that.coefficients);
    }
}
