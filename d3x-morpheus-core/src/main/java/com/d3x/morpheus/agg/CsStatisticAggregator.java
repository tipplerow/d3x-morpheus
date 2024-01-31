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

import lombok.NonNull;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Aggregates cross-sectional data using a univariate statistic.
 *
 * @author Scott Shaffer
 */
public class CsStatisticAggregator extends CrossSectionAggregator {
    // Returns a new instance of the aggregator statistic...
    private final Supplier<Statistic1> supplier;

    /**
     * Creates a new aggregator using the specified parameters.
     *
     * @param aggType   the enumerated aggregation type.
     * @param nanPolicy the missing value policy to apply.
     */
    public CsStatisticAggregator(@NonNull AggregatorType aggType, @NonNull NanPolicy nanPolicy) {
        super(aggType, nanPolicy);
        this.supplier = aggType.getStatistic();
    }

    @Override
    public double apply(@NonNull Iterator<Double> iterator) {
        return StatisticAggregator.aggregate(iterator, supplier.get(), nanPolicy);
    }
}
