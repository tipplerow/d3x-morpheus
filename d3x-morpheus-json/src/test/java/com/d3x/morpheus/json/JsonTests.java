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

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.util.Resource;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for reading / writing DataFrame JSON files
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class JsonTests {

    private static final File testDir = new File(System.getProperty("user.home"), "builds/tests/morpheus/json");

    static {
        testDir.mkdirs();
    }


    @DataProvider(name="ticker")
    public Object[][] getTickers() {
        return new Object[][] { {"blk"}, {"csco"}, {"spy"}, {"yhoo"} };
    }

    @DataProvider(name="types")
    public Object[][] types() {
        return new Object[][] {
            { String.class, "/csv/frame-with-string-index.csv" },
            { Integer.class, "/csv/frame-with-int-index.csv" },
            { LocalDate.class, "/csv/frame-with-local-date-index.csv" },
            { LocalTime.class, "/csv/frame-with-local-time-index.csv" },
            { LocalDateTime.class, "/csv/frame-with-local-date-time-index.csv" },
            { ZonedDateTime.class, "/csv/frame-with-zoned-date-time-index.csv" }
        };
    }


    /**
     * Loads a DataFrame from the resource specified
     * @param rowType   the row type for index
     * @param resource  the resource path
     * @return          the newly loaded frame
     */
    @SuppressWarnings("unchecked")
    private <T> DataFrame<T,String> load(Class<T> rowType, String resource) {
        return DataFrame.read().<T>csv(resource).read(options -> {
            options.setRowKeyColumnIndex(0);
            options.setParser("DataFrame", rowType, value -> {
                if (rowType == String.class) {
                    return (T)value;
                } else if (rowType == Integer.class) {
                    return (T)Integer.valueOf(Integer.parseInt(value));
                } else if (rowType == LocalTime.class) {
                    return (T)LocalTime.parse(value);
                } else if (rowType == LocalDate.class) {
                    return (T)LocalDate.parse(value);
                } else if (rowType == LocalDateTime.class) {
                    return (T)LocalDateTime.parse(value);
                } else if (rowType == ZonedDateTime.class) {
                    return (T)ZonedDateTime.parse(value);
                } else {
                    return (T)value;
                }
            });
        });
    }


    @Test(dataProvider="types")
    public <T> void testReadWriteDefault(Class<T> rowType, String resource) {
        final JsonSink<T,String> sink = JsonSink.create();
        final DataFrame<T,String> frame = load(rowType, resource);
        final File file = new File(testDir, String.format("DataFrame-default-%s.json", rowType.getSimpleName()));
        sink.write(frame, JsonStyle.DEFAULT, file);
        readAndValidate(frame, JsonStyle.DEFAULT, file);
    }


    @Test(dataProvider="types")
    public <T> void testReadWriteSplit(Class<T> rowType, String resource) {
        final JsonSink<T,String> sink = JsonSink.create();
        final DataFrame<T,String> frame = load(rowType, resource);
        final File file = new File(testDir, String.format("DataFrame-split-%s.json", rowType.getSimpleName()));
        sink.write(frame, JsonStyle.SPLIT, file);
        //readAndValidate(frame, JsonStyle.SPLIT, file);
    }


    @Test(dataProvider="types")
    public <T> void testReadWriteColumns(Class<T> rowType, String resource) {
        final JsonSink<T,String> sink = JsonSink.create();
        final DataFrame<T,String> frame = load(rowType, resource);
        final File file = new File(testDir, String.format("DataFrame-columns-%s.json", rowType.getSimpleName()));
        sink.write(frame, JsonStyle.COLUMNS, file);
        //readAndValidate(frame, JsonStyle.COLUMNS, file);
    }


    /**
     * Loads the DataFrame from the file and compares it to original
     * @param original  the original frame
     * @param style     the json style
     * @param file      the file to read from
     */
    private <T> void readAndValidate(DataFrame<T,String> original, JsonStyle style, File file) {
        final JsonSource<T,String> source = JsonSource.create();
        final DataFrame<T,String> result = source.read(options -> {
            options.resource(Resource.of(file));
            options.style(style);
        });
        original.out().print();
        result.out().print();
        DataFrameAsserts.assertEqualsByIndex(original, result);
    }





}
