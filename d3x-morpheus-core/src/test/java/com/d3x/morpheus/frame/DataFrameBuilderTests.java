/*
 * Copyright (C) 2014-2022 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.frame;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for the DataFrameBuilder class
 *
 * @author Xavier Witdouck
 */
public class DataFrameBuilderTests {


    @Test()
    public void basic() {
        var date1 = LocalDate.now();
        var date2 = LocalDate.now().plusDays(1);
        var defaultValues = List.of(0d, Double.NaN);
        defaultValues.forEach(defaultValue -> {
            var frame = DataFrame.of(LocalDate.class, String.class, df -> {
                df.setDefaultValue(v -> defaultValue);
                df.plusDouble(date1, "X", 5d);
                df.plusDouble(date1, "X", 12d);
                df.plusDouble(date2, "Y", 5d);
                df.putDouble(date2, "Y", 12d);
            });
            frame.out().print();
            Assert.assertEquals(frame.rowCount(), 2);
            Assert.assertEquals(frame.colCount(), 2);
            Assert.assertEquals(frame.getDouble(date1, "X"), 17d);
            Assert.assertEquals(frame.getDouble(date1, "Y"), defaultValue);
            Assert.assertEquals(frame.getDouble(date2, "X"), defaultValue);
            Assert.assertEquals(frame.getDouble(date2, "Y"), 12d);
        });
    }


    @Test()
    public void toBuilder() {
        var date1 = LocalDate.now();
        var date2 = LocalDate.now().plusDays(1);
        var date3 = LocalDate.now().plusDays(2);
        var frame = DataFrame.of(LocalDate.class, String.class, df -> {
            df.plusDouble(date1, "X", 5d);
            df.plusDouble(date1, "X", 12d);
            df.plusDouble(date2, "Y", 5d);
            df.putDouble(date2, "Y", 12d);
        }).toBuilder()
            .putDouble(date1, "Z", 230d)
            .plusDouble(date2, "Y", 10d)
            .plusDouble(date2, "Z", 100d)
            .putDouble(date3, "X", 1.1d)
            .putDouble(date3, "Y", 2.2d)
            .putDouble(date3, "Z", 3.3d)
            .build();
        frame.out().print();
        Assert.assertEquals(frame.rowCount(), 3);
        Assert.assertEquals(frame.colCount(), 3);
        Assert.assertEquals(frame.getDouble(date1, "X"), 17d);
        Assert.assertEquals(frame.getDouble(date1, "Y"), Double.NaN);
        Assert.assertEquals(frame.getDouble(date1, "Z"), 230d);
        Assert.assertEquals(frame.getDouble(date2, "X"), Double.NaN);
        Assert.assertEquals(frame.getDouble(date2, "Y"), 22d);
        Assert.assertEquals(frame.getDouble(date2, "Z"), 100d);
        Assert.assertEquals(frame.getDouble(date3, "X"), 1.1d);
        Assert.assertEquals(frame.getDouble(date3, "Y"), 2.2d);
        Assert.assertEquals(frame.getDouble(date3, "Z"), 3.3d);
    }


    @Test()
    public void transpose() {
        var date1 = LocalDate.now();
        var date2 = LocalDate.now().plusDays(1);
        var date3 = LocalDate.now().plusDays(2);
        var frame = DataFrame.of(LocalDate.class, String.class, df -> {
            df.plusDouble(date1, "X", 5d);
            df.plusDouble(date1, "X", 12d);
            df.plusDouble(date2, "Y", 5d);
            df.putDouble(date2, "Y", 12d);
        }).transpose()
            .toBuilder()
            .putDouble("Z", date1, 230d)
            .plusDouble( "Y", date2, 10d)
            .plusDouble("Z", date2, 100d)
            .putDouble("X", date3, 1.1d)
            .putDouble("Y", date3, 2.2d)
            .putDouble("Z", date3, 3.3d)
            .build();

        frame.out().print();
        Assert.assertEquals(frame.rowCount(), 3);
        Assert.assertEquals(frame.colCount(), 3);
        Assert.assertEquals(frame.getDouble("X", date1), 17d);
        Assert.assertEquals(frame.getDouble("Y", date1), Double.NaN);
        Assert.assertEquals(frame.getDouble("Z", date1), 230d);
        Assert.assertEquals(frame.getDouble("X", date2), Double.NaN);
        Assert.assertEquals(frame.getDouble("Y", date2), 22d);
        Assert.assertEquals(frame.getDouble("Z", date2), 100d);
        Assert.assertEquals(frame.getDouble("X", date3), 1.1d);
        Assert.assertEquals(frame.getDouble("Y", date3), 2.2d);
        Assert.assertEquals(frame.getDouble("Z", date3), 3.3d);
    }


    @Test()
    public void addRows() {
        var date1 = LocalDate.now();
        var date2 = LocalDate.now().plusDays(1);
        var date3 = LocalDate.now().plusDays(2);
        var defaultValues = List.of(0d, Double.NaN);
        defaultValues.forEach(defaultValue -> {
            var frame = DataFrame.of(LocalDate.class, String.class, df -> {
                df.setDefaultValue(v -> defaultValue);
                df.plusDouble(date1, "X", 5.5d);
                df.addRows(List.of(date2, date3));
            });
            frame.out().print();
            Assert.assertEquals(frame.rowCount(), 3);
            Assert.assertEquals(frame.colCount(), 1);
            Assert.assertEquals(frame.getDouble(date1, "X"), 5.5d);
            Assert.assertEquals(frame.getDouble(date2, "X"), defaultValue);
            Assert.assertEquals(frame.getDouble(date3, "X"), defaultValue);
        });
    }


    @Test()
    public void addColumns() {
        var date1 = LocalDate.now();
        var defaultValues = List.of(0d, Double.NaN);
        defaultValues.forEach(defaultValue -> {
            var frame = DataFrame.of(LocalDate.class, String.class, df -> {
                df.setDefaultValue(v -> defaultValue);
                df.plusDouble(date1, "X", 5.5d);
                df.addColumns(Set.of("Y", "Z"), Double.class);
            });
            frame.out().print();
            Assert.assertEquals(frame.rowCount(), 1);
            Assert.assertEquals(frame.colCount(), 3);
            Assert.assertEquals(frame.getDouble(date1, "X"), 5.5d);
            Assert.assertEquals(frame.getDouble(date1, "Y"), defaultValue);
            Assert.assertEquals(frame.getDouble(date1, "Z"), defaultValue);
        });
    }


    @Test
    public void mixed() {
        var date1 = LocalDate.now();
        var date2 = LocalDate.now().plusDays(1);
        var frame = DataFrame.of(LocalDate.class, String.class, df -> {
            df.putBoolean(date1, "A", true);
            df.putInt(date1, "B", 12);
            df.putLong(date1, "C", 100L);
            df.putDouble(date1, "D", 1034d);
            df.putValue(date1, "E", "Hello!");
            df.putValue(date1, "F", LocalDate.now());
            df.putValue(date1, "G", Duration.ofSeconds(12));
            df.putValue(date2, "G", Period.ofDays(1));
        });
        frame.out().print();
        Assert.assertEquals(frame.rowCount(), 2);
        Assert.assertEquals(frame.colCount(), 7);
        Assert.assertEquals(frame.col("A").dataClass(), Boolean.class);
        Assert.assertEquals(frame.col("B").dataClass(), Integer.class);
        Assert.assertEquals(frame.col("C").dataClass(), Long.class);
        Assert.assertEquals(frame.col("D").dataClass(), Double.class);
        Assert.assertEquals(frame.col("E").dataClass(), String.class);
        Assert.assertEquals(frame.col("F").dataClass(), LocalDate.class);
        Assert.assertEquals(frame.col("G").dataClass(), Object.class);
        frame.values().forEach(v -> {
            if (v.rowKey().equals(date1)) {
                switch (v.colKey()) {
                    case "A":   Assert.assertTrue(v.getBoolean());                          break;
                    case "B":   Assert.assertEquals(v.getInt(), 12);               break;
                    case "C":   Assert.assertEquals(v.getLong(), 100L);            break;
                    case "D":   Assert.assertEquals(v.getDouble(), 1034d);         break;
                    case "E":   Assert.assertEquals(v.getValue(), "Hello!");       break;
                    case "F":   Assert.assertEquals(v.getValue(), LocalDate.now());         break;
                    case "G":   Assert.assertEquals(v.getValue(), Duration.ofSeconds(12));  break;
                    default:    throw new IllegalArgumentException("Unexpected column: " + v.colKey());
                }
            } else {
                switch (v.colKey()) {
                    case "A":   Assert.assertFalse(v.getBoolean());                     break;
                    case "B":   Assert.assertEquals(v.getInt(), 0);            break;
                    case "C":   Assert.assertEquals(v.getLong(), 0L);          break;
                    case "D":   Assert.assertTrue(v.isNull());                          break;
                    case "E":   Assert.assertTrue(v.isNull());                          break;
                    case "F":   Assert.assertTrue(v.isNull());                          break;
                    case "G":   Assert.assertEquals(v.getValue(), Period.ofDays(1));    break;
                    default:    throw new IllegalArgumentException("Unexpected column: " + v.colKey());
                }
            }
        });
    }
}
