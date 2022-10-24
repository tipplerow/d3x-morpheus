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
package com.d3x.morpheus.csv;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.d3x.morpheus.TestSuite;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.frame.DataFrameCursor;
import com.d3x.morpheus.reference.TestDataFrames;
import com.d3x.morpheus.util.Predicates;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.parser.Parser;
import com.d3x.morpheus.util.text.printer.Printer;

/**
 * A unit test of the DataFrame CSV reader
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class CsvTests {

    private File tmpDir = TestSuite.getOutputDir("csv-tests");
    private String[] quoteFields = {"Date", "Open", "High", "Low", "Close", "Volume", "Adj Close"};


    @DataProvider(name="parallel")
    public Object[][] parallel() {
        return new Object[][] {
            { false },
            { true }
        };
    }


    @DataProvider(name="types")
    public Object[][] types() {
        return new Object[][] {
            {String.class},
            {Integer.class},
            {Long.class},
            {LocalDate.class},
            {LocalTime.class},
            {LocalDateTime.class},
            {ZonedDateTime.class}
        };
    }


    @Test(dataProvider="types")
    public <T> void testLocalDateAxis(Class<T> rowType) {
        var tmpDir = System.getProperty("java.io.tmpdir");
        var file = new File(tmpDir, "DataFrame-" + rowType.getSimpleName() + ".csv");
        var frame = TestDataFrames.createMixedRandomFrame(rowType, 100);
        System.out.println("Writing to " + file.getAbsolutePath());
        frame.write().csv(file).apply(options -> {
            options.withFormats(formats -> {
                formats.setPrinter("LocalDateTimeColumn", Printer.ofLocalDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                formats.setPrinter("ZonedDateTimeColumn", Printer.ofZonedDateTime(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            });
        });
        frame.out().print();
        readAndValidate(frame, rowType, file);
    }


    /**
     * Loads the DataFrame from the file and compares it to original
     * @param original  the original frame
     * @param file      the file to read from
     */
    private <T> void readAndValidate(DataFrame<T,String> original, Class<T> rowType, File file) {
        var formats = new Formats();
        var parser = formats.getParserOrFail(rowType);
        var result = DataFrame.read(file).csv(rowType, options -> {
            options.setFormats(formats);
            options.setRowKeyColumnName("DataFrame");
            options.getFormats().setParser("DataFrame", parser);
            options.getFormats().setParser("DoubleColumn", Double.class);
            options.getFormats().setParser("EnumColumn", Month.class);
            options.getFormats().setParser("LongColumn", Long.class);
            options.getFormats().setParser("IntegerColumn", Integer.class);
            options.getFormats().setParser("LocalDateTimeColumn", Parser.ofLocalDateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            options.getFormats().setParser("ZonedDateTimeColumn", Parser.ofZonedDateTime(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        });
        original.out().print();
        result.out().print();
        DataFrameAsserts.assertEqualsByIndex(original, result);
    }


    @Test()
    public void testBasicRead() {
        var frame = DataFrame.read("/csv/aapl.csv").csv(Integer.class, options -> {
            options.getFormats().setParser("Volume", Long.class);
        });
        assertTrue(frame.rows().count() > 0, "There is at least one row");
        assertEquals(frame.cols().count(),  7, "There are 7 columns");
        assertTrue(frame.cols().keys().allMatch(Predicates.in(quoteFields)), "Contains all expected columns");
        assertEquals(frame.rowCount(), 8503);
        assertEquals(frame.cols().type("Date"), LocalDate.class);
        assertEquals(frame.cols().type("Open"), Double.class);
        assertEquals(frame.cols().type("High"), Double.class);
        assertEquals(frame.cols().type("Low"), Double.class);
        assertEquals(frame.cols().type("Close"), Double.class);
        assertEquals(frame.cols().type("Volume"), Long.class);
        assertEquals(frame.cols().type("Adj Close"), Double.class);
        assertEquals(frame.rows().firstKey(), Optional.of(0));
        assertEquals(frame.rows().lastKey(), Optional.of(8502));

        final DataFrameCursor<Integer,String> cursor = frame.cursor();
        cursor.rowAt(0);
        assertEquals(cursor.col("Date").getValue(), LocalDate.of(1980, 12, 12));
        assertEquals(cursor.col("Open").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("High").getDouble(), 28.87472, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 117258400L);
        assertEquals(cursor.col("Adj Close").getDouble(), 0.44203, 0.00001);

        cursor.rowAt(7690);
        assertEquals(cursor.col("Date").getValue(), LocalDate.of(2011, 6, 8));
        assertEquals(cursor.col("Open").getDouble(), 331.77997, 0.00001);
        assertEquals(cursor.col("High").getDouble(), 334.79999, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 330.64996, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 332.24002, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 83430900L);
        assertEquals(cursor.col("Adj Close").getDouble(), 44.76965, 0.00001);

        cursor.rowAt(8502);
        assertEquals(cursor.col("Date").getValue(), LocalDate.of(2014, 8, 29));
        assertEquals(cursor.col("Open").getDouble(), 102.86, 0.00001);
        assertEquals(cursor.col("High").getDouble(), 102.9, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 102.2, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 102.5, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 44595000L);
        assertEquals(cursor.col("Adj Close").getDouble(), 101.65627, 0.00001);

        for (int i=0; i<frame.rows().count(); ++i) {
            var rowKey = frame.rows().key(i);
            assertEquals(rowKey.intValue(), i, "Row key matches at index " + i);
        }
    }

    @Test()
    public void testRowKeyParser() {
        var frame = DataFrame.read("/csv/aapl.csv").csv(LocalDate.class, options -> {
            options.setRowKeyColumnName("Date");
            options.getFormats().copyParser(Long.class, "Volume");
        });
        assertEquals(frame.rowCount(), 8503);
        assertTrue(frame.cols().count() == 6);
        assertTrue(frame.cols().keys().allMatch(Predicates.in("Open", "High", "Low", "Close", "Volume", "Adj Close")));

        assertEquals(frame.cols().type("Open"), Double.class);
        assertEquals(frame.cols().type("High"), Double.class);
        assertEquals(frame.cols().type("Low"), Double.class);
        assertEquals(frame.cols().type("Close"), Double.class);
        assertEquals(frame.cols().type("Volume"), Long.class);
        assertEquals(frame.cols().type("Adj Close"), Double.class);

        assertEquals(frame.rows().key(0), LocalDate.of(1980, 12, 12));

        final DataFrameCursor<LocalDate,String> cursor = frame.cursor();
        cursor.row(LocalDate.of(1980, 12, 12));
        assertEquals(cursor.col("Open").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("High").getDouble(), 28.87472, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 117258400L);
        assertEquals(cursor.col("Adj Close").getDouble(), 0.44203, 0.00001);

        cursor.row(LocalDate.of(2011, 6, 8));
        assertEquals(frame.rows().key(7690), LocalDate.of(2011, 6, 8));
        assertEquals(cursor.col("Open").getDouble(), 331.77997, 0.00001);
        assertEquals(cursor.col("High").getDouble(), 334.79999, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 330.64996, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 332.24002, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 83430900L);
        assertEquals(cursor.col("Adj Close").getDouble(), 44.76965, 0.00001);

        cursor.row(LocalDate.of(2014, 8, 29));
        assertEquals(frame.rows().key(8502), LocalDate.of(2014, 8, 29));
        assertEquals(cursor.col("Open").getDouble(), 102.86, 0.00001);
        assertEquals(cursor.col("High").getDouble(), 102.9, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 102.2, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 102.5, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 44595000L);
        assertEquals(cursor.col("Adj Close").getDouble(), 101.65627, 0.00001);

        for (int i=0; i<frame.rows().count(); ++i) {
            final LocalDate rowKey = frame.rows().key(i);
            assertEquals(rowKey.getClass(), LocalDate.class, "Row key matches LocalDate type ");
        }
    }


    @Test()
    public void testRowPredicate() {
        var frame = DataFrame.read("/csv/aapl.csv").csv(Integer.class, options -> {
            options.setRowPredicate(values -> values[0].startsWith("2012"));
            options.getFormats().copyParser(Long.class, "Volume");
        });
        assertEquals(frame.rows().count(), 250, "There is at least one row");
        assertEquals(frame.cols().count(), 7, "There are 7 columns");
        assertTrue(frame.cols().keys().allMatch(Predicates.in(quoteFields)), "Contains all expected columns");

        assertEquals(frame.cols().type("Date"), LocalDate.class);
        assertEquals(frame.cols().type("Open"), Double.class);
        assertEquals(frame.cols().type("High"), Double.class);
        assertEquals(frame.cols().type("Low"), Double.class);
        assertEquals(frame.cols().type("Close"), Double.class);
        assertEquals(frame.cols().type("Volume"), Long.class);
        assertEquals(frame.cols().type("Adj Close"), Double.class);

        final DataFrameCursor<Integer,String> cursor = frame.cursor();
        cursor.rowAt(0);
        assertEquals(cursor.col("Date").getValue(), LocalDate.of(2012, 1, 3));
        assertEquals(cursor.col("Open").getDouble(), 409.39996, 0.00001);
        assertEquals(cursor.col("High").getDouble(), 412.5, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 409, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 411.22998, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 75555200L);
        assertEquals(cursor.col("Adj Close").getDouble(), 55.41362, 0.00001);

        cursor.rowAt(249);
        assertEquals(cursor.col("Date").getValue(), LocalDate.of(2012, 12, 31));
        assertEquals(cursor.col("Open").getDouble(), 510.53003, 0.00001);
        assertEquals(cursor.col("High").getDouble(), 535.39996, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 509, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 532.17004, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 164873100L);
        assertEquals(cursor.col("Adj Close").getDouble(), 72.34723, 0.00001);

        frame.rows().forEach(row -> assertTrue(row.<LocalDate>getValue("Date").getYear() == 2012));
    }


    @Test()
    public void testColumnPredicate() {
        final String[] columns = {"Date", "Close", "Volume"};
        var frame = DataFrame.read("/csv/aapl.csv").csv(Integer.class, options -> {
            options.setIncludeColumns(columns);
            options.getFormats().setParser("Volume", Long.class);
        });
        var expected = DataFrame.read("/csv/aapl.csv").csv(Integer.class, options -> {
            options.getFormats().setParser("Volume", Long.class);
        });
        assertEquals(frame.rowCount(), 8503);
        assertEquals(frame.cols().count(), 3);
        assertTrue(frame.cols().keys().allMatch(Predicates.in(columns)));
        assertEquals(frame.cols().type("Date"), LocalDate.class);
        assertEquals(frame.cols().type("Close"), Double.class);
        assertEquals(frame.cols().type("Volume"), Long.class);
        final DataFrameCursor<Integer,String> cursor = expected.cursor();
        frame.rows().forEach(row -> {
            for (String column : columns) {
                final Object actual = row.getValue(column);
                final Object expect = cursor.row(row.key()).col(column).getValue();
                assertEquals(actual, expect, "The values match for " + row.key() + ", " + column);
            }
        });
    }


    @Test()
    public void testRowAndColumnPredicate() {
        final String[] columns = {"Date", "Close", "Volume"};
        var frame = DataFrame.read("/csv/aapl.csv").csv(Integer.class, options -> {
            options.setRowPredicate(values -> values[0].startsWith("2012"));
            options.setColNamePredicate(Predicates.in(columns));
            options.getFormats().copyParser(Long.class, "Volume");
        });
        var expected = DataFrame.read("/csv/aapl.csv").csv(LocalDate.class, options -> {
            options.setRowKeyColumnName("Date");
            options.getFormats().copyParser(Long.class, "Volume");
        });
        assertEquals(frame.rows().count(), 250);
        assertEquals(frame.cols().count(), 3);
        assertTrue(frame.cols().keys().allMatch(Predicates.in(columns)));
        assertEquals(frame.cols().type("Date"), LocalDate.class);
        assertEquals(frame.cols().type("Close"), Double.class);
        assertEquals(frame.cols().type("Volume"), Long.class);
        final DataFrameCursor<LocalDate,String> cursor = expected.cursor();
        frame.rows().forEach(row -> assertTrue(row.<LocalDate>getValue("Date").getYear() == 2012));
        frame.rows().forEach(row -> {
            final LocalDate date = row.getValue("Date");
            assertTrue(date.getYear() == 2012);
            for (String column : Arrays.asList("Close", "Volume")) {
                final Object actual = row.getValue(column);
                final Object expect = cursor.row(date).col(column).getValue();
                assertEquals(actual, expect, "The values match for " + row.key() + ", " + column);
            }
        });
    }


    @Test()
    public void testWriteFollowedByRead() {
        final File file = new File(tmpDir, "aapl.csv");
        var frame1 = DataFrame.read("/csv/aapl.csv").csv(LocalDate.class, options -> {
            options.setRowKeyColumnName("Date");
            options.getFormats().setParser("Volume", Long.class);
        });
        frame1.write().csv(file).apply();
        var frame2 = DataFrame.read(file).csv(LocalDate.class, options -> {
            options.setRowKeyColumnName("DataFrame");
            options.getFormats().setParser("Volume", Long.class);
        });
        DataFrameAsserts.assertEqualsByIndex(frame1, frame2);
    }


    @Test()
    public void testCustomParsers() {
        var frame = DataFrame.read("/csv/aapl.csv").csv(LocalDate.class, options -> {
            options.setRowKeyColumnName("Date");
            options.getFormats().copyParser(Double.class, "Volume");
            options.getFormats().copyParser(BigDecimal.class, "Close");
        });
        assertEquals(frame.rowCount(), 8503);
        assertEquals(frame.cols().count(), 6);
        assertTrue(frame.cols().keys().allMatch(Predicates.in("Open", "High", "Low", "Close", "Volume", "Adj Close")));
        assertEquals(frame.cols().type("Open"), Double.class);
        assertEquals(frame.cols().type("High"), Double.class);
        assertEquals(frame.cols().type("Low"), Double.class);
        assertEquals(frame.cols().type("Close"), BigDecimal.class);
        assertEquals(frame.cols().type("Volume"), Double.class);
        assertEquals(frame.cols().type("Adj Close"), Double.class);
    }


    @Test()
    public void testWindowsTasks() {
        final String[] columns = {"Image Name", "PID", "Session Name", "Session#", "Mem Usage", "Status", "User Name", "CPU Time", "Window Title"};
        var frame = DataFrame.read("/csv/tasks.csv").csv(Integer.class, options -> {
            options.setColNamePredicate(Predicates.in(columns));
            options.setRowKeyColumnName("PID");
        });
        assertEquals(frame.rowCount(), 45, "Frame row count is as expected");
        assertEquals(frame.colCount(), columns.length-1, "Frame column count as expected");
        Arrays.stream(columns).filter(c -> !c.equals("PID")).forEach(column -> assertTrue(frame.cols().contains(column)));
        assertTrue(frame.cols().keys().allMatch(Predicates.in(columns)), "Contains all expected columns");
    }

    @Test()
    public void testWindowsTasksColumnInclude() {
        final String[] columns1 = {"Image Name", "Session Name", "Session#", "Mem Usage", "Status", "User Name", "CPU Time", "Window Title", "PID"};
        final String[] columns2 = {"Image Name", "Mem Usage", "User Name", "CPU Time", "PID"};
        var frame1 = DataFrame.read("/csv/tasks.csv").csv(Integer.class, options -> {
            options.setRowKeyColumnName("PID");
            options.setParser("PID", Parser.ofInteger());
            options.setColNamePredicate(Predicates.in(columns1));
        });
        var frame2 = DataFrame.read("/csv/tasks.csv").csv(Integer.class, options -> {
            options.setRowKeyColumnName("PID");
            options.setParser("PID", Parser.ofInteger());
            options.setColNamePredicate(Predicates.in(columns2));
        });
        frame1.out().print();
        frame2.out().print();
        assertEquals(frame1.rowCount(), 45, "Frame1 row count is as expected");
        assertEquals(frame2.rowCount(), 45, "Frame2 row count is as expected");
        assertTrue(frame1.colCount() > frame2.colCount(), "First frame has more columns");
        assertEquals(frame1.colCount()+1, columns1.length, "Frame1 column count as expected");
        assertEquals(frame2.colCount()+1, columns2.length, "Frame2 column count as expected");
        Arrays.stream(columns1).filter(v -> !v.equals("PID")).forEach(column -> assertTrue(frame1.cols().contains(column)));
        Arrays.stream(columns2).filter(v -> !v.equals("PID")).forEach(column -> assertTrue(frame2.cols().contains(column)));
    }

    @Test()
    public void testCustomCharset() {
        var frame = DataFrame.read("/csv/process.csv").csv(Integer.class, options -> {
            options.setCharset(StandardCharsets.UTF_16);
            options.setRowKeyColumnName("ProcessId");
            options.getFormats().copyParser(Long.class, "KernelModeTime");
        });
        assertEquals(frame.rowCount(), 43, "Frame row count is as expected");
        assertEquals(frame.colCount(), 45, "Frame column count is as expected");
    }


    @Test()
    public void testMultipleColumnPredicates() {
        var frame = DataFrame.read("/csv/aapl.csv").csv(LocalDate.class, options -> {
            options.setExcludeColumns("Open");
            options.setRowKeyColumnName("Date");
            options.getFormats().copyParser(Long.class, "Volume");
        });

        assertEquals(frame.rowCount(), 8503);
        assertTrue(frame.cols().count() == 5);
        assertTrue(frame.cols().keys().allMatch(Predicates.in("High", "Low", "Close", "Volume", "Adj Close")));

        assertEquals(frame.cols().type("High"), Double.class);
        assertEquals(frame.cols().type("Low"), Double.class);
        assertEquals(frame.cols().type("Close"), Double.class);
        assertEquals(frame.cols().type("Volume"), Long.class);
        assertEquals(frame.cols().type("Adj Close"), Double.class);

        final DataFrameCursor<LocalDate,String> cursor = frame.cursor();
        cursor.row(LocalDate.of(1980, 12, 12));
        assertEquals(frame.rows().key(0), LocalDate.of(1980, 12, 12));
        assertEquals(cursor.col("High").getDouble(), 28.87472, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 117258400L);
        assertEquals(cursor.col("Adj Close").getDouble(), 0.44203, 0.00001);

        cursor.row(LocalDate.of(2011, 6, 8));
        assertEquals(frame.rows().key(7690), LocalDate.of(2011, 6, 8));
        assertEquals(cursor.col("High").getDouble(), 334.79999, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 330.64996, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 332.24002, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 83430900L);
        assertEquals(cursor.col("Adj Close").getDouble(), 44.76965, 0.00001);

        cursor.row(LocalDate.of(2014, 8, 29));
        assertEquals(frame.rows().key(8502), LocalDate.of(2014, 8, 29));
        assertEquals(cursor.col("High").getDouble(), 102.9, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 102.2, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 102.5, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 44595000L);
        assertEquals(cursor.col("Adj Close").getDouble(), 101.65627, 0.00001);
    }


    @Test()
    public void testIncludeColumnIndexes() {
        var frame = DataFrame.read("/csv/aapl.csv").csv(LocalDate.class, options -> {
            options.setRowKeyColumnName("Date");
            options.setIncludeColumnIndexes(0, 2, 3, 4, 5, 6);
            options.getFormats().copyParser(Long.class, "Volume");
        });

        assertEquals(frame.rowCount(), 8503);
        assertTrue(frame.cols().count() == 5);
        assertTrue(frame.cols().keys().allMatch(Predicates.in("High", "Low", "Close", "Volume", "Adj Close")));

        assertEquals(frame.cols().type("High"), Double.class);
        assertEquals(frame.cols().type("Low"), Double.class);
        assertEquals(frame.cols().type("Close"), Double.class);
        assertEquals(frame.cols().type("Volume"), Long.class);
        assertEquals(frame.cols().type("Adj Close"), Double.class);

        final DataFrameCursor<LocalDate,String> cursor = frame.cursor();
        cursor.row(LocalDate.of(1980, 12, 12));
        assertEquals(frame.rows().key(0), LocalDate.of(1980, 12, 12));
        assertEquals(cursor.col("High").getDouble(), 28.87472, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 28.74984, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 117258400L);
        assertEquals(cursor.col("Adj Close").getDouble(), 0.44203, 0.00001);

        cursor.row(LocalDate.of(2011, 6, 8));
        assertEquals(frame.rows().key(7690), LocalDate.of(2011, 6, 8));
        assertEquals(cursor.col("High").getDouble(), 334.79999, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 330.64996, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 332.24002, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 83430900L);
        assertEquals(cursor.col("Adj Close").getDouble(), 44.76965, 0.00001);

        cursor.row(LocalDate.of(2014, 8, 29));
        assertEquals(frame.rows().key(8502), LocalDate.of(2014, 8, 29));
        assertEquals(cursor.col("High").getDouble(), 102.9, 0.00001);
        assertEquals(cursor.col("Low").getDouble(), 102.2, 0.00001);
        assertEquals(cursor.col("Close").getDouble(), 102.5, 0.00001);
        assertEquals(cursor.col("Volume").getLong(), 44595000L);
        assertEquals(cursor.col("Adj Close").getDouble(), 101.65627, 0.00001);
    }



    private enum QuoteField {
        OPEN, HIGH, LOW, CLOSE, VOLUME, ADJ_CLOSE;

        public String toString() {
            switch (this) {
                case OPEN:      return "Open";
                case HIGH:      return "High";
                case LOW:       return "Low";
                case CLOSE:     return "Close";
                case VOLUME:    return "Volume";
                case ADJ_CLOSE: return "Adj Close";
                default:    throw new IllegalArgumentException("Unexpected type: " + this.name());
            }
        }

        public static QuoteField parse(String s) {
            if (s.equalsIgnoreCase("Open"))             return OPEN;
            else if (s.equalsIgnoreCase("High"))        return HIGH;
            else if (s.equalsIgnoreCase("Low"))         return LOW;
            else if (s.equalsIgnoreCase("Close"))       return CLOSE;
            else if (s.equalsIgnoreCase("Volume"))      return VOLUME;
            else if (s.equalsIgnoreCase("Adj Close"))   return ADJ_CLOSE;
            else {
                throw new IllegalArgumentException("Unsupported field name: " + s);
            }
        }
    }
}


