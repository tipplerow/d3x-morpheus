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

import com.d3x.morpheus.filter.LWMAFilter;

import lombok.NonNull;

import java.util.Map;

/**
 * Aggregates time-series data using an exponentially-weighted moving average.
 *
 * @author Scott Shaffer
 */
public class LwmaAggregator extends TsFilterAggregator {
    /**
     * Creates a new LWMA aggregator using the specified parameters.
     *
     * @param windowLen   the length of the aggregation window.
     * @param renormalize whether to renormalize the coefficients in the presence of missing values.
     * @param nanPolicy   the missing value policy to apply.
     */
    public LwmaAggregator(int windowLen,
                          boolean renormalize,
                          @NonNull NanPolicy nanPolicy) {
        super(AggregatorType.LWMA, nanPolicy, LWMAFilter.computeWeights(windowLen), renormalize);
    }

    /**
     * Creates a new EWMA aggregator using the specified parameters.
     *
     * <p>The parameter map must contain the half-life as an entry with
     * the key {@code halfLife}. The entry {@code renormalize} may also
     * be present to specify the renormalization flag; it will default
     * to {@code true} if missing.</p>
     *
     * @param windowLen the length of the aggregation window.
     * @param nanPolicy the missing value policy to apply.
     * @param paramMap  the JSON-encoded filter parameters.
     *
     * @return a new EWMA aggregator given the specified parameters.
     *
     * @throws RuntimeException unless the parameter map includes the half-life.
     */
    public static LwmaAggregator create(int windowLen,
                                        @NonNull NanPolicy nanPolicy,
                                        @NonNull Map<String, Object> paramMap) {
        var renormalize = (Boolean) paramMap.getOrDefault(AggregatorJson.RENORMALIZE, true);
        return new LwmaAggregator(windowLen, renormalize, nanPolicy);
    }
}
