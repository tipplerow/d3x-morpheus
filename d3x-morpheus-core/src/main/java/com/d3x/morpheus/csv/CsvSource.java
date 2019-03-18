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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameCursor;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameSource;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.util.http.HttpClient;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.parser.Parser;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 * A DataFrameSource designed to load DataFrames from a CSV resource based on the CSV request descriptor.
 *
 * @param <R>   the row key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class CsvSource<R> implements DataFrameSource<R,String,CsvSourceOptions<R>> {


    /**
     * Constructor
     */
    public CsvSource() {
        super();
    }


    @Override
    public DataFrame<R,String> read(Consumer<CsvSourceOptions<R>> configurator) throws DataFrameException {
        try {
            final CsvSourceOptions<R> options = initOptions(new CsvSourceOptions<>(), configurator);
            final Resource resource = options.getResource();
            switch (resource.getType()) {
                case FILE:          return parse(options, new FileInputStream(resource.asFile()));
                case URL:           return parse(options, resource.asURL());
                case INPUT_STREAM:  return parse(options, resource.asInputStream());
                default:    throw new DataFrameException("Unsupported resource specified in CSVRequest: " + resource);
            }
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to create DataFrame from CSV source", ex);
        }
    }


    /**
     * Returns a DataFrame parsed from the url specified
     * @param url   the url to parse
     * @return      the DataFrame parsed from url
     * @throws IOException      if there stream read error
     */
    private DataFrame<R,String> parse(CsvSourceOptions<R> request, URL url) throws IOException {
        Objects.requireNonNull(url, "The URL cannot be null");
        if (!url.getProtocol().startsWith("http")) {
            return parse(request, url.openStream());
        } else {
            return HttpClient.getDefault().<DataFrame<R,String>>doGet(httpRequest -> {
                httpRequest.setUrl(url);
                httpRequest.setResponseHandler(response -> {
                    try (InputStream stream = response.getStream()) {
                        final DataFrame<R,String> frame = parse(request, stream);
                        return Optional.ofNullable(frame);
                    } catch (IOException ex) {
                        throw new RuntimeException("Failed to load DataFrame from csv: " + url, ex);
                    }
                });
            }).orElse(null);
        }
    }


    /**
     * Returns a DataFrame parsed from the stream specified stream
     * @param stream    the stream to parse
     * @return          the DataFrame parsed from stream
     * @throws IOException      if there stream read error
     */
    private DataFrame<R,String> parse(CsvSourceOptions<R> options, InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, options.getCharset()))) {
            final CsvRequestHandler handler = new CsvRequestHandler(options);
            final CsvParserSettings settings = new CsvParserSettings();
            settings.getFormat().setDelimiter(options.getDelimiter());
            settings.setHeaderExtractionEnabled(options.isHeader());
            settings.setLineSeparatorDetectionEnabled(true);
            settings.setProcessor(handler);
            settings.setIgnoreTrailingWhitespaces(true);
            settings.setIgnoreLeadingWhitespaces(true);
            settings.setSkipEmptyLines(true);
            settings.setMaxColumns(options.getMaxColumns());
            settings.setNumberOfRowsToSkip(options.getSkipRowCount());
            settings.setNumberOfRecordsToRead(options.getReadRowCount());
            settings.setReadInputOnSeparateThread(false);
            final CsvParser parser = new CsvParser(settings);
            parser.parse(reader);
            return handler.getFrame();
        }
    }



    /**
     * A RowProcessor that receives callbacks and incrementally builds the DataFrame.
     */
    private class CsvRequestHandler implements RowProcessor {

        private int rowCounter;
        private String[] headers;
        private int[] colIndexes;
        private int logBatchSize;
        private String[] rowValues;
        private DataBatch<R> batch;
        private Parser<?>[] parsers;
        private CsvSourceOptions<R> options;
        private DataFrame<R,String> frame;
        private Predicate<String[]> rowPredicate;
        private Function<String[],R> rowKeyParser;

        /**
         * Constructor
         * @param options   the options
         */
        CsvRequestHandler(CsvSourceOptions<R> options) {
            this.options = options;
            this.rowPredicate = options.getRowPredicate().orElse(null);
            this.rowKeyParser = options.getRowKeyParser().orElse(null);
            this.logBatchSize = options.getLogBatchSize();
        }


        /**
         * Returns the DataFrame result for this processor
         * @return  the DataFrame result
         */
        public DataFrame<R,String> getFrame() {
            return frame;
        }


        @Override
        public void processStarted(ParsingContext context) {}


        @Override
        public void rowProcessed(String[] row, ParsingContext context) {
            try {
                if (batch == null) {
                    initBatch(row.length, context);
                }
                if (rowPredicate == null || rowPredicate.test(row)) {
                    this.rowCounter++;
                    if (logBatchSize > 0 && rowCounter % logBatchSize == 0) {
                        IO.println("Loaded " + rowCounter + " rows...");
                    }
                    for (int i = 0; i < colIndexes.length; ++i) {
                        final int colIndex = colIndexes[i];
                        final String rawValue = row.length > colIndex ? row[colIndex] : null;
                        this.rowValues[i] = rawValue;
                    }
                    if (rowKeyParser == null) {
                        this.batch.addRow(rowCounter - 1, rowValues);
                    } else {
                        final R rowKey = rowKeyParser.apply(row);
                        this.batch.addRow(rowKey, rowValues);
                    }
                    if (batch.rowCount() == options.getReadBatchSize()) {
                        this.processBatch(batch);
                        this.batch.clear();
                    }
                }
            } catch (DataFrameException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new DataFrameException("Failed to parse row: " + Arrays.toString(row), ex);
            }
        }


        @Override
        public void processEnded(ParsingContext context) {
            try {
                this.batch = batch != null ? batch : new DataBatch<>(options, 0);
                this.processBatch(batch);
            } catch (DataFrameException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new DataFrameException("Failed to process CSV parse end", ex);
            }
        }


        /**
         * Initializes data structures to capture parsed content
         * @param csvColCount   the number of columns in CSV content
         * @param context       the parsing context
         */
        @SuppressWarnings("unchecked")
        private void initBatch(int csvColCount, ParsingContext context) {
            final int colCount = initHeader(csvColCount, context);
            this.rowValues = new String[colCount];
            this.batch = new DataBatch<>(options, colCount);
            this.parsers = new Parser[colCount];
        }


        /**
         * Initializes the header array and column ordinals
         * @param csvColCount   the number of columns in CSV content
         * @param context       the parsing context
         * @return              the column count for frame
         */
        private int initHeader(int csvColCount, ParsingContext context) {
            this.headers = options.isHeader() ? context.headers() : IntStream.range(0, csvColCount).mapToObj(i -> "Column-" + i).toArray(String[]::new);
            this.headers = IntStream.range(0, headers.length).mapToObj(i -> headers[i] != null ? headers[i] : "Column-" + i).toArray(String[]::new);
            this.colIndexes = IntStream.range(0, headers.length).toArray();
            this.options.getColIndexPredicate().ifPresent(predicate -> {
                final Map<String,Integer> indexMap = IntStream.range(0, headers.length).boxed().collect(Collectors.toMap(i -> headers[i], i -> colIndexes[i]));
                this.headers = Arrays.stream(headers).filter(colName -> predicate.test(indexMap.get(colName))).toArray(String[]::new);
                this.colIndexes = Arrays.stream(headers).mapToInt(indexMap::get).toArray();
            });
            this.options.getColNamePredicate().ifPresent(predicate -> {
                final Map<String,Integer> indexMap = IntStream.range(0, headers.length).boxed().collect(Collectors.toMap(i -> headers[i], i -> colIndexes[i]));
                this.headers = Arrays.stream(headers).filter(predicate).toArray(String[]::new);
                this.colIndexes = Arrays.stream(headers).mapToInt(indexMap::get).toArray();
            });
            this.options.getColumnNameMapping().ifPresent(mapping -> {
                final IntStream colOrdinals = IntStream.range(0, headers.length);
                this.headers = colOrdinals.mapToObj(ordinal -> mapping.apply(headers[ordinal], ordinal)).toArray(String[]::new);
            });
            return colIndexes.length;
        }


        /**
         * Initializes the frame based on the contents of the first batch
         * @param batch     the initial batch to initialize frame
         */
        private void initFrame(DataBatch<R> batch) {
            if (headers == null) {
                final Class<R> rowType = options.getRowAxisType();
                final Index<R> rowKeys = Index.of(rowType, 1);
                final Index<String> colKeys = Index.of(String.class, 1);
                this.frame = DataFrame.of(rowKeys, colKeys, Object.class);
            } else {
                final int colCount = headers.length;
                final Formats formats = options.getFormats();
                final Class<R> rowType = options.getRowAxisType();
                final Index<R> rowKeys = Index.of(rowType, Math.max(10, options.getRowCapacity()));
                final Index<String> colKeys = Index.of(String.class, colCount);
                this.frame = DataFrame.of(rowKeys, colKeys, Object.class);
                for (int i=0; i<colCount; ++i) {
                    final String colName = headers[i] != null ? headers[i] : "Column-" + i;
                    try {
                        final String[] rawValues = batch.colData(i);
                        final Optional<Parser<?>> userParser = getParser(options.getFormats(), colName);
                        final Optional<Class<?>> colType = getColumnType(colName);
                        if (colType.isPresent()) {
                            final Class<?> type = colType.get();
                            final Parser<?> parser = userParser.orElse(formats.getParserOrFail(type, Object.class));
                            this.parsers[i] = parser;
                            this.frame.cols().add(colName, type);
                        } else {
                            final Parser<?> stringParser = formats.getParserOrFail(String.class);
                            final Parser<?> parser = userParser.orElse(formats.findParser(rawValues).orElse(stringParser));
                            final Set<Class<?>> typeSet = Arrays.stream(rawValues).map(parser).filter(Objects::nonNull).map(Object::getClass).collect(Collectors.toSet());
                            final Class<?> type = typeSet.size() == 1 ? typeSet.iterator().next() : Object.class;
                            this.parsers[i] = parser;
                            this.frame.cols().add(colName, type);
                        }
                    } catch (Exception ex) {
                        throw new DataFrameException("Failed to inspect seed values in column: " + colName, ex);
                    }
                }
            }
        }

        /**
         * Returns the column type for the column name
         * @param colName   the column name
         * @return          the column type
         */
        private Optional<Class<?>> getColumnType(String colName) {
            final Optional<Class<?>> colType = options.getColumnType(colName);
            if (colType.isPresent()) {
                return colType;
            } else {
                for (Map.Entry<String,Class<?>> entry : options.getColTypeMap().entrySet()) {
                    final String key = entry.getKey();
                    if (colName.matches(key)) {
                        return Optional.of(entry.getValue());
                    }
                }
                return Optional.empty();
            }
        }

        /**
         * Processes the batch of data provided
         * @param batch the batch reference
         */
        private void processBatch(DataBatch<R> batch) {
            int rowOrdinal = -1;
            try {
                if (frame == null) {
                    initFrame(batch);
                }
                if (batch.rowCount() > 0) {
                    final int rowCount = batch.rowCount();
                    final Array<R> keys = batch.keys();
                    final int fromRowOrdinal = frame.rowCount();
                    final Array<R> rowKeys = rowCount < options.getReadBatchSize() ? keys.copy(0, rowCount) : keys;
                    final Array<R> added = this.frame.rows().addAll(rowKeys);
                    if (added.length() < rowKeys.length()) {
                        throw new DataFrameException("Duplicate row keys encountered in csv source");
                    }
                    final DataFrameCursor<R,String> cursor = frame.cursor();
                    for (int j=0; j<colIndexes.length; ++j) {
                        final String[] colValues = batch.colData(j);
                        final Parser<?> parser = parsers[j];
                        cursor.atCol(j);
                        for (int i=0; i<rowCount; ++i) {
                            rowOrdinal = fromRowOrdinal + i;
                            cursor.atRow(rowOrdinal);
                            final String rawValue = colValues[i];
                            switch (parser.getStyle()) {
                                case BOOLEAN:   cursor.setBoolean(parser.applyAsBoolean(rawValue));  break;
                                case INTEGER:   cursor.setInt(parser.applyAsInt(rawValue));          break;
                                case LONG:      cursor.setLong(parser.applyAsLong(rawValue));        break;
                                case DOUBLE:    cursor.setDouble(parser.applyAsDouble(rawValue));    break;
                                default:        cursor.setValue(parser.apply(rawValue));             break;
                            }
                        }
                    }
                    if (frame.rowCount() % 100000 == 0) {
                        System.out.println("Processed " + frame.rowCount() + " rows...");
                    }
                }
            } catch (Exception ex) {
                final int lineNo = options.isHeader() ? rowOrdinal + 2 : rowOrdinal + 1;
                throw new DataFrameException("Failed to process CSV batch, line no " + lineNo, ex);
            }
        }
    }

    /**
     * Returns the user configured parser for column name
     * @param colName   the column name
     * @return          the parser match
     */
    private static Optional<Parser<?>> getParser(Formats formats, String colName) {
        final Parser<?> userParser = formats.getParser(colName);
        if (userParser != null) {
            return Optional.of(userParser);
        } else {
            for (Object key : formats.getParserKeys()) {
                if (key instanceof String) {
                    final String keyString = key.toString();
                    if (colName.matches(keyString)) {
                        final Parser<?> parser = formats.getParserOrFail(keyString);
                        return Optional.ofNullable(parser);
                    }
                }
            }
            return Optional.empty();
        }
    }


    /**
     * A class that represents a batch of raw CSV that needs to be parsed into type specific values
     * @param <X>       the row key type
     */
    protected static class DataBatch<X> {

        private Array<X> keys;
        private int rowCount;
        private String[][] data;

        /**
         * Constructor
         * @param request   the CSV request descriptor
         * @param colCount  the column count for this batch
         */
        private DataBatch(CsvSourceOptions<X> request, int colCount) {
            this( request.getRowAxisType(), request.getReadBatchSize(), colCount);
        }


        private DataBatch(Class<X> rowAxisType, int readBatchSize, int colCount) {
            this.keys = Array.of(rowAxisType, readBatchSize);
            this.data = new String[colCount][readBatchSize];
        }

        /**
         * Returns the row count for this batch
         * @return  the populated row count
         */
        protected int rowCount() {
            return rowCount;
        }

        /**
         * Returns the keys for this batch
         * @return  the keys for this batch
         */
        protected Array<X> keys() {
            return keys;
        }

        /**
         * Returns the vector of column data for the index
         * @param colIndex  the column index
         * @return          the column vector
         */
        private String[] colData(int colIndex) {
            return data[colIndex];
        }

        /**
         * Resets this batch so that it can be used again
         */
        protected void clear() {
            this.rowCount = 0;
            this.keys.fill(null);
            for (int i=0; i<data.length; ++i) {
                for (int j=0; j<data[i].length; j++) {
                    this.data[i][j] = null;
                }
            }
        }

        /**
         * Adds a row to this batch with the key provided
         * @param rowKey    the row key
         * @param rowValues the row value tokens
         */
        private void addRow(X rowKey, String[] rowValues) {
            this.keys.setValue(rowCount, rowKey);
            for (int i=0; i<rowValues.length; ++i) {
                this.data[i][rowCount] = rowValues[i];
            }
            this.rowCount++;
        }

        /**
         * Adds a row to this batch with the key provided
         * @param rowKey    the row key
         * @param rowValues the row value tokens
         */
        private void addRow(int rowKey, String[] rowValues) {
            this.keys.setInt(rowCount, rowKey);
            for (int i=0; i<rowValues.length; ++i) {
                this.data[i][rowCount] = rowValues[i];
            }
            this.rowCount++;
        }

    }


    public static void main(String[] args) {
        DataFrame<String,String> frame = DataFrame.read().csv(options -> {
            options.setHeader(true);
            options.setReadBatchSize(100);
            options.setExcludeColumnIndexes(0);
            options.setResource("/Users/witdxav/d3x/sedol-to-cusip.csv");
            options.setRowKeyParser(String.class, row -> row[0]);
        });

        frame.out().print();
    }

}
