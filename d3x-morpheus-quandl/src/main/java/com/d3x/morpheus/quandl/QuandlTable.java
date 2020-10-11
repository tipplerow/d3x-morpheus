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
package com.d3x.morpheus.quandl;

import java.time.LocalDate;

import com.d3x.core.json.JsonAdapter;
import com.d3x.core.json.JsonEngine;
import com.d3x.core.json.JsonSchema;
import com.d3x.core.util.Option;
import com.d3x.morpheus.frame.DataFrame;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

@lombok.AllArgsConstructor()
public class QuandlTable {

    /** The frequency for this dataset */
    @lombok.NonNull @lombok.Getter
    private final String frequency;
    /** The transform applied to this data */
    @lombok.NonNull @lombok.Getter
    private final Option<String> transform;
    /** The frame of data */
    @lombok.NonNull @lombok.Getter
    private final DataFrame<LocalDate,String> frame;


    public static class JsonIO implements JsonAdapter<QuandlTable> {

        @Override
        public JsonSchema schema() {
            return JsonSchema.of(QuandlTable.class, 1);
        }

        @Override
        public Read<QuandlTable> read(JsonEngine engine, JsonReader reader) {
            return () -> {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                } else {
                    reader.beginObject();


                    reader.endObject();
                    return null;
                }
            };
        }

        @Override
        public Write<QuandlTable> write(JsonEngine engine, JsonWriter writer) {
            throw new UnsupportedOperationException("Operation not supported");
        }
    }
}
