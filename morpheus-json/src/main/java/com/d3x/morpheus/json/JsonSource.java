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
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.Asserts;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.parser.Parser;

/**
 * A source used to load a DataFrame from JSON content
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class JsonSource {


    /**
     * Returns a DataFrame loaded from JSON in the resource specified
     * @param file  the resource to load from
     * @return      the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    public <R,C> DataFrame<R,C> read(File file) throws DataFrameException {
        return read(o -> o.setFile(file));
    }


    /**
     * Returns a DataFrame loaded from JSON in the resource specified
     * @param url  the resource to load from
     * @return      the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    public <R,C> DataFrame<R,C> read(URL url) throws DataFrameException {
        return read(o -> o.setURL(url));
    }

    /**
     * Returns a DataFrame loaded from JSON in the resource specified
     * @param is  the resource to load from
     * @return      the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    public <R,C> DataFrame<R,C> read(InputStream is) throws DataFrameException {
        return read(o -> o.setInputStream(is));
    }

    /**
     * Returns a DataFrame loaded from JSON in the resource specified
     * @param resource  the resource to load from
     * @return      the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    public <R,C> DataFrame<R,C> read(String resource) throws DataFrameException {
        return read(o -> o.setResource(Resource.of(resource)));
    }


    /**
     * Returns a DataFrame loaded from JSON as specified by the options
     * @param configurator      the options configurator
     * @return                  the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    public <R,C> DataFrame<R,C> read(Consumer<Options<R,C>> configurator) throws DataFrameException {
        final Options<R,C> options = new Options<>();
        configurator.accept(options);
        try {
            final JsonReader reader = options.getReader();
            reader.beginObject();
            if (reader.hasNext()) {
                final String rootName = reader.nextName();
                if (!rootName.equalsIgnoreCase("DataFrame")) {
                    throw new DataFrameException("Unsupported JSON format for DataFrame");
                } else {
                    reader.beginObject();
                    int rowCount = -1;
                    int colCount = -1;
                    while (reader.hasNext()) {
                        final String name = reader.nextName();
                        if (name.equalsIgnoreCase("rowCount")) {
                            rowCount = reader.nextInt();
                        } else if (name.equalsIgnoreCase("colCount")) {
                            colCount = reader.nextInt();
                        } else if (name.equalsIgnoreCase("rowKeys")) {
                            readRowKeys(options, rowCount);
                        } else if (name.equalsIgnoreCase("columns")) {
                            readColumns(options, rowCount, colCount);
                        }
                    }
                }
            }
            reader.endObject();
            return createFrame(options);
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to load DataFrame for request: " + options, ex);
        } finally {
            IO.close(options);
        }
    }

    /**
     * Returns a newly created DataFrame from the contents that has been read from JSON
     * @param options   the json options
     * @return      the newly created DataFrame
     */
    private <R,C> DataFrame<R,C> createFrame(Options<R,C> options) {
        final Array<R> rowKeys = options.rowKeyBuilder.toArray();
        final Array<C> colKeys = options.colKeyBuilder.toArray();
        final Class<C> colType = colKeys.type();
        return DataFrame.of(rowKeys, colType, columns -> {
            for (int i=0; i<colKeys.length(); ++i) {
                final C colKey = colKeys.getValue(i);
                final Array<?> array = options.dataList.get(i);
                columns.add(colKey, array);
            }
        });
    }


    /**
     * Reads row keys from the stream
     * @param options   the json options
     * @param rowCount  the row count for frame
     */
    @SuppressWarnings("unchecked")
    private <R,C> void readRowKeys(Options<R,C> options, int rowCount) {
        try {
            final Formats formats = options.formats;
            final JsonReader reader = options.reader;
            reader.beginObject();
            options.rowKeyBuilder = ArrayBuilder.of(rowCount);
            Parser<R> parser = formats.getParserOrFail(Object.class);
            final Map<String,Class<?>> typeMap = getTypeMap(formats);
            while (reader.hasNext()) {
                final String name = reader.nextName();
                if (name.equalsIgnoreCase("type")) {
                    final String typeName = reader.nextString();
                    final Class<?> dataType = typeMap.get(typeName);
                    if (dataType == null) throw new IllegalArgumentException("No Formats parser exists for type name: " + typeName);
                    parser = formats.getParserOrFail(typeName, dataType);
                } else if (name.equalsIgnoreCase("values")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        final String value = reader.nextString();
                        final R key = parser.apply(value);
                        options.rowKeyBuilder.add(key);
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to read row keys for DataFrame", ex);
        }
    }

    /**
     * Reads the column data from JSON and returs a apply of column data by key
     * @param rowCount      the row count
     * @param colCount      the column count
     */
    @SuppressWarnings("unchecked")
    private <R,C> void readColumns(Options<R,C> options, int rowCount, int colCount) {
        try {
            options.reader.beginArray();
            options.dataList = new ArrayList<>(colCount);
            options.colKeyBuilder = ArrayBuilder.of(colCount);
            final JsonReader reader = options.reader;
            final Formats formats = options.formats;
            final Map<String,Class<?>> typeMap = getTypeMap(formats);
            while (reader.hasNext()) {
                reader.beginObject();
                String key = null;
                boolean sparse = false;
                ArrayType dataType = null;
                Class<?> dataClass = null;
                String keyTypeName = null;
                Function<String,?> parser = formats.getParserOrFail(Object.class);
                ArrayBuilder<Object> dataBuilder = null;
                while (reader.hasNext()) {
                    final String name = reader.nextName();
                    if (name.equalsIgnoreCase("key")) {
                        key = reader.nextString();
                    } else if (name.equalsIgnoreCase("keyType")) {
                        keyTypeName = reader.nextString();
                        final Class<?> keyType = typeMap.get(keyTypeName);
                        final C colKey = (C)formats.getParserOrFail(keyType).apply(key);
                        options.colKeyBuilder.add(colKey);
                    } else if (name.equalsIgnoreCase("dataType")) {
                        final String dataTypeName = reader.nextString();
                        dataClass = typeMap.get(dataTypeName);
                        parser = formats.getParserOrFail(dataTypeName, dataClass);
                        if (parser == null) throw new RuntimeException("No parser configured in formats for type named: " + dataTypeName);
                        dataType = ArrayType.of(dataClass);
                    } else if (name.equalsIgnoreCase("sparse")) {
                        sparse = reader.nextBoolean();
                    } else if (name.equalsIgnoreCase("defaultValue")) {
                        Asserts.notNull(dataType, "The data type for column has not been resolved: " + key);
                        final JsonToken token = reader.peek();
                        if (token == JsonToken.NULL) {
                            reader.nextNull();
                            dataBuilder = ArrayBuilder.of(rowCount, (Class<Object>)dataClass, null);
                        } else if (token == JsonToken.BOOLEAN) {
                            final boolean nullValue = token != JsonToken.NULL && reader.nextBoolean();
                            dataBuilder = ArrayBuilder.of(rowCount, (Class<Object>)dataClass, nullValue);
                        } else if (token == JsonToken.NUMBER) {
                            final String nullString = token == JsonToken.NULL ? null : reader.nextString();
                            dataBuilder = ArrayBuilder.of(rowCount, (Class<Object>)dataClass, parser.apply(nullString));
                        } else if (token == JsonToken.STRING) {
                            final String nullString = token == JsonToken.NULL ? null : String.valueOf(reader.nextDouble());
                            dataBuilder = ArrayBuilder.of(rowCount, (Class<Object>)dataClass, parser.apply(nullString));
                        } else {
                            throw new IllegalStateException("Unexpected JsonToken: " + token);
                        }
                    } else if (name.equalsIgnoreCase("values")) {
                        Asserts.notNull(dataType, "The data type for column has not been resolved: " + key);
                        Asserts.notNull(dataBuilder, "");
                        reader.beginArray();
                        if (dataType.isBoolean()) {
                            while (reader.hasNext()) {
                                final boolean isNull = reader.peek() == JsonToken.NULL;
                                final boolean rawValue = !isNull && reader.nextBoolean();
                                dataBuilder.addBoolean(rawValue);
                            }
                        } else if (dataType.isInteger()) {
                            while (reader.hasNext()) {
                                final boolean isNull = reader.peek() == JsonToken.NULL;
                                final int rawValue = isNull ? 0 : reader.nextInt();
                                dataBuilder.addInt(rawValue);
                            }
                        } else if (dataType.isLong()) {
                            while (reader.hasNext()) {
                                final boolean isNull = reader.peek() == JsonToken.NULL;
                                final long rawValue = isNull ? 0L : reader.nextLong();
                                dataBuilder.addLong(rawValue);
                            }
                        } else if (dataType.isDouble()) {
                            while (reader.hasNext()) {
                                final boolean isNull = reader.peek() == JsonToken.NULL;
                                final double rawValue = isNull ? Double.NaN : reader.nextDouble();
                                dataBuilder.addDouble(rawValue);
                            }
                        } else {
                            while (reader.hasNext()) {
                                final boolean isNull = reader.peek() == JsonToken.NULL;
                                final String rawValue = isNull ? null : reader.nextString();
                                final Object value = parser.apply(rawValue);
                                dataBuilder.add(value);
                            }
                        }
                        final Array<?> array = dataBuilder.toArray();
                        options.dataList.add(array);
                        options.reader.endArray();
                    }
                }
                reader.endObject();
            }
            reader.endArray();
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to read columns for DataFrame", ex);
        }
    }


    /**
     * Returns the type apply from the formats
     * @param formats   the formats
     * @return          the type apply
     */
    private Map<String,Class<?>> getTypeMap(Formats formats) {
        final Map<String,Class<?>> typeMap = new HashMap<>();
        formats.getParserKeys().forEach(key -> {
            if (key instanceof Class) {
                final Class<?> type = (Class<?>)key;
                final String name = type.getSimpleName();
                typeMap.put(name, type);
            }
        });
        return typeMap;
    }


    /**
     * The options for this source
     * @param <R>   the row key type
     * @param <C>   the column key type
     */
    public static class Options<R,C> implements Closeable {

        private JsonReader reader;
        private List<Array<?>> dataList;
        private ArrayBuilder<R> rowKeyBuilder;
        private ArrayBuilder<C> colKeyBuilder;
        /** The resource to load frame from */
        @lombok.Setter @lombok.Getter private Resource resource;
        /** The formats used to parse JSON values */
        @lombok.Setter @lombok.Getter private Formats formats;
        /** The character encoding for content */
        @lombok.Setter @lombok.Getter private Charset charset;
        /** The optional row predicate to filter rows */
        @lombok.Setter @lombok.Getter private Predicate<R> rowPredicate;
        /** The optional column predicate to filter columns */
        @lombok.Setter @lombok.Getter private Predicate<C> colPredicate;

        /**
         * Constructor
         */
        public Options() {
            this.formats = new Formats();
            this.charset = StandardCharsets.UTF_8;
        }

        /**
         * Sets the input file for these options
         * @param file  the input file
         */
        public void setFile(File file) {
            this.resource = Resource.of(file);
        }

        /**
         * Sets the input URL for these options
         * @param url   the input url
         */
        public void setURL(URL url) {
            this.resource = Resource.of(url);
        }

        /**
         * Applies to resource to load CSV content from
         * @param inputStream   the input stream to load from
         */
        public void setInputStream(InputStream inputStream) {
            this.resource = Resource.of(inputStream);
        }

        /**
         * Returns the reader for these options
         * @return  the reader for options
         */
        private synchronized JsonReader getReader() {
            if (reader != null) {
                return reader;
            } else if (resource == null) {
                throw new DataFrameException("No resource specified to load JSON");
            } else {
                final InputStream is = resource.toInputStream();
                final BufferedInputStream bis = new BufferedInputStream(is);
                this.reader = new JsonReader(new InputStreamReader(bis, charset));
                return reader;
            }
        }

        @Override
        public void close() {
            IO.close(reader);
        }
    }


}
