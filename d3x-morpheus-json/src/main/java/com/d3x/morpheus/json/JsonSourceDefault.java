/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.json;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Resource;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * A JsonSource implementation that can parsed the DEFAULT
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
public class JsonSourceDefault<R,C> implements JsonSource<R,C> {


    @Override
    public DataFrame<R,C> read(Options<R,C> options) throws DataFrameException {
        var is = options.getResource().toInputStream();
        var reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
        try {
            return read(reader, options);
        } finally {
            IO.close(reader);
        }
    }

    /**
     * Returns a DataFrame loaded from the Json reader
     * @param reader        the Json stream reader
     * @param options       the options for parsing
     * @return              the resulting DataFrame
     * @throws DataFrameException   if fails to parse json into DataFrame
     */
    @SuppressWarnings("unchecked")
    public DataFrame<R,C> read(JsonReader reader, Options<R,C> options) throws DataFrameException {
        try {
            var token = reader.peek();
            if (token == JsonToken.NULL) {
                reader.nextNull();
                return null;
            } else {
                reader.beginObject();
                token = reader.peek();
                var rowCount = 10;
                var colCount = 10;
                DataFrame<R,C> result = null;
                var rowType = (Class<R>)Object.class;
                var colType = (Class<C>)Object.class;
                var formats = options.getFormats();
                while (token != JsonToken.END_OBJECT) {
                    var name = reader.nextName();
                    if (name.equalsIgnoreCase("rowCount")) {
                        rowCount = reader.nextInt();
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("colCount")) {
                        colCount = reader.nextInt();
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("rowType")) {
                        rowType = JsonSink.getDataType(reader.nextString());
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("colType")) {
                        colType = JsonSink.getDataType(reader.nextString());
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("columns")) {
                        var rows = Index.of(rowType, rowCount);
                        var cols = Index.of(colType, colCount);
                        result = DataFrame.ofObjects(rows, cols);
                        var columns = (JsonArray)new Gson().fromJson(reader, JsonArray.class);
                        for (int i=0; i<columns.size(); ++i) {
                            var object = columns.get(i).getAsJsonObject();
                            var key = formats.<C>parse(colType, object.get("key").getAsString());
                            var dataType = JsonSink.getDataType(object.get("dataType").getAsString());
                            var array = Array.of(dataType, rowCount);
                            result.cols().add(key, array);
                        }
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("data")) {
                        readData(result, reader, options);
                        token = reader.peek();
                    }
                }
                reader.endObject();
                return result;
            }
        } catch (IOException ex) {
            throw new DataFrameException("Failed to deserialzie DataFrame from json", ex);
        }
    }


    /**
     * Reads data from the json stream into the data frame
     * @param frame     the frame to populate with data
     * @param reader    the json stream reader
     * @param options   the json source options
     */
    private void readData(DataFrame<R,C> frame, JsonReader reader, Options options) throws IOException {
        var token = reader.peek();
        if (token == JsonToken.NULL) {
            reader.nextNull();
        } else {
            reader.beginObject();
            token = reader.peek();
            var cursor = frame.cursor();
            var formats = options.getFormats();
            var rowKeyType = frame.rows().keyClass();
            var typeMap = frame.cols().keys().collect(Collectors.toMap(v -> v, v -> frame.cols().type(v)));
            while (token != JsonToken.END_OBJECT) {
                var rowLabel = reader.nextName();
                var rowKey = formats.<R>parse(rowKeyType, rowLabel);
                frame.rows().add(rowKey);
                cursor.row(rowKey);
                reader.beginObject();
                token = reader.peek();
                while (token != JsonToken.END_OBJECT) {
                    var colLabel = reader.nextName();
                    var colIndex = Integer.parseInt(colLabel.replace("#", ""));
                    var colKey = frame.cols().key(colIndex);
                    var dataType = typeMap.get(colKey);
                    var typeCode = ArrayType.of(dataType);
                    token = reader.peek();
                    if (token == JsonToken.NULL) {
                        reader.nextNull();
                        cursor.colAt(colIndex).setValue(null);
                        token = reader.peek();
                    } else if (token == JsonToken.BOOLEAN) {
                        var value = reader.nextBoolean();
                        cursor.colAt(colIndex).setBoolean(value);
                        token = reader.peek();
                    } else if (token == JsonToken.STRING) {
                        var text = reader.nextString();
                        var parser = formats.getParserOrFail(colKey, dataType);
                        var value = parser.apply(text);
                        cursor.colAt(colIndex).setValue(value);
                        token = reader.peek();
                    } else if (typeCode == ArrayType.DOUBLE) {
                        var value = reader.nextDouble();
                        cursor.colAt(colIndex).setDouble(value);
                        token = reader.peek();
                    } else if (typeCode == ArrayType.LONG) {
                        var value = reader.nextLong();
                        cursor.colAt(colIndex).setLong(value);
                        token = reader.peek();
                    } else {
                        var value = reader.nextInt();
                        cursor.colAt(colIndex).setInt(value);
                        token = reader.peek();
                    }
                }
                reader.endObject();
                token = reader.peek();
            }
            reader.endObject();
        }
    }


    public static void main(String[] args) {
        var range = Range.of(LocalDate.parse("2019-05-01"), LocalDate.parse("2019-05-10"));
        var columns = IntStream.range(0, 10).mapToObj(i -> "Column-" + i).collect(Collectors.toList());
        var frame = DataFrame.ofDoubles(range, columns, v -> Math.random());
        var sink = new JsonSinkDefault<LocalDate,String>();
        var os = new ByteArrayOutputStream(1024 * 10);
        sink.write(frame, JsonSink.Options.create(v -> {
            v.resource(Resource.of(os));
            v.pretty(true);
        }));
        IO.println(new String(os.toByteArray()));
        var source = new JsonSourceDefault<LocalDate,String>();
        var result = source.read(o -> o.resource(Resource.of(new ByteArrayInputStream(os.toByteArray()))));
        IO.println(result);
    }


}
