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

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

import com.d3x.core.json.JsonAdapter;
import com.d3x.core.json.JsonEngine;
import com.d3x.core.json.JsonSchema;
import com.d3x.morpheus.util.text.Formats;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A Json IO adapter for for various types of DataSeries
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author Xavier Witdouck
 */
@lombok.AllArgsConstructor()
public class DataSeriesJson<K,V,S extends DataSeries<K,V>> implements JsonAdapter<S> {

    @lombok.NonNull
    private ParameterizedType type;
    @lombok.NonNull
    private Function<K,String> encode;
    @lombok.NonNull
    private Function<String,K> decode;


    /**
     * Returns a newly created data series json IO adapter
     * @param keyType   the key type
     * @param encode    the key encoder
     * @param decode    the key decoder
     * @return          the IO adapter
     */
    public static <K,V> JsonAdapter<DataSeries<K,V>> of(
        @lombok.NonNull Class<K> keyType,
        @lombok.NonNull Class<V> valueType,
        @lombok.NonNull Function<K, String> encode,
        @lombok.NonNull Function<String, K> decode) {
        return new DataSeriesJson<>(DataSeries.ofType(keyType, valueType), encode, decode);
    }

    /**
     * Registers all default json serializers for data series
     * @param engine    the engine to register adapters against
     * @return          the same as arg
     */
    public static JsonEngine registerDefaults(JsonEngine engine) {
        var formats = new Formats();
        var keys = formats.getParserKeys();
        keys.forEach(key -> {
            if (key instanceof Class) {
                var dataType = (Class<?>)key;
                var parser = formats.getParser(dataType);
                var printer = formats.getPrinter(dataType);
                if (parser != null && printer != null) {
                    keys.forEach(valueType -> {
                        var paramType = DataSeries.ofType((Class<?>)key, (Class<?>)valueType);
                        engine.register(new DataSeriesJson<>(paramType, printer, parser));
                    });
                }
            }
        });
        return engine;
    }


    @Override
    public JsonSchema schema() {
        return JsonSchema.of(type, "DataSeries", 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Read<S> read(JsonEngine engine, JsonReader reader) {
        return () -> {
            var token = reader.peek();
            if (token == null) {
                reader.nextNull();
                return null;
            } else {
                reader.beginObject();
                token = reader.peek();
                var keyType = (Class<K>)type.getActualTypeArguments()[0];
                var valueType = (Class<V>)type.getActualTypeArguments()[1];
                var builder = DataSeriesBuilder.builder(keyType, valueType);
                while (token != JsonToken.END_OBJECT) {
                    var name = reader.nextName();
                    if (name.equalsIgnoreCase("schemaName")) {
                        var schemaName = reader.nextString();
                        assert(schemaName.equals(schema().getName()));
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("schemaVersion")) {
                        var schemaVersion = reader.nextInt();
                        assert(schemaVersion == schema().getVersion());
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("length")) {
                        builder.capacity(reader.nextInt());
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("values")) {
                        reader.beginObject();
                        token = reader.peek();
                        if (token == JsonToken.NULL) {
                            reader.nextNull();
                        } else if (valueType.equals(Boolean.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.putBoolean(key, reader.nextBoolean());
                                token = reader.peek();
                            }
                        } else if (valueType.equals(Integer.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.putInt(key, reader.nextInt());
                                token = reader.peek();
                            }
                        } else if (valueType.equals(Long.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.putLong(key, reader.nextLong());
                                token = reader.peek();
                            }
                        } else if (valueType.equals(Double.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                token = reader.peek();
                                if (token == JsonToken.NULL) {
                                    reader.nextNull();
                                    builder.putDouble(key, Double.NaN);
                                    token = reader.peek();
                                } else {
                                    builder.putDouble(key, reader.nextDouble());
                                    token = reader.peek();
                                }
                            }
                        } else if (valueType.equals(String.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.putValue(key, (V)reader.nextString());
                                token = reader.peek();
                            }
                        } else {
                            var input = engine.io(valueType).input(reader);
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.putValue(key, input.read());
                                token = reader.peek();
                            }
                        }
                        reader.endObject();
                    }
                }
                reader.endObject();
                return (S)builder.build();
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Write<S> write(JsonEngine engine, JsonWriter writer) {
        return (record) -> {
            if (record == null) {
                writer.nullValue();
            } else {
                writer.beginObject();
                writer.name("schemaName").value("DataSeries");
                writer.name("schemaVersion").value(1);
                writer.name("length").value(record.size());
                writer.name("values");
                writer.beginObject();
                var iterator = record.keys().iterator();
                if (record instanceof DoubleSeries) {
                    var series = (DoubleSeries<K>)record;
                    while (iterator.hasNext()) {
                        var key = iterator.next();
                        var name = encode.apply(key);
                        writeDouble(writer, name, series.getDouble(key));
                    }
                } else {
                    var valueType = (Class<V>)type.getActualTypeArguments()[1];
                    var output = engine.io(valueType).output(writer);
                    while (iterator.hasNext()) {
                        var key = iterator.next();
                        writer.name(encode.apply(key));
                        output.write(record.getValue(key));
                    }
                }
                writer.endObject();
                writer.endObject();
            }
        };
    }
}
