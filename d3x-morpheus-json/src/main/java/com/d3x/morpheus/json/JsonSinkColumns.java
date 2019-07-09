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

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.printer.Printer;
import com.google.gson.stream.JsonWriter;

/**
 * A JsonSink.Handler that writes a DataFrame using a JSON format compatible with Pandas "columns" orientation
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
class JsonSinkColumns<R,C> implements JsonSink<R,C> {

    private JsonWriter writer;


    @Override
    public synchronized void write(DataFrame<R,C> frame, Options options) {
        try {
            final String encoding = options.getEncoding();
            final OutputStream os = options.getResource().toOutputStream();
            final Formats formats = options.getFormats();
            final Printer<Object> rowPrinter = formats.getPrinterOrFail(frame.rows().keyClass());
            writer = new JsonWriter(new OutputStreamWriter(os, encoding));
            writer.setIndent("  ");
            writer.beginObject();
            for (DataFrameColumn<?,?> column : frame.cols()) {
                final Object colKey = column.key();
                final Class<?> keyType = colKey.getClass();
                final Class<?> valueType = column.dataClass();
                final String colName = formats.getPrinterOrFail(keyType).apply(colKey);
                writer.name(colName);
                writer.beginObject();
                if (valueType == Boolean.class) {
                    for (DataFrameValue<?,?> value : column) {
                        final Object rowKey = value.rowKey();
                        writer.name(rowPrinter.apply(rowKey));
                        writer.value(value.getBoolean());
                    }
                } else if (valueType == Integer.class) {
                    for (DataFrameValue<?,?> value : column) {
                        final Object rowKey = value.rowKey();
                        writer.name(rowPrinter.apply(rowKey));
                        writer.value(value.getInt());
                    }
                } else if (valueType == Long.class) {
                    for (DataFrameValue<?,?> value : column) {
                        final Object rowKey = value.rowKey();
                        writer.name(rowPrinter.apply(rowKey));
                        writer.value(value.getLong());
                    }
                } else if (valueType == Double.class) {
                    for (DataFrameValue<?,?> value : column) {
                        final Object rowKey = value.rowKey();
                        writer.name(rowPrinter.apply(rowKey));
                        writer.value(value.getDouble());
                    }
                } else {
                    final Printer<Object> valuePrinter = formats.getPrinterOrFail(valueType);
                    for (DataFrameValue<?,?> value : column) {
                        final Object rowKey = value.rowKey();
                        writer.name(rowPrinter.apply(rowKey));
                        writer.value(valuePrinter.apply(value.getValue()));
                    }
                }
                writer.endObject();
            }
            writer.endObject();
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame to JSON output", ex);
        } finally {
            IO.close(writer);
        }
    }

}
