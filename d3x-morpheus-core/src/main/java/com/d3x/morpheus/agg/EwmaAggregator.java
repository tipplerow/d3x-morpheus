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

import com.d3x.morpheus.filter.EWMAFilter;

import com.google.gson.stream.JsonWriter;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;

/**
 * Aggregates time-series data using an exponentially-weighted moving average.
 *
 * @author Scott Shaffer
 */
public class EwmaAggregator extends TsFilterAggregator {
    /**
     * The half-life for the exponential decay of the weights.
     */
    @Getter
    private final double halfLife;

    /**
     * Creates a new EWMA aggregator using the specified parameters.
     *
     * @param windowLen   the length of the aggregation window.
     * @param halfLife    the half-life for the exponential decay of the weights.
     * @param renormalize whether to renormalize the coefficients in the presence of missing values.
     * @param nanPolicy   the missing value policy to apply.
     */
    public EwmaAggregator(int windowLen,
                          double halfLife,
                          boolean renormalize,
                          @NonNull NanPolicy nanPolicy) {
        super(AggregatorType.EWMA, nanPolicy, EWMAFilter.computeWeights(halfLife, windowLen), renormalize);
        this.halfLife = halfLife;
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
    public static EwmaAggregator create(int windowLen,
                                        @NonNull NanPolicy nanPolicy,
                                        @NonNull Map<String, Object> paramMap) {
        var halfLife = (Double) paramMap.get(AggregatorJson.HALF_LIFE);
        var renormalize = (Boolean) paramMap.getOrDefault(AggregatorJson.RENORMALIZE, true);

        if (halfLife != null) {
            return new EwmaAggregator(windowLen, halfLife, renormalize, nanPolicy);
        }
        else {
            throw new IllegalArgumentException("Missing half-life parameter.");
        }
    }

    @Override
    public EwmaAggregator validate() {
        super.validate();
        EWMAFilter.validateHalfLife(halfLife);
        return this;
    }

    @Override
    protected void writeBody(@NonNull JsonWriter writer) throws IOException {
        super.writeBody(writer);
        writer.name(AggregatorJson.HALF_LIFE);
        writer.value(halfLife);
    }
}
