/*
 * Copyright (C) 2018-2019 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.series;

import java.util.function.Function;
import java.util.stream.Collectors;

import com.d3x.core.json.JsonAdapter;
import com.d3x.core.json.JsonEngine;
import com.d3x.morpheus.util.text.Formats;

/**
 * A Json IO adapter for DoubleSeries
 * @param <K>   the key type
 */
public class DoubleSeriesJson<K> extends DataSeriesJson<K,Double,DoubleSeries<K>> {

    /**
     * Constructor
     * @param keyType   the key type
     * @param encode    the key encoder
     * @param decode    the key decoder
     */
    private DoubleSeriesJson(
        @lombok.NonNull Class<K> keyType,
        @lombok.NonNull Function<K, String> encode,
        @lombok.NonNull Function<String, K> decode) {
        super(DoubleSeries.ofType(keyType), encode, decode);
    }

    /**
     * Returns a newly created double series json IO adapter
     * @param keyType   the key type
     * @param encode    the key encoder
     * @param decode    the key decoder
     * @return          the IO adapter
     */
    public static <K> JsonAdapter<DoubleSeries<K>> of(
        @lombok.NonNull Class<K> keyType,
        @lombok.NonNull Function<K, String> encode,
        @lombok.NonNull Function<String, K> decode) {
        return new DoubleSeriesJson<>(keyType, encode, decode);
    }

    /**
     * Registers all default json serializers for data series
     * @param engine    the engine to register adapters against
     * @return          the same as arg
     */
    @SuppressWarnings("unchecked")
    public static JsonEngine registerDefaults(JsonEngine engine) {
        var formats = new Formats();
        var types = formats.getParserKeys().stream().filter(v -> v instanceof Class).map(Class.class::cast).collect(Collectors.toList());
        types.forEach(keyType -> {
            var parser = formats.getParser(keyType);
            var printer = formats.getPrinter(keyType);
            if (parser != null && printer != null) {
                engine.register(new DoubleSeriesJson<>(keyType, printer, parser));
            }
        });
        return engine;
    }

}
