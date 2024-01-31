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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads vector aggregators from JSON streams.
 *
 * @author Scott Shaffer
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class AggregatorJsonReader implements AggregatorJson {
    private final JsonReader reader;

    private Double halfLife = null;
    private Integer windowLen = null;
    private boolean renormalize = true;
    private NanPolicy nanPolicy = null;
    private AggregatorType aggType = null;
    private CrossSectionAggregator compositor = null;
    private final List<TimeSeriesAggregator> components = new ArrayList<>();

    /**
     * Reads a cross-section aggregator from a JSON stream.
     *
     * @param reader the JSON reader to read the aggregator.
     *
     * @return the cross-section aggregator read from the stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    static CrossSectionAggregator readCrossSectionAggregator(@NonNull JsonReader reader) throws IOException {
        return new AggregatorJsonReader(reader).readCrossSectionAggregator();
    }

    /**
     * Reads a time series aggregator from a JSON stream.
     *
     * @param reader the JSON reader to read the aggregator.
     *
     * @return the time series aggregator read from the stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    static TimeSeriesAggregator readTimeSeriesAggregator(@NonNull JsonReader reader) throws IOException {
        return new AggregatorJsonReader(reader).readTimeSeriesAggregator();
    }

    private CrossSectionAggregator readCrossSectionAggregator() throws IOException {
        var token = reader.peek();

        if (token.equals(JsonToken.NULL)) {
            reader.nextNull();
            return null;
        }

        readAggregator();
        requireAggType();

        if (aggType.isStatType()) {
            return createCsStat();
        }
        else {
            throw new MorpheusException("Unsupported cross-section aggregator type: [%s].", aggType);
        }
    }

    private TimeSeriesAggregator readTimeSeriesAggregator() throws IOException {
        var token = reader.peek();

        if (token.equals(JsonToken.NULL)) {
            reader.nextNull();
            return null;
        }

        readAggregator();
        requireAggType();

        if (aggType.isStatType()) {
            return createTsStat();
        }
        else if (aggType.equals(AggregatorType.EWMA)) {
            return createEwma();
        }
        else if (aggType.equals(AggregatorType.LWMA)) {
            return createLwma();
        }
        else if (aggType.equals(AggregatorType.COMPOSITE)) {
            return createComposite();
        }
        else {
            throw new MorpheusException("Unsupported time-series aggregator type: [%s].", aggType);
        }
    }

    private void readAggregator() throws IOException {
        reader.beginObject();

        while (reader.hasNext()) {
            var name = reader.nextName();

            switch (name) {
                case AGG_TYPE -> aggType = AggregatorType.valueOf(reader.nextString());
                case COMPONENTS -> readComponents();
                case COMPOSITOR -> compositor = CrossSectionAggregator.read(reader);
                case HALF_LIFE -> halfLife = reader.nextDouble();
                case NAN_POLICY -> nanPolicy = NanPolicy.read(reader);
                case WINDOW_LEN -> windowLen = reader.nextInt();
                case RENORMALIZE -> renormalize = reader.nextBoolean();
                default -> reader.skipValue();
            }
        }

        reader.endObject();
    }

    private void readComponents() throws IOException {
        reader.beginArray();

        while (reader.hasNext())
            components.add(TimeSeriesAggregator.read(reader));

        reader.endArray();
    }

    private CsStatisticAggregator createCsStat() {
        requireNanPolicy();
        return new CsStatisticAggregator(aggType, nanPolicy);
    }

    private TsStatisticAggregator createTsStat() {
        requireNanPolicy();
        requireWindowLen();
        return new TsStatisticAggregator(aggType, nanPolicy, windowLen);
    }

    private EwmaAggregator createEwma() {
        requireNanPolicy();
        requireWindowLen();
        requireHalfLife();
        return new EwmaAggregator(windowLen, halfLife, renormalize, nanPolicy);
    }

    private LwmaAggregator createLwma() {
        requireNanPolicy();
        requireWindowLen();
        return new LwmaAggregator(windowLen, renormalize, nanPolicy);
    }

    private TsCompositeAggregator createComposite() {
        requireCompositor();
        requireComponents();
        return new TsCompositeAggregator(compositor, components);
    }

    private void requireAggType() {
        if (aggType == null) {
            throw new MorpheusException("Missing aggregator type.");
        }
    }

    private void requireComponents() {
        if (components.isEmpty()) {
            throw new MorpheusException("Missing components.");
        }
    }

    private void requireCompositor() {
        if (compositor == null) {
            throw new MorpheusException("Missing compositor.");
        }
    }

    private void requireHalfLife() {
        if (halfLife == null) {
            throw new MorpheusException("Missing half-life.");
        }
    }

    private void requireNanPolicy() {
        if (nanPolicy == null) {
            throw new MorpheusException("Missing NaN policy.");
        }
    }

    private void requireWindowLen() {
        if (windowLen == null) {
            throw new MorpheusException("Missing window length.");
        }
    }
}
