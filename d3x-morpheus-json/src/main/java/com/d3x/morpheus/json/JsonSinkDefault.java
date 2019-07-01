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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Resource;
import com.google.gson.stream.JsonWriter;

/**
 * A JsonSink.Handler that outputs a Morpheus proprietary JSON format
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
public class JsonSinkDefault<R,C> extends JsonSinkBase<R,C> {


    @Override
    public void write(JsonWriter writer, DataFrame<R,C> frame, Options options) {
        try {
            writer.beginObject();
            writer.name("rowCount").value(frame.rowCount());
            writer.name("colCount").value(frame.colCount());
            writer.name("rowType").value(getTypeName(frame.rows().keyType()));
            writer.name("colType").value(getTypeName(frame.cols().keyType()));
            writer.name("columns");
            writeColumns(writer, frame, options);
            writer.name("data");
            writeData(writer, frame, options);
            writer.endObject();
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame to JSON output", ex);
        }
    }


    /**
     * Writes the column definitions for the data frame
     * @param writer    the json writer
     * @param frame     the data frame
     * @param options   the output options
     */
    private void writeColumns(JsonWriter writer, DataFrame<R,C> frame, Options options) throws IOException {
        writer.beginArray();
        frame.cols().forEach(v -> {
            try {
                var formats = options.getFormats();
                writer.beginObject();
                writer.name("key").value(formats.format(v.key()));
                writer.name("keyType").value(getTypeName(v.key().getClass()));
                writer.name("dataType").value(getTypeName(v.typeInfo()));
                writer.name("ordinal").value(v.ordinal());
                writer.name("default").nullValue();
                writer.name("sparse").value(false);
                writer.endObject();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to serialize DataFrame to json", ex);
            }
        });
        writer.endArray();
    }


    /**
     * Writes the data for the data frame
     * @param writer    the json writer
     * @param frame     the data frame
     * @param options   the output options
     */
    void writeData(JsonWriter writer, DataFrame<R,C> frame, Options options) throws IOException {
        var formats = options.getFormats();
        var types = frame.cols().types().map(ArrayType::of).collect(Collectors.toList());
        writer.beginObject();
        frame.rows().forEach(row -> {
            var rowKey = row.key();
            try {
                writer.name(formats.format(rowKey));
                writer.beginObject();
                for (int i=0; i<frame.colCount(); ++i) {
                    if (!row.isNullAt(i)) {
                        var dataType = types.get(i);
                        writer.name("#" + i);
                        switch (dataType) {
                            case BOOLEAN:   writer.value(row.getBooleanAt(i));          break;
                            case INTEGER:   writer.value(row.getIntAt(i));              break;
                            case LONG:      writer.value(row.getLongAt(i));             break;
                            case DOUBLE:    writer.value(row.getDoubleAt(i));           break;
                            case STRING:    writer.value(row.<String>getValueAt(i));    break;
                            default:
                                var value = row.getValueAt(i);
                                var text = formats.format(value);
                                writer.value(text);
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
    }



    /**
     * Returns the type name for the data type
     * @param type  the data type
     * @return      the type name
     */
    private String getTypeName(Class<?> type) {
        if (type.equals(Boolean.class)) return "boolean";
        else if (type.equals(Integer.class)) return "integer";
        else if (type.equals(Long.class)) return "long";
        else if (type.equals(Double.class)) return "double";
        else if (type.equals(String.class)) return "string";
        else if (type.equals(LocalDate.class)) return "date";
        else if (type.equals(LocalTime.class)) return "time";
        else if (type.equals(LocalDateTime.class)) return "datetime";
        else if (type.equals(ZonedDateTime.class)) return "datetime-tz";
        else if (type.equals(ZoneId.class)) return "zone-id";
        else if (type.equals(TimeZone.class)) return "timezone";
        else if (type.equals(Currency.class)) return "currency";
        else if (type.isEnum()) return "enum:" + type.getName();
        else return type.getName();
    }



    public static void main(String[] args) {
        var range = Range.of(LocalDate.parse("2019-05-01"), LocalDate.parse("2019-05-10"));
        var columns = IntStream.range(0, 10).mapToObj(i -> "Column-" + i).collect(Collectors.toList());
        var frame = DataFrame.ofDoubles(range, columns, v -> Math.random());
        var sink = new JsonSinkDefault<LocalDate,String>();
        var os = new ByteArrayOutputStream(1024 * 10);
        sink.write(frame, Options.create(v -> {
            v.resource(Resource.of(os));
            v.pretty(true);
        }));
        IO.println(new String(os.toByteArray()));
    }

}
