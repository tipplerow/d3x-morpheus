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
package com.d3x.morpheus.docs.basic;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

import com.d3x.morpheus.db.DbSink;
import com.d3x.morpheus.json.JsonSink;
import org.testng.annotations.Test;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameSink;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Initialiser;
import com.d3x.morpheus.util.Try;
import com.d3x.morpheus.util.functions.Function1;
import com.d3x.morpheus.util.http.HttpClient;
import com.d3x.morpheus.util.text.printer.Printer;

public class WritingDocs {

    @Test()
    public void testCars() {
        DataFrame.read().<Integer>csv(options -> {
            options.setResource("http://zavtech.com/data/samples/cars93.csv");
            options.setRowKeyParser(Integer.class, values -> Integer.parseInt(values[0]));
            options.setExcludeColumnIndexes(0);
        }).cols().add("WeightKG", Double.class, value -> {
            return value.row().getDouble("Weight") / 1.5d;
        }).rows().select(row -> {
           return row.getDouble("Width") > 70d;
        }).write().csv(options -> {

        });
    }


    private DataFrame<LocalDate,String> frame() {
        var start = LocalDate.of(2014, 1, 1);
        var rowKeys = Range.of(start, start.plusDays(10));
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("Column-0", Boolean.class, v -> Math.random() > 0.5d);
            columns.add("Column-1", Double.class, v -> Math.random() * 10d);
            columns.add("Column-2", LocalTime.class, v -> LocalTime.now().plusMinutes(v.rowOrdinal()));
            columns.add("Column-3", Month.class, v -> Month.values()[v.rowOrdinal()+1]);
            columns.add("Column-4", Integer.class, v -> (int)(Math.random() * 10));
            columns.add("Column-5", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
            columns.add("Column-6", LocalDateTime.class, v -> LocalDateTime.now().plusMinutes(v.rowOrdinal()).minusYears(3).plusMonths(5));
        });
    }


    @Test()
    public void writeCsv1() {
        var frame = frame();
        frame.write().csv(options -> {
            options.setFile("/Users/witdxav/temp/test.csv");
            options.setSeparator(",");
            options.setIncludeRowHeader(true);
            options.setIncludeColumnHeader(true);
            options.setNullText("null");
            options.setTitle("Date");
            options.setRowKeyPrinter(Printer.ofLocalDate("yyyy-MM-dd"));
            options.withFormats(formats -> {
                var timeFormat = DateTimeFormatter.ofPattern("HH:mm");
                var dateTimeFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
                formats.setDecimalFormat(Double.class, "0.00##;-0.00##", 1);
                formats.setPrinter("Column-3", Printer.ofEnum());
                formats.setPrinter("Column-2", Printer.ofLocalTime(timeFormat));
                formats.setPrinter("Column-6", Printer.ofLocalDateTime(dateTimeFormat));
            });
        });
    }


    @Test()
    public void writeCsv2() {
        var frame = frame();
        var bytes = new ByteArrayOutputStream(1024 * 100);
        frame.write().csv(options -> {
            options.setOutputStream(bytes);
            options.setSeparator(",");
            options.setIncludeRowHeader(true);
            options.setIncludeColumnHeader(true);
            options.setNullText("null");
            options.setTitle("Date");
            options.setRowKeyPrinter(Printer.ofLocalDate("yyyy-MM-dd"));
            options.withFormats(formats -> {
                var timeFormat = DateTimeFormatter.ofPattern("HH:mm");
                var dateTimeFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
                formats.setDecimalFormat(Double.class, "0.00##;-0.00##", 1);
                formats.setPrinter("Column-3", Printer.ofEnum());
                formats.setPrinter("Column-2", Printer.ofLocalTime(timeFormat));
                formats.setPrinter("Column-6", Printer.ofLocalDateTime(dateTimeFormat));
            });
        });

        IO.println(new String(bytes.toByteArray()));
    }


    @Test()
    public void writeJson() {
        var frame = frame();
        var jsonSink = new JsonSink();
        var bytes = new ByteArrayOutputStream(1024 * 100);
        jsonSink.write(frame, options -> {
            options.setOutputStream(bytes);
            options.setEncoding("UTF-8");
            options.withFormats(formats -> {
                formats.setTimeFormat("Column-2", "HH:mm");
                formats.setDateTimeFormat("Column-6", "dd-MMM-yyyy HH:mm");
                formats.setDecimalFormat(Double.class, "0.00##;-0.00##", 1);
                formats.setPrinter("Column-3", Printer.<Month>forObject(v -> v.name().toLowerCase()));
            });
        });

        IO.println(new String(bytes.toByteArray()));
    }


    @Test()
    public void writeDb() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        var frame = frame();
        var dbSink = new DbSink();
        dbSink.write(frame, options -> {
            options.setBatchSize(1000);
            options.setTableName("TestTable");
            options.setConnection("jdbc:hsqldb:/Users/witdxav/morpheus/tests/DataFrame_3.db", "sa", null);
            options.setRowKeyMapping("Date", Date.class, Function1.toValue(Date::valueOf));
            options.setColumnMappings(mappings -> {
                mappings.add(Month.class, String.class, Function1.toValue(v -> {
                    return v.<Month>getValue().name().toLowerCase();
                }));
                mappings.add(LocalDate.class, java.sql.Date.class, Function1.toValue(v -> {
                    return Date.valueOf(v.<LocalDate>getValue());
                }));
            });
            options.setColumnNames(colKey -> {
                switch (colKey) {
                    case "Column-1":    return "Column-A";
                    case "Column-2":    return "Column-B";
                    case "Column-3":    return "Column-C";
                    default:            return colKey;
                }
            });
        });
    }



    @Test()
    public void postCsvToHttpEndPoint() throws IOException {

        var start = LocalDate.of(2014, 1, 1);
        var rowKeys = Range.of(start, start.plusDays(10));
        DataFrame<LocalDate,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("Column-0", Double.class, v -> Math.random());
            columns.add("Column-1", LocalTime.class, v -> LocalTime.now().plusMinutes(v.rowOrdinal()));
            columns.add("Column-2", Month.class, v -> Month.values()[v.rowOrdinal()+1]);
            columns.add("Column-3", Integer.class, v -> (int)(Math.random() * 10));
            columns.add("Column-4", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
            columns.add("Column-5", LocalDateTime.class, v -> LocalDateTime.now().plusMinutes(v.rowOrdinal()));
        });

        var baos = new ByteArrayOutputStream();
        frame.write().csv(options -> {
            try {
                options.setOutputStream(new GZIPOutputStream(baos));
                options.setSeparator(",");
                options.setIncludeRowHeader(true);
                options.setIncludeColumnHeader(true);
                options.setNullText("null");
                options.setTitle("Date");
                options.setRowKeyPrinter(Printer.forObject(LocalDate::toString));
                options.withFormats(formats -> {
                    formats.setDecimalFormat(Double.class, "0.00##;-0.00##", 1);
                    formats.setTimeFormat("Column-1", "HH:mm");
                    formats.setDateTimeFormat("Column-5", "dd-MMM-yyyy HH:mm");
                    formats.setPrinter("Column-2", Printer.<Month>forObject(v -> v.name().toLowerCase()));
                });
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        var bytes = baos.toByteArray();
        HttpClient.getDefault().doPost(post -> {
            post.setRetryCount(5);
            post.setReadTimeout(5000);
            post.setConnectTimeout(1000);
            post.setUrl("http://www.domain.con/test");
            post.setContent(bytes);
            post.setContentType("application/x-gzip");
            post.setContentLength(bytes.length);
            post.setResponseHandler(response -> {
                if (response.getStatus().getCode() == 200) {
                    return Optional.empty();
                } else {
                    throw new RuntimeException("Failed with response: " + response.getStatus().getCode());
                }
            });
        });
    }


    @Test()
    public void writeCustom() {
        var frame = frame();
        frame.write().to(new CustomSink<>(), options -> {
            try {
                options.setOutput(new FileOutputStream(new File("/Users/witdxav/test/DataFrame-1.gzip")));
                options.setCompressed(true);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });
    }



    @lombok.NoArgsConstructor()
    @lombok.AllArgsConstructor()
    @lombok.Builder(toBuilder = true)
    public static class CustomSinkOptions {
        @lombok.Getter @lombok.Setter private OutputStream output;
        @lombok.Getter @lombok.Setter private boolean compressed;
    }



    public class CustomSink<R,C> implements DataFrameSink<R,C,CustomSinkOptions> {
        @Override
        public void write(DataFrame<R,C> frame, Consumer<CustomSinkOptions> configurator) {
            ObjectOutputStream os = null;
            var options = Initialiser.apply(new CustomSinkOptions(), configurator);
            try {
                if (options.isCompressed()) {
                    os = new ObjectOutputStream(new GZIPOutputStream(options.getOutput()));
                    os.writeObject(frame);
                } else {
                    os = new ObjectOutputStream(options.getOutput());
                    os.writeObject(frame);
                }
            } catch (Exception ex) {
                throw new DataFrameException("Failed to write DataFrame to serialized output", ex);
            } finally {
                IO.close(os);
            }
        }
    }


}