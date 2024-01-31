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

import com.google.gson.stream.JsonWriter;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Aggregates time series data using a collection of other time series
 * aggregators.
 *
 * @author Scott Shaffer
 */
public class TsCompositeAggregator extends TimeSeriesAggregator {
    /**
     * The composite aggregator that combines the component aggregators.
     */
    @Getter
    @NonNull
    private final CrossSectionAggregator compositor;

    /**
     * The component aggregators that are combined by the compositor.
     */
    @Getter
    @NonNull
    private final List<TimeSeriesAggregator> components;

    /**
     * Creates a new aggregator using the specified parameters.
     *
     * @param compositor the composite aggregator that combines the components.
     * @param components the component aggregators that are combined by the compositor.
     */
    public TsCompositeAggregator(@NonNull CrossSectionAggregator compositor,
                                 @NonNull Collection<TimeSeriesAggregator> components) {
        super(AggregatorType.COMPOSITE, compositor.getNanPolicy(), resolveWindowLen(components));
        this.compositor = compositor;
        this.components = List.copyOf(components);
    }

    @Override
    public double apply(@NonNull D3xVectorView series) {
        validate(series);
        return compositor.apply(components.stream().mapToDouble(agg -> applyComponent(agg, series)).toArray());
    }

    @Override
    public TsCompositeAggregator validate() {
        if (components.isEmpty())
            throw new MorpheusException("At least one component is required.");

        return this;
    }

    @Override
    protected void writeBody(@NonNull JsonWriter writer) throws IOException {
        writer.name(AggregatorJson.COMPOSITOR);
        compositor.write(writer);
        writer.name(AggregatorJson.COMPONENTS);
        writer.beginArray();

        for (var component : components)
            component.write(writer);

        writer.endArray();
    }

    private static int resolveWindowLen(Collection<TimeSeriesAggregator> components) {
        return components.stream().mapToInt(TimeSeriesAggregator::getWindowLen).max().orElse(0);
    }

    private static double applyComponent(TimeSeriesAggregator component, D3xVectorView series) {
        // The component aggregators may take time series with different window lengths,
        // so always feed in the slice of the series beginning at the appropriate index.
        var window = component.getWindowLen();
        var subVec = series.subVectorView(series.length() - window, window);
        return component.apply(subVec);
    }
}
