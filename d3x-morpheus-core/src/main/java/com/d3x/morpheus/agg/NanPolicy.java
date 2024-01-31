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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;

/**
 * Defines a policy to handle missing (NaN) values in vector aggregation.
 *
 * @author Scott Shaffer
 */
@Value
@Builder
public class NanPolicy {
    /**
     * The enumerated policy type.
     */
    @NonNull
    Type type;

    /**
     * The minimum number of non-missing values required for the aggregate
     * value to be non-missing.
     */
    @Builder.Default
    int minObs = 1;

    /**
     * The replacement for missing values.
     */
    @Builder.Default
    double replace = 0.0;

    /**
     * The enumerated policy types.
     */
    public enum Type {
        NAN, OMIT, REPLACE
    }

    /**
     * Reads a NanPolicy object as the next token in a JSON stream.
     *
     * @param reader the JSON reader to read from.
     *
     * @return the NanPolicy object read from the JSON stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public static NanPolicy read(@NonNull JsonReader reader) throws IOException {
        var token = reader.peek();

        if (token.equals(JsonToken.NULL)) {
            reader.nextNull();
            return null;
        }

        var builder = builder();
        reader.beginObject();

        while (reader.hasNext()) {
            var name = reader.nextName();

            switch (name) {
                case "type" -> builder.type(Type.valueOf(reader.nextString()));
                case "minObs" -> builder.minObs(reader.nextInt());
                case "replace" -> builder.replace(reader.nextDouble());
                default -> reader.skipValue();
            }
        }

        reader.endObject();
        return builder.build();
    }

    /**
     * Applies this policy to an element value.
     *
     * @param value the actual element value.
     *
     * @return the value according to this policy.
     */
    public double apply(double value) {
        if (type.equals(Type.REPLACE) && Double.isNaN(value)) {
            return replace;
        }
        else {
            // Missing input values remain missing...
            return value;
        }
    }

    /**
     * Determines whether to omit a value from the vector aggregation.
     *
     * @param value the actual element value.
     *
     * @return {@code true} iff the value should be omitted from the
     * vector aggregation.
     */
    public boolean omit(double value) {
        return type.equals(Type.OMIT) && Double.isNaN(value);
    }

    /**
     * Writes this NanPolicy object to a JSON stream.
     *
     * @param writer the JSON writer to write to.
     *
     * @return the JSON writer, for operator chaining.
     *
     * @throws IOException if an I/O error occurs.
     */
    public JsonWriter write(@NonNull JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("type").value(type.name());
        writer.name("minObs").value(minObs);

        if (Double.isFinite(replace))
            writer.name("replace").value(replace);

        writer.endObject();
        return writer;
    }

    @Override
    public boolean equals(Object that) {
        return (that instanceof NanPolicy) && equalsPolicy((NanPolicy) that);
    }

    private boolean equalsPolicy(NanPolicy that) {
        return this.type.equals(that.type)
                && this.minObs == that.minObs
                && DoubleComparator.DEFAULT.equals(this.replace, that.replace);
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("NanPolicy objects may not be used as hash keys.");
    }
}
