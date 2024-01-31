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

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.vector.D3xVectorView;
import com.d3x.morpheus.vector.DataVector;
import com.d3x.morpheus.vector.DataVectorElement;
import com.d3x.morpheus.vector.DataVectorView;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import lombok.NonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Provides a base class for mathematical operations that reduce a
 * cross-section (unordered collection) of numerical observations
 * into a single numerical value.
 *
 * @author Scott Shaffer
 */
public abstract class CrossSectionAggregator extends VectorAggregator {
    /**
     * Creates a new aggregator using the specified parameters.
     *
     * @param aggType   the enumerated aggregation type.
     * @param nanPolicy the missing value policy to apply.
     */
    protected CrossSectionAggregator(@NonNull AggregatorType aggType, @NonNull NanPolicy nanPolicy) {
        super(aggType, nanPolicy);
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
    public static CrossSectionAggregator read(@NonNull JsonReader reader) throws IOException {
        return AggregatorJsonReader.readCrossSectionAggregator(reader);
    }

    /**
     * Aggregates a collection of observations.
     *
     * @param iterator an iterator over the observations.
     *
     * @return the aggregated value.
     */
    public abstract double apply(Iterator<Double> iterator);

    /**
     * Aggregates a collection of observations.
     *
     * @param observations the observations to aggregate.
     *
     * @return the aggregated value.
     */
    public double apply(@NonNull Collection<Double> observations) {
        return apply(observations.iterator());
    }

    /**
     * Aggregates a vector of observations.
     *
     * @param vector a vector of observations (unchanged).
     *
     * @return the aggregated value.
     */
    public double apply(@NonNull DataVectorView<?> vector) {
        return apply(vector.streamValues().iterator());
    }

    /**
     * Applies this aggregator to each row of a data frame.
     *
     * @param frame the data frame to aggregate (unchanged).
     *
     * @return a data vector of aggregated row values.
     */
    public <R,C> DataVector<R> byrow(@NonNull DataFrame<R,C> frame) {
        return DataVector.collect(
                frame.rows().stream().map(row -> DataVectorElement.of(row.key(), apply(row))
        ));
    }

    /**
     * Applies this aggregator to each column of a data frame.
     *
     * @param frame the data frame to aggregate (unchanged).
     *
     * @return a data vector of aggregated column values.
     */
    public <R,C> DataVector<C> bycol(@NonNull DataFrame<R,C> frame) {
        return DataVector.collect(
                frame.cols().stream().map(col -> DataVectorElement.of(col.key(), apply(col))
        ));
    }

    @Override
    public double apply(@NonNull D3xVectorView vector) {
        return apply(vector.iterator());
    }

    @Override
    protected void writeBody(@NonNull JsonWriter writer) throws IOException {
        writer.name(AggregatorJson.NAN_POLICY);
        nanPolicy.write(writer);
    }
}
