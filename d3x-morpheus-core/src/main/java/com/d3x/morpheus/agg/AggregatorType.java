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

import com.d3x.morpheus.stats.GeoMean;
import com.d3x.morpheus.stats.Mean;
import com.d3x.morpheus.stats.Median;
import com.d3x.morpheus.stats.Product;
import com.d3x.morpheus.stats.Statistic1;
import com.d3x.morpheus.stats.Sum;

import java.util.function.Supplier;

/**
 * Enumerates the types of vector aggregators supported by the Morpheus library.
 *
 * @author Scott Shaffer
 */
public enum AggregatorType {
    /**
     * Exponentially-weighted moving average.
     */
    EWMA,

    /**
     * Linearly-weighted moving average.
     */
    LWMA,

    /**
     * Geometric mean.
     */
    GEO_MEAN(GeoMean::new),

    /**
     * Equally-weighted average.
     */
    MEAN(Mean::new),

    /**
     * Equally-weighted median.
     */
    MEDIAN(Median::new),

    /**
     * The component product.
     */
    PRODUCT(Product::new),

    /**
     * The component sum.
     */
    SUM(Sum::new),

    /**
     * An aggregated collection of other time-series aggregators.
     */
    COMPOSITE;

    /**
     * Returns a supplier of the univariate statistic associated with this
     * aggregator type.
     *
     * @return a supplier of the univariate statistic associated with this
     * aggregator type.
     *
     * @throws UnsupportedOperationException if the type does not support
     * aggregation via univariate statistics.
     */
    public Supplier<Statistic1> getStatistic() {
        if (statistic != null)
            return statistic;
        else
            throw new UnsupportedOperationException("Aggregator type does not support univariate statistics.");
    }

    /**
     * Identifies aggregator types that are based on univariate statistics.
     *
     * @return {@code true} if the aggregator type is based on a univariate
     * statistic; otherwise, {@code false}.
     */
    public boolean isStatType() {
        return statistic != null;
    }

    private final Supplier<Statistic1> statistic;

    AggregatorType() {
        this(null);
    }

    AggregatorType(Supplier<Statistic1> statistic) {
        this.statistic = statistic;
    }
}
