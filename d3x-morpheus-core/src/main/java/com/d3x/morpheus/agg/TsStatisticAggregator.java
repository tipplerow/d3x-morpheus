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

import com.d3x.morpheus.stats.Statistic1;
import com.d3x.morpheus.vector.D3xVectorView;

import lombok.NonNull;

import java.util.function.Supplier;

/**
 * Aggregates time series data using a univariate statistic.
 *
 * @author Scott Shaffer
 */
public class TsStatisticAggregator extends TimeSeriesAggregator {
    // Returns a new instance of the aggregator statistic...
    private final Supplier<Statistic1> supplier;

    /**
     * Creates a new statistical aggregator using the specified parameters.
     *
     * @param aggType   the enumerated aggregation type.
     * @param windowLen the length of the aggregation window.
     * @param nanPolicy the missing value policy to apply.
     */
    public TsStatisticAggregator(@NonNull AggregatorType aggType,
                                 @NonNull NanPolicy nanPolicy,
                                 int windowLen) {
        super(aggType, nanPolicy, windowLen);
        this.supplier = aggType.getStatistic();
    }

    @Override
    public double apply(@NonNull D3xVectorView series) {
        validate(series);
        return StatisticAggregator.aggregate(series.iterator(), supplier.get(), nanPolicy);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && equalsAgg((TsStatisticAggregator) other);
    }

    private boolean equalsAgg(TsStatisticAggregator that) {
        return this.windowLen == that.windowLen;
    }
}
