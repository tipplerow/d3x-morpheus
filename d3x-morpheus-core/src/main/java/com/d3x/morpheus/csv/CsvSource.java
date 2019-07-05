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
package com.d3x.morpheus.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.Predicates;
import com.d3x.morpheus.util.functions.ObjectIntBiFunction;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.parser.Parser;

/**
 * Interface to a component that can read CSV contents into a Morpheus DataFrame
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public interface CsvSource {

    /**
     * Loads underlying resource into a DataFrame with sequence of integers for row keys
     * @return      the data frame result containing CSV data
     */
    DataFrame<Integer,String> read();

    /**
     * Loads underlying resource into a DataFrame with using a configured column for row keys
     * @param rowType       the row type
     * @param configurator  the options configurator
     * @return      the data frame result containing CSV data
     */
    <R> DataFrame<R,String> read(Class<R> rowType, Consumer<Options> configurator);


    /**
     * The options to tailor CSV parsing
     */
    @lombok.Data()
    class Options  {

        /** Indicates whether content contains column headings */
        private boolean header;
        /** The formats used to register parsers for various types */
        private Formats formats;
        /** The initial row capacity for DataFrame */
        private int rowCapacity;
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
        /** The column name to use for row keys */
        private String rowKeyColumnName;
        /** The column index to use for row keys */
        private Integer rowKeyColumnIndex;
        /** The optional column type mapped keyed by column names */
        private Map<String,Class<?>> colTypeMap;
        /** The optional row predicate to filter rows */
        private Predicate<String[]> rowPredicate;
        /** The optional column predicate based on column names */
        private Predicate<String> colNamePredicate;
        /** The optional column predicate based on column indexes */
        private Predicate<Integer> colIndexPredicate;
        /** The optional column name mapping function */
        private ObjectIntBiFunction<String,String> columnNameMapping;


        /**
         * Constructor
         */
        public Options() {
            this.header = true;
            this.delimiter = ',';
            this.rowCapacity = 1000;
            this.maxColumns = 1000;
            this.skipRowCount = 0;
            this.readRowCount = Integer.MAX_VALUE;
            this.readBatchSize = 1000;
            this.formats = new Formats();
            this.charset = StandardCharsets.UTF_8;
            this.colTypeMap = new HashMap<>();
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
         * Sets a parser for one or more columns
         * @param colNameRegex  the column name regular expression
         * @param parser        the parser
         */
        public void setParser(String colNameRegex, Parser<?> parser) {
            this.formats.setParser(colNameRegex, parser);
        }

        /**
         * Sets a parser for one or more columns
         * @param colNameRegex  the column name regular expression
         * @param type          the type for column
         * @param parser        the parser
         * @param <T>           the data type
         */
        public <T> void setParser(String colNameRegex, Class<T> type, Function<String,T> parser) {
            this.formats.setParser(colNameRegex, Parser.forObject(type, parser));
        }

        /**
         * Applies the column type for the name specified
         * @param colNameRegex  the column name, which can be a regular expression
         * @param type          the column data type
         */
        public void setColumnType(String colNameRegex, Class<?> type) {
            this.colTypeMap.put(colNameRegex, type);
            if (formats != null && formats.getParser(colNameRegex) == null) {
                var parser = formats.getParser(type);
                if (parser != null) {
                    this.formats.setParser(colNameRegex, parser);
                }
            }
        }

        /**
         * Returns the parser for the column name
         * @param colName   the column name
         * @return          the parsers
         */
        Optional<Parser<?>> getParser(String colName) {
            var userParser = formats.getParser(colName);
            if (userParser != null) {
                return Optional.of(userParser);
            } else {
                for (Object key : formats.getParserKeys()) {
                    if (key instanceof String) {
                        var keyString = key.toString();
                        if (colName.matches(keyString)) {
                            var parser = formats.getParserOrFail(keyString);
                            return Optional.ofNullable(parser);
                        }
                    }
                }
                return Optional.empty();
            }
        }

        /**
         * Returns true if the column should be included in result
         * @param colName   the column name
         * @param colIndex  the column index
         * @return          true if column should be included
         */
        boolean include(String colName, int colIndex) {
            if (colIndexPredicate != null && colIndexPredicate.test(colIndex)) {
                return true;
            } else if (colNamePredicate != null && colNamePredicate.test(colName)) {
                return true;
            } else {
                return colIndexPredicate == null && colNamePredicate == null;
            }
        }
    }
}
