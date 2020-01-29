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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.range.Range;
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
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.extern.slf4j.Slf4j()
@lombok.AllArgsConstructor()
public class CsvSourceDefault implements CsvSource {

    @lombok.NonNull
    private Resource resource;


    @Override
    @SuppressWarnings("unchecked")
    public DataFrame<Integer,String> read() throws DataFrameException {
        return read(Integer.class, o -> {
            o.setHeader(true);
        });
    }

    @Override
    public <R> DataFrame<R,String> read(Class<R> rowType, Consumer<Options> configurator) throws DataFrameException {
        try {
            var options = new Options();
            configurator.accept(options);
            switch (resource.getType()) {
                case FILE:          return parse(rowType, options, resource.toInputStream());
                case URL:           return parse(rowType, options, resource.asURL());
                case INPUT_STREAM:  return parse(rowType, options, resource.toInputStream());
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
    private <R> DataFrame<R,String> parse(Class<R> rowType, Options options, URL url) throws IOException {
        Objects.requireNonNull(url, "The URL cannot be null");
        if (!url.getProtocol().startsWith("http")) {
            return parse(rowType, options, url.openStream());
        } else {
            return HttpClient.getDefault().<DataFrame<R,String>>doGet(httpRequest -> {
                httpRequest.setUrl(url);
                httpRequest.setResponseHandler(response -> {
                    try (InputStream stream = response.getStream()) {
                        final DataFrame<R,String> frame = parse(rowType, options, stream);
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
     * @param rowType   the row type
     * @param options   the CSV options
     * @param stream    the stream to parse
     * @return          the DataFrame parsed from stream
     * @throws IOException      if there stream read error
     */
    private <R> DataFrame<R,String> parse(Class<R> rowType, Options options, InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, options.getCharset()))) {
            var handler = new CsvProcessor<R>(options);
            var settings = new CsvParserSettings();
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
            settings.setReadInputOnSeparateThread(true);
            var parser = new CsvParser(settings);
            parser.parse(reader);
            return handler.build();
        }
    }





    /**
     * A RowProcessor that receives callbacks and incrementally builds the DataFrame.
     */
    private class CsvProcessor<R> implements RowProcessor {

        @lombok.Getter
        private int rowCounter;
        @lombok.Getter
        private long startTime;
        @lombok.Getter
        private long endTime;
        private List<CsvColumn> columns;
        private Options options;
        private Predicate<String[]> rowPredicate;

        /**
         * Constructor
         * @param options   the options
         */
        CsvProcessor(Options options) {
            this.options = options;
            this.rowPredicate = options.getRowPredicate();
        }


        /**
         * Initializes the columns that will injest data from the CSV stream
         * @param colCount      the column count in source
         * @param context       the parsing context
         */
        private void initColumns(int colCount, ParsingContext context) {
            this.columns = new ArrayList<>();
            var headers = options.isHeader() ? context.headers() : IntStream.range(0, colCount).mapToObj(i -> "Column-" + i).toArray(String[]::new);
            for (int colIndex=0; colIndex<colCount; ++colIndex) {
                var colName = Optional.ofNullable(headers[colIndex]).orElse(String.format("Column-%s", colIndex));
                if (options.include(colName, colIndex)) {
                    var formats = options.getFormats();
                    var buffer = options.getReadBatchSize();
                    var capacity = options.getRowCapacity();
                    var parser = options.getParser(colName).orElse(null);
                    if (parser==null){
                        log.info("getting parser by column index:"+colIndex);
                        parser = options.getFormats().getParser(colIndex);
                    }
                    var column = new CsvColumn(colName, colIndex, capacity, buffer, formats, parser);
                    this.columns.add(column);
                }
            }
        }


        @Override
        public void processStarted(ParsingContext context) {
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void processEnded(ParsingContext context) {
            this.columns.forEach(CsvColumn::flush);
            this.endTime = System.currentTimeMillis();
        }


        @Override
        public void rowProcessed(String[] row, ParsingContext context) {
            try {
                if (columns == null) {
                    initColumns(row.length, context);
                }
                if (rowPredicate == null || rowPredicate.test(row)) {
                    this.rowCounter++;
                    if (rowCounter % 10000 == 0) {
                        var time = System.currentTimeMillis() - startTime;
                        IO.println("Loaded " + rowCounter + " rows in " + time + " millis");
                    }
                    for (CsvColumn column : columns) {
                        column.apply(row);
                    }
                }
            } catch (DataFrameException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new DataFrameException("Failed to parse row: " + Arrays.toString(row), ex);
            }
        }


        /**
         * Builds the data frame from the contents in this handler
         * @return      the newly built data frame
         */
        @SuppressWarnings("unchecked")
        private DataFrame<R,String> build() {
            try {
                if (options.getRowKeyColumnName() != null) {
                    var rowKeyColumn = options.getRowKeyColumnName();
                    var rowColumn = columns.stream().filter(v -> v.name.equals(rowKeyColumn)).findFirst().orElse(null);
                    if (rowColumn == null) throw new DataFrameException("No column in content matching: " + rowKeyColumn);
                    var rowKeys = (Array<R>)rowColumn.toArray();
                    var selection = columns.stream().filter(v -> !v.name.equals(rowKeyColumn));
                    return DataFrame.of(rowKeys, String.class, cols -> selection.forEach(v -> {
                        var values = v.toArray();
                        cols.add(v.name, values);
                    }));
                } else if (options.getRowKeyColumnIndex() != null) {
                    var rowKeyIndex = options.getRowKeyColumnIndex();
                    var rowColumn = columns.get(rowKeyIndex);
                    var rowKeys = (Array<R>)rowColumn.toArray();
                    var selection = columns.stream().filter(v -> !v.name.equals(rowColumn.name));
                    return DataFrame.of(rowKeys, String.class, cols -> selection.forEach(v -> {
                        var values = v.toArray();
                        cols.add(v.name, values);
                    }));
                } else {
                    var rowKeys = (Range<R>)Range.of(0, rowCounter);
                    return DataFrame.of(rowKeys, String.class, cols -> columns.forEach(v -> {
                        var values = v.toArray();
                        cols.add(v.name, values);
                    }));
                }
            } catch (DataFrameException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new DataFrameException("Failed to process CSV parse end", ex);
            }
        }
    }



    /**
     * A class that represents a column in a CSV parse operation
     */
    private static class CsvColumn {

        private int index;
        private String name;
        private int bufferSize;
        private Formats formats;
        private Parser<?> parser;
        private List<String> buffer;
        private ArrayBuilder<Object> array;

        /**
         * Constructor
         * @param name          the label for column
         * @param index         the index for column
         * @param capacity      the initial capacity
         * @param bufferSize    the buffer size
         * @param parser        the optional parser for column, otherwise auto resolve from contents
         */
        @SuppressWarnings("unchecked")
        CsvColumn(String name, int index, int capacity, int bufferSize, Formats formats, Parser<?> parser) {
            this.index = index;
            this.name = name;
            this.bufferSize = bufferSize;
            this.formats = formats;
            this.parser = parser;
            this.buffer = new ArrayList<>(bufferSize);
            this.array = parser == null ? ArrayBuilder.of(capacity) : (ArrayBuilder<Object>)ArrayBuilder.of(capacity, parser.getType());
        }


        /**
         * Returns the array of values for this column
         * @return      the array of values
         */
        final Array<?> toArray() {
            return array.toArray();
        }


        /**
         * Called to apply a row parsed from CSV stream
         * @param row   the parsed row tokens
         */
        final void apply(String[] row) {
            var value = row[index];
            this.buffer.add(value);
            if (buffer.size() >= bufferSize) {
                this.flush();
            }
        }

        /**
         * Resolves an appropriate parser based on contents of buffer
         */
        private void resolveParser() {
            if (parser == null) {
                var stringParser = formats.getParserOrFail(String.class);
                this.parser = formats.findParser(buffer).orElse(stringParser);
            }
        }


        /**
         * Flushes the buffered values into the array builder
         */
        private void flush() {
            try {
                this.resolveParser();
                var dataType = ArrayType.of(parser.getType());
                switch (dataType) {
                    case BOOLEAN:   buffer.forEach(v -> array.appendBoolean(parser.applyAsBoolean(v)));    break;
                    case INTEGER:   buffer.forEach(v -> array.appendInt(parser.applyAsInt(v)));            break;
                    case LONG:      buffer.forEach(v -> array.appendLong(parser.applyAsLong(v)));          break;
                    case DOUBLE:    buffer.forEach(v -> array.appendDouble(parser.applyAsDouble(v)));      break;
                    default:        buffer.forEach(v -> array.append(parser.apply(v)));                    break;
                }
            } finally {
                buffer.clear();
            }
        }

        @Override
        public String toString() {
            return String.format("Column: %s @ %s", name, index);
        }
    }


    public static void main(String[] args) {
        var path = "/Users/witdxav/temp/opt-models/bf5489bf-18be-4442-8c49-d659207ceeee-data/opt-data.csv";
        var frame = DataFrame.read(path).csv(String.class, options -> {
            options.setHeader(true);
            options.setReadBatchSize(1000);
            options.setRowKeyColumnName("symbol");
        });
        frame.out().print();
    }

}
