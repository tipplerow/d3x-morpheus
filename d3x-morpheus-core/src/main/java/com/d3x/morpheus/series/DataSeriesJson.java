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
import java.util.stream.Collectors;

import com.d3x.core.json.JsonAdapter;
import com.d3x.core.json.JsonEngine;
import com.d3x.core.json.JsonSchema;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.text.Formats;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A Json IO adapter for for various types of DataSeries
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
     * Registers all default json serializers for data series
     * @param engine    the engine to register adapters against
     * @return          the same as arg
     */
    public static JsonEngine registerDefaults(JsonEngine engine) {
        var formats = new Formats();
        var types = formats.getParserKeys().stream().filter(v -> v instanceof Class).map(Class.class::cast).collect(Collectors.toList());
        types.forEach(keyType -> {
            var parser = formats.getParser(keyType);
            var printer = formats.getPrinter(keyType);
            if (parser != null && printer != null) {
                engine.register(new DataSeriesJson<>(DoubleSeries.typeOf((Class<?>)keyType), printer, parser));
                types.forEach(valueType -> {
                    var paramType = DataSeries.typeOf((Class<?>)keyType, (Class<?>)valueType);
                    engine.register(new DataSeriesJson<>(paramType, printer, parser));
                });
            }
        });
        return engine;
    }


    @Override
    public JsonSchema schema() {
        return JsonSchema.of(type, ((Class<?>)type.getRawType()).getSimpleName(), 1);
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
                var builder = DataSeries.builder();
                var valueType = type.getActualTypeArguments()[1];
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
                                builder.addBoolean(key, reader.nextBoolean());
                                token = reader.peek();
                            }
                        } else if (valueType.equals(Integer.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.addInt(key, reader.nextInt());
                                token = reader.peek();
                            }
                        } else if (valueType.equals(Long.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.addLong(key, reader.nextLong());
                                token = reader.peek();
                            }
                        } else if (valueType.equals(Double.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                token = reader.peek();
                                if (token == JsonToken.NULL) {
                                    reader.nextNull();
                                    builder.addDouble(key, Double.NaN);
                                    token = reader.peek();
                                } else {
                                    builder.addDouble(key, reader.nextDouble());
                                    token = reader.peek();
                                }
                            }
                        } else if (valueType.equals(String.class)) {
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.addValue(key, reader.nextString());
                                token = reader.peek();
                            }
                        } else {
                            var input = engine.io(valueType).input(reader);
                            while (token != JsonToken.END_OBJECT) {
                                var key = decode.apply(reader.nextName());
                                builder.addValue(key, input.read());
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
                writer.name("schemaName").value(record.getClass().getSimpleName());
                writer.name("schemaVersion").value(1);
                writer.name("length").value(record.size());
                writer.name("values");
                writer.beginObject();
                var iterator = record.getKeys().iterator();
                if (record instanceof DoubleSeries) {
                    var series = (DoubleSeries<K>)record;
                    while (iterator.hasNext()) {
                        var key = iterator.next();
                        var name = encode.apply(key);
                        writeDouble(writer, name, series.getDouble(key));
                    }
                } else {
                    var dataClass = (Class<V>)record.dataClass();
                    var output = engine.io(dataClass).output(writer);
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


    public static void main(String[] args) {
        var engine = DataSeriesJson.registerDefaults(new JsonEngine());
        IO.println(engine);
    }
}
