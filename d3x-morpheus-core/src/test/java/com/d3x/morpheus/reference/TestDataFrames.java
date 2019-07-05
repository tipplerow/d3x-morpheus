/*
 * Copyright (C) 2014-2017 Xavier Witdouck
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
package com.d3x.morpheus.reference;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.Predicates;
import com.d3x.morpheus.util.text.parser.Parser;
import org.testng.annotations.Test;

/**
 * A provider of various kinds of DataFrame configurations used across various tests.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class TestDataFrames {

    /**
     * Returns a randomized DataFrame of based on the args specified
     * @param type      the type for all columns
     * @param rowCount  the row count
     * @param colCount  the column count
     * @return          the newly created DataFrame
     */
    public static DataFrame<String,String> random(Class type, int rowCount, int colCount) {
        final Range<String> rowKeys = Range.of(0, rowCount).map(i -> "R" + i);
        final Range<String> colKeys = Range.of(0, colCount).map(i -> "C" + i);
        return random(type, rowKeys, colKeys);
    }

    /**
     * Returns a randomized DataFrame of based on the args specified
     * @param type      the type for all columns
     * @param rowCount  the row count
     * @param colCount  the column count
     * @return          the newly created DataFrame
     */
    public static DataFrame<Integer,Integer> random2(Class type, int rowCount, int colCount) {
        final Range<Integer> rowKeys = Range.of(0, rowCount);
        final Range<Integer> colKeys = Range.of(0, colCount);
        return random(type, rowKeys, colKeys);
    }


    /**
     * Returns a DataFrame for the ticker specified
     * @param ticker        the ticker reference
     * @return              the DataFrame result
     * @throws java.io.IOException  if there is an IO exception
     */
    public static DataFrame<LocalDate,String> getQuotes(String ticker) throws IOException {
        var path = String.format("/quotes/%s.csv", ticker.toLowerCase());
        return DataFrame.read(path).csv(LocalDate.class, options -> {
            options.setRowKeyColumnName("Date");
            options.getFormats().setParser("Date", Parser.ofLocalDate("yyyy-MM-dd"));
            options.setColNamePredicate(Predicates.in("Date", "Open", "High", "Low", "Close", "Volume", "Adj Close"));
        });
    }


    /**
     * Returns a newly created DataFrame initialized with the arguments specified
     * @param type      the type for all columns
     * @param rowKeys      the row keys
     * @param colKeys      the column keys
     * @return          the newly created DataFrame
     */
    public static <R,C> DataFrame<R,C> random(Class type, Iterable<R> rowKeys, Iterable<C> colKeys) {
        final Random random = new Random();
        final ArrayType arrayType = ArrayType.of(type);
        final DataFrame<R,C> frame = DataFrame.of(rowKeys, colKeys, type);
        switch (arrayType) {
            case BOOLEAN:           return frame.applyBooleans(v -> Math.random() > 0.5d);
            case INTEGER:           return frame.applyInts(v -> Math.random() > 0.5 ? 0 : (int) (Math.random() * 1000));
            case LONG:              return frame.applyLongs(v -> Math.random() > 0.5 ? 0 : (long) (Math.random() * 1000));
            case DOUBLE:            return frame.applyDoubles(v -> Math.random() > 0.5 ? Double.NaN : Math.random() * 100d);
            case STRING:            return frame.applyValues(v -> Double.toString(Math.random() * 100d));
            case OBJECT:            return frame.applyValues(v -> Double.toString(Math.random() * 100d));
            case DATE:              return frame.applyValues(v -> new Date(random.nextInt()));
            case LOCAL_DATE:        return frame.applyValues(v -> LocalDate.now().plusDays((long)random.nextInt(frame.rowCount())));
            case LOCAL_TIME:        return frame.applyValues(v -> LocalTime.now().plusNanos((long)random.nextInt(frame.rowCount())));
            case LOCAL_DATETIME:    return frame.applyValues(v -> LocalDateTime.now().plusMinutes((long)random.nextInt(frame.rowCount())));
            case ZONED_DATETIME:    return frame.applyValues(v -> ZonedDateTime.now().plusMinutes((long)random.nextInt(frame.rowCount())));
            default:                throw new IllegalArgumentException("Unsupported type specified: " + type);
        }
    }

    /**
     * Returns a DataFrame initialized with random data of various types
     * @param rowType      the rowType for row keys
     * @param rowCount  the row count
     * @param <T>       the row class
     * @return          the newly created DataFrame
     */
    @SuppressWarnings("unchecked")
    public static <T> DataFrame<T,String> createMixedRandomFrame(Class<T> rowType, int rowCount) {
        final Random random = new Random();
        final Index<T> rowKeys = createRange(rowType, rowCount).toIndex(rowType);
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("BooleanColumn", Boolean.class).applyBooleans(v -> random.nextBoolean());
            columns.add("IntegerColumn", Integer.class).applyInts(v -> random.nextInt());
            columns.add("LongColumn", Long.class).applyLongs(v -> random.nextLong());
            columns.add("DoubleColumn", Double.class).applyDoubles(v -> random.nextDouble());
            columns.add("LocalDateColumn", LocalDate.class).applyValues(v -> LocalDate.now().plusDays(v.rowOrdinal()));
            columns.add("LocalTimeColumn", LocalTime.class).applyValues(v -> LocalDateTime.now().minusSeconds(v.rowOrdinal()).toLocalTime());
            columns.add("LocalDateTimeColumn", LocalDateTime.class).applyValues(v -> LocalDateTime.now().plusDays(v.rowOrdinal()));
            columns.add("ZonedDateTimeColumn", ZonedDateTime.class).applyValues(v -> ZonedDateTime.now().plusDays(v.rowOrdinal()));
            columns.add("EnumColumn", Month.class).applyValues(v -> LocalDateTime.now().minusDays(v.rowOrdinal()).getMonth());
        });
    }


    @Test(enabled = false)
    public void test() {
        createMixedRandomFrame(Integer.class, 1000).write().csv(new File("frame-with-int-index.csv")).apply();
        createMixedRandomFrame(String.class, 1000).write().csv(new File("frame-with-string-index.csv")).apply();
        createMixedRandomFrame(LocalDate.class, 1000).write().csv(new File("frame-with-local-date-index.csv")).apply();
        createMixedRandomFrame(LocalTime.class, 1000).write().csv(new File("frame-with-local-time-index.csv")).apply();
        createMixedRandomFrame(LocalDateTime.class, 1000).write().csv(new File("frame-with-local-date-time-index.csv")).apply();
        createMixedRandomFrame(ZonedDateTime.class, 1000).write().csv(new File("frame-with-zoned-date-time-index.csv")).apply();
    }


    @SuppressWarnings("unchecked")
    public static <T> Range<T> createRange(Class<T> type, int count) {
        if (type == Integer.class) {
            return Range.of(0, count).map(v -> (T)v);
        } else if (type == Long.class) {
            return Range.of(0, count).map(v -> (T)Long.valueOf(v.longValue()));
        } else if (type == String.class) {
            return Range.of(0, count).map(v -> (T)("R" + v));
        } else if (type == LocalDate.class) {
            final LocalDate start = LocalDate.now().minusDays(count);
            return Range.of(0, count).map(start::plusDays).map(v -> (T)v);
        } else if (type == LocalTime.class) {
            final LocalTime start = LocalTime.now().minusSeconds(count);
            return Range.of(0, count).map(start::plusSeconds).map(v -> (T)v);
        } else if (type == ZonedDateTime.class) {
            final ZonedDateTime start = ZonedDateTime.now().minusDays(count);
            return Range.of(0, count).map(start::plusDays).map(v -> (T) v);
        } else if (type == LocalDateTime.class) {
            final LocalDateTime start = LocalDateTime.now().minusDays(count);
            return Range.of(0, count).map(start::plusDays).map(v -> (T) v);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
