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

import com.d3x.morpheus.vector.D3xVectorView;

import com.google.gson.stream.JsonWriter;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Provides a base class for mathematical operations that reduce a vector
 * of numerical observations to a single numerical value.
 *
 * @author Scott Shaffer
 */
@RequiredArgsConstructor
public abstract class VectorAggregator {
    /**
     * The enumerated type of this vector aggregator.
     */
    @Getter
    @NonNull
    protected final AggregatorType aggType;

    /**
     * The missing value policy to apply during aggregation.
     */
    @Getter
    @NonNull
    protected final NanPolicy nanPolicy;

    /**
     * Aggregates a numeric vector.
     *
     * @param vector the observations to aggregate.
     *
     * @return the aggregated value.
     */
    public abstract double apply(D3xVectorView vector);

    /**
     * Writes this body of this aggregator (all attributes except the type)
     * to a JSON stream.
     *
     * <p>Subclasses should not call {@code beginObject()} or {@code endObject()};
     * the calling method will.</p>
     *
     * @param writer the JSON writer to write to.
     *
     * @throws IOException if an I/O error occurs.
     */
    protected abstract void writeBody(JsonWriter writer) throws IOException;

    /**
     * Aggregates a numeric vector.
     *
     * @param vector the observations to aggregate.
     *
     * @return the aggregated value.
     */
    public double apply(double... vector) {
        return apply(D3xVectorView.of(vector));
    }

    /**
     * Writes this aggregator object to a JSON stream.
     *
     * @param writer the JSON writer to write to.
     *
     * @return the JSON writer, for operator chaining.
     *
     * @throws IOException if an I/O error occurs.
     */
    public JsonWriter write(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(AggregatorJson.AGG_TYPE);
        writer.value(aggType.name());
        writeBody(writer);
        writer.endObject();
        return writer;
    }

    @Override
    public boolean equals(Object other) {
        // Ensure that the concrete subclasses are identical...
        return other != null && other.getClass().equals(this.getClass()) && equalsAgg((VectorAggregator) other);
    }

    private boolean equalsAgg(VectorAggregator that) {
        return this.aggType.equals(that.aggType) && this.nanPolicy.equals(that.nanPolicy);
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Vector aggregators may not be used as hash keys.");
    }

    @Override
    public String toString() {
        try {
            var strWriter = new StringWriter();
            var jsonWriter = new JsonWriter(strWriter);
            write(jsonWriter);
            jsonWriter.close();
            return strWriter.toString();
        }
        catch (IOException ex) {
            return String.format("%s(%s)", getClass().getSimpleName(), aggType);
        }
    }
}
