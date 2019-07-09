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

import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.Initialiser;
import com.d3x.morpheus.util.Resource;

/**
 * The default implementation of the CsvSink interface
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.AllArgsConstructor()
public class CsvSinkDefault<R,C> implements CsvSink<R,C> {

    @lombok.NonNull
    private Resource resource;
    @lombok.NonNull
    private DataFrame<R,C> frame;


    @Override
    public void apply() {
        this.apply(o -> o.setNullText(""));
    }

    @Override()
    public void apply(Consumer<Options<R,C>> configurator) {
        var options = Initialiser.apply(new Options<>(), configurator);
        Objects.requireNonNull(options.getFormats(), "The CSV options formats cannot be null");
        Objects.requireNonNull(options.getSeparator(), "The CSV options separator cannot be null");
        try (OutputStream os = resource.toOutputStream()) {
            var formats = options.getFormats();
            var separator = options.getSeparator();
            if (options.isIncludeColumnHeader()) {
                writeHeader(frame, options, os);
            }
            var rowKeyType = frame.rows().keyClass();
            var cursor = frame.cursor();
            var rowKeyPrinter = Optional.ofNullable(options.getRowKeyPrinter()).orElse(formats.getPrinterOrFail(rowKeyType));
            var colPrinters = frame.cols().stream().map(c -> formats.getPrinterOrFail(c.key(), c.dataClass())).collect(Collectors.toList());
            for (int i = 0; i < frame.rowCount(); ++i) {
                var row = new StringBuilder();
                if (options.isIncludeRowHeader()) {
                    var rowKey = frame.rows().key(i);
                    row.append(rowKeyPrinter.apply(rowKey));
                    row.append(separator);
                }
                cursor.rowAt(i);
                for (int j = 0; j < frame.colCount(); ++j) {
                    var printer = colPrinters.get(j);
                    cursor.colAt(j);
                    switch (printer.getStyle()) {
                        case BOOLEAN:   row.append(printer.apply(cursor.getBoolean()));   break;
                        case INTEGER:   row.append(printer.apply(cursor.getInt()));       break;
                        case LONG:      row.append(printer.apply(cursor.getLong()));      break;
                        case DOUBLE:    row.append(printer.apply(cursor.getDouble()));    break;
                        default:
                            var text = printer.apply(cursor.getValue());
                            if (text != null && text.contains(",")) {
                                row.append("\"").append(text).append("\"");
                                break;
                            } else {
                                row.append(text);
                                break;
                            }
                    }
                    if (j < frame.colCount() - 1) {
                        row.append(separator);
                    } else {
                        row.append("\n");
                        os.write(row.toString().getBytes());
                    }
                }
            }
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame to CSV output", ex);
        }
    }

    /**
     * Writes the frame column header to the output stream
     * @param frame     the frame to write headers for
     * @param options   the options to tailor output
     * @param os        the output stream to write to
     * @throws DataFrameException  if there is a write error
     */
    private void writeHeader(DataFrame<R,C> frame, Options options, OutputStream os) {
        try {
            var header = new StringBuilder();
            if (options.isIncludeRowHeader()) {
                header.append(options.getTitle());
                header.append(options.getSeparator());
            }
            var formats = options.getFormats();
            var printer = formats.getPrinterOrFail(frame.cols().keyClass());
            for (int i = 0; i<frame.colCount(); ++i) {
                var column = frame.cols().key(i);
                header.append(printer.apply(column));
                if (i<frame.colCount()-1) {
                    header.append(options.getSeparator());
                } else {
                    header.append("\n");
                    os.write(header.toString().getBytes());
                }
            }
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame header to CSV", ex);
        }
    }

}
