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
package com.d3x.morpheus.io;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.d3x.morpheus.json.JsonSink;
import com.d3x.morpheus.json.JsonSource;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
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
        return DataFrame.read().csv(options -> {
            options.setResource(resource);
            options.setExcludeColumnIndexes(0);
            options.setRowKeyParser(rowType, values -> {
                final String value = values[0];
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
    public <T> void testReadWrite(Class<T> rowType, String resource) throws Exception {
        final JsonSink sink = new JsonSink();
        final File file = File.createTempFile(UUID.randomUUID().toString(), ".json");
        final DataFrame<T,String> frame = load(rowType, resource);
        frame.out().print();
        sink.write(frame, o -> o.setFile(file));
        frame.out().print();
        readAndValidate(frame, file);
    }


    /**
     * Loads the DataFrame from the file and compares it to original
     * @param original  the original frame
     * @param file      the file to read from
     */
    private <T> void readAndValidate(DataFrame<T,String> original, File file) {
        final JsonSource source = new JsonSource();
        final DataFrame<T,String> result = source.read(file);
        original.out().print();
        result.out().print();
        DataFrameAsserts.assertEqualsByIndex(original, result);
    }



}
