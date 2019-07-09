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


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.Resource;
import com.google.gson.stream.JsonWriter;

/**
 * A JsonSink implementation that writes json compatible with Pandas "index" json format
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
public class JsonSinkIndex<R,C> extends JsonSinkBase<R,C> {

    @Override()
    public synchronized void write(JsonWriter writer, DataFrame<R,C> frame, Options options) {
        try {
            writer.beginObject();
            var rowType = frame.rows().keyClass();
            var colType = frame.cols().keyClass();
            var formats = options.getFormats();
            var rowPrinter = formats.getPrinter(rowType);
            var colPrinter = formats.getPrinter(colType);
            var types = frame.cols().types().map(ArrayType::of).collect(Collectors.toList());
            var printers = frame.cols().types().map(formats::getPrinter).collect(Collectors.toList());
            frame.rows().forEach(row -> {
                var rowKey = row.key();
                try {
                    writer.name(rowPrinter.apply(rowKey));
                    writer.beginObject();
                    for (int i=0; i<frame.colCount(); ++i) {
                        var dataType = types.get(i);
                        var colKey = frame.cols().key(i);
                        writer.name(colPrinter.apply(colKey));
                        if (row.isNullAt(i)) {
                            writer.nullValue();
                        } else {
                            switch (dataType) {
                                case BOOLEAN:   writer.value(row.getBooleanAt(i));          break;
                                case INTEGER:   writer.value(row.getIntAt(i));              break;
                                case LONG:      writer.value(row.getLongAt(i));             break;
                                case DOUBLE:    writer.value(row.getDoubleAt(i));           break;
                                case STRING:    writer.value(row.<String>getValueAt(i));    break;
                                default:
                                    var value = row.getValueAt(i);
                                    var printer = printers.get(i);
                                    writer.value(printer.apply(value));
                                    break;
                            }
                        }
                    }
                    writer.endObject();
                } catch (IOException ex) {
                    throw new DataFrameException("Failed to serialize DataFrame row to json: " + rowKey, ex);
                }
            });
            writer.endObject();
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame to JSON output", ex);
        }
    }


    public static void main(String[] args) throws Exception {
        var range = Range.of(LocalDate.parse("2000-01-01"), LocalDate.parse("2019-05-10"));
        var columns = IntStream.range(0, 100).mapToObj(i -> "Column-" + i).collect(Collectors.toList());
        var frame = DataFrame.ofDoubles(range, columns, v -> Math.random());
        var sink = new JsonSinkIndex<LocalDate,String>();
        var os = new BufferedOutputStream(new FileOutputStream(new File("frame-as-index.json")));
        sink.write(frame, Options.create(v -> {
            v.resource(Resource.of(os));
            v.pretty(true);
        }));
    }

}