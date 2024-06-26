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
package com.d3x.morpheus.docs;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameRow;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.Asserts;
import com.d3x.morpheus.util.Tuple;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AccessDocs {


    @DataProvider(name="frame")
    public Object[][] getFrame() {
        //Create 5x5 frame with columns of different types.
        var years = Range.of(2000 ,2005).map(Year::of);
        var frame = DataFrame.of(years, String.class, columns -> {
            columns.add("Column-0", Array.of(true, false, false, true, true));
            columns.add("Column-1", Array.of(1, 2, 3, 4, 5));
            columns.add("Column-2", Array.of(10L, 11L, 12L, 13L, 14L));
            columns.add("Column-3", Array.of(20d, 21d, 22d, 23d, 24d));
            columns.add("Column-4", Array.of("Hello", LocalDate.of(1998, 1, 1), Month.JANUARY, 56.45d, true));
        });
        return new Object[][] { { frame } };
    }


    @Test()
    public void testFrame() {
        //Create 5x5 frame with columns of different types.
        var years = Range.of(2000 ,2005).map(Year::of);
        var frame = DataFrame.of(years, String.class, columns -> {
            columns.add("Column-0", Array.of(true, false, false, true, true));
            columns.add("Column-1", Array.of(1, 2, 3, 4, 5));
            columns.add("Column-2", Array.of(10L, 11L, 12L, 13L, 14L));
            columns.add("Column-3", Array.of(20d, 21d, 22d, 23d, 24d));
            columns.add("Column-4", Array.of("Hello", LocalDate.of(1998, 1, 1), Month.JANUARY, 56.45d, true));
        });

        frame.out().print();
    }


    @Test(dataProvider="frame")
    public void testAccessMethods(DataFrame<Year,String> frame) {

        frame.out().print();

        //Random access to primitive boolean values via ordinal or keys or any combination thereof
        var b1 = frame.getBooleanAt(0, 0);
        var b2 = frame.getBoolean(Year.of(2001), "Column-0");
        var b3 = frame.rows().getBoolean(Year.of(2003), 0);
        var b4 = frame.rows().getBooleanAt(0, "Column-0");
        var b5 = frame.cols().getBoolean("Column-0", 0);
        var b6 = frame.cols().getBooleanAt(0, Year.of(2003));

        //Random access to primitive int values via ordinal or keys or any combination thereof
        var i1 = frame.getIntAt(4, 1);
        var i2 = frame.getInt(Year.of(2001), "Column-1");
        var i3 = frame.rows().getInt(Year.of(2003), 1);
        var i4 = frame.rows().getIntAt(0, "Column-1");
        var i5 = frame.cols().getInt("Column-0", 0);
        var i6 = frame.cols().getIntAt(0, Year.of(2003));

        //Random access to primitive long values via ordinal or keys or any combination thereof
        var l1 = frame.getLongAt(4, 2);
        var l2 = frame.getLong(Year.of(2001), "Column-2");
        var l3 = frame.rows().getLong(Year.of(2003), 2);
        var l4 = frame.rows().getLongAt(0, "Column-2");
        var l5 = frame.cols().getLong("Column-0", 0);
        var l6 = frame.cols().getLongAt(0, Year.of(2003));

        //Random access to primitive double values via ordinal or keys or any combination thereof
        var d1 = frame.getDoubleAt(4, 3);
        var d2 = frame.getDouble(Year.of(2001), "Column-3");
        var d3 = frame.rows().getDouble(Year.of(2003), 3);
        var d4 = frame.rows().getDoubleAt(0, "Column-3");
        var d5 = frame.cols().getDouble("Column-0", 0);
        var d6 = frame.cols().getDoubleAt(0, Year.of(2003));

        //Random access to any values via ordinal or keys or any combination thereof
        var o1 = frame.<String>getValueAt(0, 4);
        var o2 = frame.<Double>getValue(Year.of(2003), "Column-4");
        var o3 = frame.rows().<LocalDate>getValue(Year.of(2001), 4);
        var o4 = frame.rows().<Month>getValueAt(2, "Column-4");
    }


    @Test()
    public void testAllAccess() {

        //Create DataFrame of random doubles
        var frame = DataFrame.ofDoubles(
            Array.of(0, 1, 2, 3, 4, 5, 6, 7),
            Array.of("A", "B", "C", "D"),
            value -> Math.random()
        );

        //Count number of values > 0.5d first sequentially, then in parallel
        long count1 = frame.values().filter(v -> v.getDouble() > 0.5d).count();
        long count2 = frame.values().parallel().filter(v -> v.getDouble() > 0.5d).count();
        Assert.assertEquals(count1, count2);

    }

    @Test()
    public void testPercentageConversion() {
        var frame = DemoData.loadPopulationDataset();
        //Sequential: Convert male & female population counts into weights
        frame.rows().parallel().forEach(row -> row.forEach(value -> {
            if (value.colKey().matches("M\\s+\\d+")) {
                double totalMales = value.row().getDouble("All Males");
                double count = value.getDouble();
                value.setDouble(count / totalMales);
            } else if (value.colKey().matches("F\\s+\\d+")) {
                double totalFemales = value.row().getDouble("All Females");
                double count = value.getDouble();
                value.setDouble(count / totalFemales);
            }
        }));

        //Print frame to std out with custom formatting
        frame.out().print(formats -> {
            formats.setDecimalFormat("All Persons", "0;-0", 1);
            formats.setDecimalFormat("All Males", "0;-0", 1);
            formats.setDecimalFormat("All Females", "0;-0", 1);
            formats.setDecimalFormat(Double.class, "0.00'%';-0.00'%'", 100);
        });
    }



    @Test()
    public void testRandomAccess1() {
        //Load the ONS dataset
        var frame = DemoData.loadPopulationDataset();

        //Random access to a row by ordinal or key
        var row1 = frame.rowAt(4);
        var row2 = frame.row(Tuple.of(2003, "City of London"));

        //Random access to a column by ordinal or key
        var column1 = frame.colAt(2);
        var column2 = frame.col("All Persons");

        //Access first and last rows
        var firstRow = frame.rows().first();
        var lastRow = frame.rows().last();

        //Access first and last columns
        var firstColumn = frame.cols().first();
        var lastColumn = frame.cols().last();

    }


    @Test()
    public void testRandomAccess2() {
        var frame = DemoData.loadPopulationDataset();

        // Find row with max value for All Persons column using column index
        frame.colAt(2).max().ifPresent(value -> {
            DataFrameRow<Tuple,String> row = frame.row(value.rowKey());
            System.out.printf("Max population for %s in %s\n", row.getValue("Borough"), row.getInt("Year"));
        });

        //Access 5th row by row ordinal, and find first value that matches a predicate
        frame.rowAt(4).first(v -> v.isDouble() && v.getDouble() < 20).ifPresent(v -> {
            System.out.println("First match > 1000 in " + v.colKey());
        });


        // Find row with max value for "All Persons: column using column key
        var expectedMax = frame.col("All Persons").stats().max();
        frame.col("All Persons").max().ifPresent(value -> {
            var row = frame.row(value.rowKey());
            var actualMax = row.getDouble("All Persons");
            Asserts.assertEquals(actualMax, expectedMax.doubleValue(), "The max values match");
        });

        //Access 5th row by row key, and find first value that matches a predicate
        frame.row(Tuple.of("2003", "E09000001")).first(v -> v.isDouble() && v.getDouble() < 20).ifPresent(v -> {
            System.out.println("First match > 1000 in " + v.colKey());
        });

    }



    @Test()
    public void testArrayAccess() {
        var booleanArray = Array.of(true, false, true, false);
        var intArray = Array.of(1, 2, 3, 4, 5);
        var longArray = Array.of(1L, 2L, 3L, 4L, 5L);
        var doubleArray = Array.of(1d, 2d, 3d, 4d, 5d);
        var objectArray = Array.of("Hello", LocalDate.of(1998, 1, 1), Month.JANUARY, 56.45d, true);

        System.out.println(booleanArray.getClass().getName());
        System.out.println(intArray.getClass().getName());
        System.out.println(longArray.getClass().getName());
        System.out.println(doubleArray.getClass().getName());
        System.out.println(objectArray.getClass().getName());
    }


    @Test()
    public void demean() {

        //Load population dataset
        var onsFrame = DemoData.loadPopulationDataset();

        //Iterate over male then female column set
        Array.of("M\\s+\\d++", "F\\s+\\d++").forEach(regex -> {
            onsFrame.cols().select(c -> c.key().matches(regex)).rows().parallel().forEach(row -> {
                final double mean = row.stats().mean();
                row.applyDoubles(v -> v.getDouble() - mean);
            });
        });

        //Print frame to standard out with formatting
        onsFrame.out().print(formats -> {
            formats.setDecimalFormat("All Persons", "0;-0", 1);
            formats.setDecimalFormat("All Males", "0;-0", 1);
            formats.setDecimalFormat("All Females", "0;-0", 1);
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });
    }

    @Test()
    public void weights() {
        //Load ONS population dataset
        var onsFrame = DemoData.loadPopulationDataset();

        //Define function to compute population weight as a percentage of 2007 value per borough
        ToDoubleFunction<DataFrameValue<Tuple,String>> compute = value -> {
            var borough = value.rowKey().item(1);
            var rowKey2014 = Tuple.of(2007, borough);
            var boroughCountIn2014 = onsFrame.getDouble(rowKey2014, "All Persons");
            return value.getDouble() / boroughCountIn2014;
        };

        //Apply function to various columns in order
        onsFrame.cols().select(c -> c.key().matches("[MF]\\s+\\d+")).applyDoubles(compute);
        onsFrame.col("All Males").applyDoubles(compute);
        onsFrame.col("All Females").applyDoubles(compute);
        onsFrame.out().print(formats -> {
            formats.setDecimalFormat("All Persons", "0.0;-0.0", 1);
            formats.setDecimalFormat(Double.class, "0.00'%';-0.00'%'", 100);
        });
    }

}
