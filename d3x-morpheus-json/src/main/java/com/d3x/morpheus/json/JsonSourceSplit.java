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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.util.text.parser.Parser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * A JsonSource implementation that can load a DataFrame from Pandas compatible JSON with "split" orientation
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
public class JsonSourceSplit<R,C> implements JsonSource<R,C> {

    @Override
    public synchronized DataFrame<R,C> read(Options<R,C> options) throws DataFrameException {
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
    public synchronized DataFrame<R,C> read(JsonReader reader, Options<R,C> options) throws DataFrameException {
        try {
            var token = reader.peek();
            if (token == null) {
                reader.nextNull();
                return null;
            } else {
                reader.beginObject();
                token = reader.peek();
                var rows = new ArrayList<R>();
                var columns = new ArrayList<C>();
                DataFrame<R,C> frame = null;
                while (token != JsonToken.END_OBJECT) {
                    var name = reader.nextName();
                    if (name.equalsIgnoreCase("columns")) {
                        columns.addAll(this.columns(reader, options));
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("index")) {
                        rows.addAll(this.rows(reader, options));
                        token = reader.peek();
                    } else if (name.equalsIgnoreCase("data")) {
                        frame = data(reader, rows, columns);
                        token = reader.peek();
                    } else {
                        throw new DataFrameException("Unexpected field name in DataFrame JSON: " + name);
                    }
                }
                reader.endObject();
                return frame;
            }
        } catch (Exception ex) {
            throw new DataFrameException("Failed to parse json into DataFrame", ex);
        }
    }


    /**
     * Returns the column keys from reader
     * @param reader    the json reader
     * @param options   the options
     * @return          the column keys
     */
    @SuppressWarnings("unchecked")
    private List<C> columns(JsonReader reader, Options<R,C> options) throws IOException {
        reader.beginArray();
        var token = reader.peek();
        var defaultParser = (Parser<C>)Parser.forObject(String.class, v -> v);
        var parser = Optional.ofNullable(options.getColKeyParser()).orElse(defaultParser);
        var columns = new ArrayList<C>();
        while (token != JsonToken.END_ARRAY) {
            token = reader.peek();
            if (token == JsonToken.STRING) {
                var value = reader.nextString();
                columns.add(parser.apply(value));
                token = reader.peek();
            } else if (token == JsonToken.NUMBER) {
                var value = String.valueOf(reader.nextDouble());
                columns.add(parser.apply(value));
                token = reader.peek();
            } else if (token == JsonToken.BOOLEAN) {
                var value = String.valueOf(reader.nextBoolean());
                columns.add(parser.apply(value));
                token = reader.peek();
            } else if (token == JsonToken.NULL) {
                throw new DataFrameException("Cannot have null column headings in JSON");
            }
        }
        reader.endArray();
        return columns;
    }


    /**
     * Returns the row keys from reader
     * @param reader    the json reader
     * @param options   the options
     * @return          the row keys
     */
    @SuppressWarnings("unchecked")
    private List<R> rows(JsonReader reader, Options<R,C> options) throws IOException {
        reader.beginArray();
        var token = reader.peek();
        var defaultParser = (Parser<R>)Parser.forObject(String.class, v -> v);
        var parser = Optional.ofNullable(options.getRowKeyParser()).orElse(defaultParser);
        var rows = new ArrayList<R>();
        while (token != JsonToken.END_ARRAY) {
            token = reader.peek();
            if (token == JsonToken.STRING) {
                var value = reader.nextString();
                rows.add(parser.apply(value));
                token = reader.peek();
            } else if (token == JsonToken.NUMBER) {
                var value = String.valueOf(reader.nextDouble());
                rows.add(parser.apply(value));
                token = reader.peek();
            } else if (token == JsonToken.BOOLEAN) {
                var value = String.valueOf(reader.nextBoolean());
                rows.add(parser.apply(value));
                token = reader.peek();
            } else if (token == JsonToken.NULL) {
                throw new DataFrameException("Cannot have null row headings in JSON");
            }
        }
        reader.endArray();
        return rows;
    }


    /**
     * Returns the data frame with all the data
     * @param reader        the json reader
     * @param rows          the row keys
     * @param columns       the column keys
     * @return              the resulting data frame
     * @throws IOException      if there is an IO error
     */
    @SuppressWarnings("unchecked")
    private DataFrame<R,C> data(JsonReader reader, List<R> rows, List<C> columns) throws IOException {
        reader.beginArray();
        var token = reader.peek();
        var rowCount = rows.size();
        var colCount = columns.size();
        var colIndexes = IntStream.range(0, columns.size());
        var arrays = colIndexes.mapToObj(i -> ArrayBuilder.of(rowCount)).collect(Collectors.toList());
        while (token != JsonToken.END_ARRAY) {
            reader.beginArray();
            token = reader.peek();
            for (int i=0; i<colCount; ++i) {
                var array = arrays.get(i);
                if (token == JsonToken.BOOLEAN) {
                    array.appendBoolean(reader.nextBoolean());
                    token = reader.peek();
                } else if (token == JsonToken.NUMBER) {
                    array.appendDouble(reader.nextDouble());
                    token = reader.peek();
                } else if (token == JsonToken.STRING) {
                    array.append(reader.nextString());
                    token = reader.peek();
                } else if (token == JsonToken.NULL) {
                    reader.nextNull();
                    array.append(null);
                    token = reader.peek();
                }
            }
            reader.endArray();
            token = reader.peek();
        }
        reader.endArray();
        var colType = (Class<C>)columns.get(0).getClass();
        return DataFrame.of(rows, colType, cols -> {
            for (int i=0; i<columns.size(); ++i) {
                var column = columns.get(i);
                var array = arrays.get(i).toArray();
                cols.add(column, array);
            }
        });
    }


    public static void main(String[] args) {
        var file = new File("/Users/witdxav/projects/d3x-morpheus/test.json");
        var options = Options.<String,String>create(v -> v.resource(Resource.of(file)));
        var source = new JsonSourceSplit<String,String>();
        var t1 = System.currentTimeMillis();
        var frame = source.read(options);
        var t2 = System.currentTimeMillis();
        IO.println("Loaded frame in " + (t2-t1) + " millis");
        frame.out().print();
    }
}
