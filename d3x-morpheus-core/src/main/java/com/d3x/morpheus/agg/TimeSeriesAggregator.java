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

import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVectorView;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

/**
 * Provides a base class for mathematical operations that reduce a time series
 * of observations to a single numeric value.
 *
 * @author Scott Shaffer
 */
public abstract class TimeSeriesAggregator extends VectorAggregator {
    /**
     * The length of the aggregation window (the number of observations that
     * are aggregated).
     */
    @Getter
    protected final int windowLen;

    /**
     * Creates a new aggregator using the specified parameters.
     *
     * @param aggType   the enumerated aggregation type.
     * @param nanPolicy the missing value policy to apply.
     * @param windowLen the length of the aggregation window.
     */
    protected TimeSeriesAggregator(@NonNull AggregatorType aggType,
                                   @NonNull NanPolicy nanPolicy,
                                   int windowLen) {
        super(aggType, nanPolicy);
        this.windowLen = windowLen;
    }

    /**
     * Reads an aggregator object from a JSON stream.
     *
     * @param reader the JSON reader to read from.
     *
     * @return the aggregator object read from the stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public static TimeSeriesAggregator read(@NonNull JsonReader reader) throws IOException {
        return AggregatorJsonReader.readTimeSeriesAggregator(reader);
    }

    /**
     * Validates this time series aggregator.
     *
     * @return this object, for operator chaining.
     *
     * @throws RuntimeException unless this aggregator is valid.
     */
    public TimeSeriesAggregator validate() {
        int minObs = nanPolicy.getMinObs();

        if (windowLen < 1)
            throw new MorpheusException("Window length [%d] must be positive.", windowLen);

        if (minObs > windowLen)
            throw new MorpheusException(
                    "The minimum number of observations [%d] must not exceed the window length [%d].",
                    minObs, windowLen);

        return this;
    }

    /**
     * Ensures a match between the series length and the window length.
     *
     * @param series the series to validate.
     *
     * @throws MorpheusException unless the series length matches the window length.
     */
    public void validate(@NonNull D3xVectorView series) {
        if (series.length() != windowLen)
            throw new MorpheusException(
                    "The vector length [%d] does not match the window length [%d].",
                    series.length(), windowLen);
    }

    @Override
    protected void writeBody(@NonNull JsonWriter writer) throws IOException {
        writer.name(AggregatorJson.NAN_POLICY);
        nanPolicy.write(writer);
        writer.name(AggregatorJson.WINDOW_LEN);
        writer.value(windowLen);
    }
}
