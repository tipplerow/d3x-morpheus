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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.printer.Printer;
import com.google.gson.stream.JsonWriter;

/**
 * A JsonSink.Handler that outputs a Morpheus proprietary JSON format
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
class JsonSinkDefault<R,C> implements JsonSink<R,C> {

    private JsonWriter writer;
    private JsonSink.Options options;


    @Override
    public synchronized void write(DataFrame<R, C> frame, Options options) {
        try {
            final String encoding = options.getEncoding();
            final OutputStream os = options.getResource().toOutputStream();
            this.options = options;
            this.writer = new JsonWriter(new OutputStreamWriter(os, encoding));
            this.writer.setIndent("  ");
            this.writer.beginObject();
            this.writer.name("DataFrame");
            this.writer.beginObject();
            this.writer.name("rowCount").value(frame.rowCount());
            this.writer.name("colCount").value(frame.colCount());
            writeRowKeys(frame);
            writeColumns(frame);
            this.writer.endObject();
            this.writer.endObject();
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame to JSON output", ex);
        } finally {
            IO.close(writer);
        }
    }


    /**
     * Writes row keys to json writer
     * @param frame the frame reference
     */
    @SuppressWarnings("unchecked")
    private void writeRowKeys(DataFrame<?,?> frame) {
        try {
            final Formats formats = options.getFormats();
            final Class keyType = frame.rows().keyType();
            writer.name("rowKeys");
            writer.beginObject();
            writer.name("type").value(keyType.getSimpleName());
            writer.name("values");
            writer.beginArray();
            final Printer<Object> format = formats.getPrinterOrFail(keyType, null);
            frame.rows().keys().forEach(rowKey -> {
                try {
                    writer.value(format.apply(rowKey));
                } catch (IOException ex) {
                    throw new DataFrameException("Failed to write DataFrame row key: " + rowKey, ex);
                }
            });
            writer.endArray();
            writer.endObject();
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write row keys for DataFrame", ex);
        }
    }


    /**
     * Writes column data to the json writer
     * @param frame     the frame reference
     */
    private void writeColumns(DataFrame<?,?> frame) {
        try {
            writer.name("columns");
            writer.beginArray();
            frame.cols().forEach(this::writeColumn);
            writer.endArray();
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame columns to json", ex);
        }
    }

    /**
     * Writes data for a specific column to the json writer
     * @param column    the column reference
     */
    @SuppressWarnings("unchecked")
    private void writeColumn(DataFrameColumn<?,?> column) {
        try {
            final Object colKey = column.key();
            final Formats formats = options.getFormats();
            final Class<?> dataType = column.typeInfo();
            final ArrayType type = ArrayType.of(dataType);
            final Object defaultValue = ArrayType.defaultValue(dataType);
            final Printer<Object> keyPrinter = formats.getPrinterOrFail(colKey.getClass());
            final Printer<Object> valuePrinter = formats.getPrinterOrFail(colKey, dataType);
            writer.beginObject();
            writer.name("key").value(keyPrinter.apply(colKey));
            writer.name("keyType").value(colKey.getClass().getSimpleName());
            writer.name("dataType").value(dataType.getSimpleName());
            if (type.isBoolean()) {
                final boolean defaultBoolean = defaultValue == null ? false : (Boolean)defaultValue;
                writer.name("defaultValue").value(defaultBoolean);
                writer.name("values").beginArray();
                column.forEachValue(v -> {
                    try {
                        writer.value(v.getBoolean());
                    } catch (IOException ex) {
                        throw new DataFrameException("Failed to write DataFrame values for column " + colKey, ex);
                    }
                });

            } else if (type.isInteger()) {
                final Number defaultNumber = defaultValue == null ? 0 : (Number) defaultValue;
                writer.name("defaultValue").value(defaultNumber);
                writer.name("values").beginArray();
                column.forEachValue(v -> {
                    try {
                        writer.value(v.getInt());
                    } catch (IOException ex) {
                        throw new DataFrameException("Failed to write DataFrame values for column " + colKey, ex);
                    }
                });
            } else if (type.isLong()) {
                final Number defaultNumber = defaultValue == null ? 0 : (Number)defaultValue;
                writer.name("defaultValue").value(defaultNumber);
                writer.name("values").beginArray();
                column.forEachValue(v -> {
                    try {
                        writer.value(v.getLong());
                    } catch (IOException ex) {
                        throw new DataFrameException("Failed to write DataFrame values for column " + colKey, ex);
                    }
                });
            } else if (type.isDouble()) {
                final Number defaultNumber = defaultValue == null ? 0 : (Number)defaultValue;
                writer.name("defaultValue").value(Double.isNaN(defaultNumber.doubleValue()) ? null : defaultNumber);
                writer.name("values").beginArray();
                column.forEachValue(v -> {
                    try {
                        writer.value(v.getDouble());
                    } catch (IOException ex) {
                        throw new DataFrameException("Failed to write DataFrame values for column " + colKey, ex);
                    }
                });
            } else {
                writer.name("defaultValue").value(defaultValue == null ? null : valuePrinter.apply(defaultValue));
                writer.name("values").beginArray();
                column.forEachValue(v -> {
                    try {
                        final Object rawValue = v.getValue();
                        final String stringValue = valuePrinter.apply(rawValue);
                        writer.value(stringValue);
                    } catch (IOException ex) {
                        throw new DataFrameException("Failed to write DataFrame values for column " + colKey, ex);
                    }
                });
            }
            writer.endArray();
            writer.endObject();
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame values for column " + column.key(), ex);
        }
    }

}
