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
package com.d3x.morpheus.csv;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.d3x.morpheus.util.Asserts;
import com.d3x.morpheus.util.Predicates;
import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.util.functions.ObjectIntBiFunction;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.parser.Parser;

/**
 * A DataFrameRequest used to load a DataFrame from a Resource expressed in ASCII CSV format.
 *
 * @param <R>   the row key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.Data()
public class CsvSourceOptions<R>  {

    /** Indicates whether content contains column headings */
    private boolean header;
    /** The formats used to register parsers for various types */
    private Formats formats;
    /** The resource to read content from */
    private Resource resource;
    /** The initial row capacity for DataFrame */
    private int rowCapacity;
    /** The data type for row keys */
    private Class<R> rowAxisType;
    /** The size of batch to log progress */
    private int logBatchSize;
    /** The batch size to process content */
    private int readBatchSize;
    /** The delimiter for row values */
    private char delimiter;
    /** The charset for content to read */
    private Charset charset;
    /** The max number of expected columns */
    private int maxColumns;
    /** The number of leading rows to skip */
    private int skipRowCount;
    /** The max number of rows to read */
    private int readRowCount;
    /** The optional column type mapped keyed by column names */
    private Map<String,Class<?>> colTypeMap;
    /** The optional row predicate to filter rows */
    private Predicate<String[]> rowPredicate;
    /** The function used to parse a row into a row key */
    private Function<String[],R> rowKeyParser;
    /** The optional column predicate based on column names */
    private Predicate<String> colNamePredicate;
    /** The optional column predicate based on column indexes */
    private Predicate<Integer> colIndexPredicate;
    /** The optional column name mapping function */
    private ObjectIntBiFunction<String,String> columnNameMapping;


    /**
     * Constructor
     */
    @SuppressWarnings("unchecked")
    public CsvSourceOptions() {
        this.header = true;
        this.delimiter = ',';
        this.rowCapacity = 1000;
        this.maxColumns = 1000;
        this.skipRowCount = 0;
        this.readRowCount = Integer.MAX_VALUE;
        this.readBatchSize = 1000;
        this.formats = new Formats();
        this.rowAxisType = (Class<R>)Integer.class;
        this.charset = StandardCharsets.UTF_8;
        this.colTypeMap = new HashMap<>();
    }

    /**
     * Returns the optional column type for the column name
     * @param colName   the column name
     * @return          the optional column type
     */
    public Optional<Class<?>> getColumnType(String colName) {
        return Optional.ofNullable(colTypeMap.get(colName));
    }

    /**
     * Returns the optional column name predicate for this request
     * @return  the optional column name predicate
     */
    public Optional<Predicate<String>> getColNamePredicate() {
        return Optional.ofNullable(colNamePredicate);
    }

    /**
     * Returns the optional column index predicate for this request
     * @return  the optional column index predicate
     */
    public Optional<Predicate<Integer>> getColIndexPredicate() {
        return Optional.ofNullable(colIndexPredicate);
    }

    /**
     * Returns the optional row predicate for this request
     * @return      the optional row predicate
     */
    public Optional<Predicate<String[]>> getRowPredicate() {
        return Optional.ofNullable(rowPredicate);
    }

    /**
     * Returns the row key function for this request
     * @return      the row key function for request
     */
    public  Optional<Function<String[],R>> getRowKeyParser() {
        return Optional.ofNullable(rowKeyParser);
    }

    /**
     * Returns the apply of column types for this request
     * @return      the apply of column types keyed by column name regex
     */
    public final Map<String,Class<?>> getColTypeMap() {
        return Collections.unmodifiableMap(colTypeMap);
    }

    /**
     * Returns the column mapping function used to rename columns
     * @return      the column mapping function
     */
    public Optional<ObjectIntBiFunction<String,String>> getColumnNameMapping() {
        return Optional.ofNullable(columnNameMapping);
    }

    /**
     * Sets the input file for these options
     * @param file  the input file
     */
    public void setFile(File file) {
        Objects.requireNonNull(file, "The file cannot be null");
        this.resource = Resource.of(file);
    }

    /**
     * Sets the input url for these options
     * @param url   the input url
     */
    public void setURL(URL url) {
        Objects.requireNonNull(url, "The file cannot be null");
        this.resource = Resource.of(url);
    }

    /**
     * Sets the input stream for these options
     * @param is    the input stream
     */
    public void setInputStream(InputStream is) {
        Objects.requireNonNull(is, "The InputStream resource cannot be null");
        this.resource = Resource.of(is);
    }

    /**
     * Applies to resource to load CSV content from
     * @param resource  the resource to load from (file, URL or Classpath resource)
     */
    public void setResource(String resource) {
        Objects.requireNonNull(resource, "The resource cannot be null");
        this.resource = Resource.of(resource);
    }

    /**
     * Applies to resource to load CSV content from
     * @param resource  the resource to load from (file, URL or Classpath resource)
     */
    public void setResource(Resource resource) {
        Objects.requireNonNull(resource, "The resource cannot be null");
        this.resource = resource;
    }

    /**
     * Applies to resource to load CSV content from
     * @param inputStream   the input stream to load from
     */
    public void setResource(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "The resource input stream cannot be null");
        this.resource = Resource.of(inputStream);
    }

    /**
     * Applies the row axis key type and associated parser function
     * @param rowType           the row axis key type
     * @param parser    the function that generates a key given tokens for a row
     */
    public void setRowKeyParser(Class<R> rowType, Function<String[],R> parser) {
        Asserts.notNull(rowType, "The row key type cannot be null");
        Asserts.notNull(parser, "The row key function cannot be null");
        this.rowAxisType = rowType;
        this.rowKeyParser = parser;
    }

    /**
     * Applies a column predicate that includes the specified columns
     * @param columns   the column names to include
     */
    public void setIncludeColumns(String... columns) {
        this.setColNamePredicate(Predicates.in(columns));
    }

    /**
     * Applies a column predicate that excludes the specified columns
     * @param columns   the column names to exclude
     */
    public void setExcludeColumns(String... columns) {
        this.setColNamePredicate(Predicates.in(columns).negate());
    }

    /**
     * Applies a column index predicate to include the specified indexes
     * @param columns   the column indexes to include
     */
    public void setIncludeColumnIndexes(int... columns) {
        this.setColIndexPredicate(Predicates.in(columns));
    }

    /**
     * Applies a column index predicate to exclude the specified indexes
     * @param columns   the column indexes to exclude
     */
    public void setExcludeColumnIndexes(int... columns) {
        this.setColIndexPredicate(Predicates.in(columns).negate());
    }

    /**
     * Applies a parser function for the column name and type specified
     * @param colNameRegex  the column name, which can be a regular expression
     * @param parser        the parser function
     * @param <T>           the type
     */
    public <T> void setParser(String colNameRegex, Parser<T> parser) {
        this.formats.setParser(colNameRegex, parser);
    }

    /**
     * Applies a parser function for the column name and type specified
     * @param colNameRegex  the column name, which can be a regular expression
     * @param type          the type for which to apply the default Parser
     */
    public void setParser(String colNameRegex, Class<?> type) {
        this.formats.setParser(colNameRegex, type);
    }

    /**
     * Applies the column type for the name specified
     * @param colNameRegex  the column name, which can be a regular expression
     * @param type          the column data type
     */
    public void setColumnType(String colNameRegex, Class<?> type) {
        this.colTypeMap.put(colNameRegex, type);
        if (formats != null && formats.getParser(colNameRegex) == null) {
            final Parser<?> parser = formats.getParser(type);
            if (parser != null) {
                this.formats.setParser(colNameRegex, parser);
            }
        }
    }

}
