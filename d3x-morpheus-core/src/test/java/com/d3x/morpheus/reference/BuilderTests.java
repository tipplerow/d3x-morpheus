/*
 * Copyright (C) 2018-2019 D3X Systems - All Rights Reserved
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.d3x.morpheus.util.StopWatch;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.util.IO;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for the DataFrameBuilder class
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class BuilderTests {


    @DataProvider(name="denseOrSparse")
    public Object[][] denseOrSparse() {
        return new Object[][] {
            { false }, { true }
        };
    }


    @Test()
    public void booleans() {
        var frame = DataFrame.builder(String.class, String.class)
            .putBoolean("R0", "C0", true)
            .putBoolean("R1", "C1", true)
            .putBoolean("R2", "C2", true)
            .putBoolean("R3", "C3", true)
            .putBoolean("R4", "C4", true)
            .build();

        frame.out().print();

        Assert.assertEquals(frame.rowCount(), 5);
        Assert.assertEquals(frame.colCount(), 5);
        Assert.assertEquals(frame.rows().keyClass(), String.class);
        Assert.assertEquals(frame.cols().keyClass(), String.class);
        frame.cols().forEach(v -> Assert.assertEquals(v.dataClass(), Boolean.class));

        IntStream.range(0, frame.rowCount()).forEach(i -> {
            IntStream.range(0, frame.colCount()).forEach(j -> {
                var value = frame.getBooleanAt(i, j);
                if (i != j) {
                    Assert.assertFalse(value);
                } else {
                    switch (i) {
                        case 0: Assert.assertTrue(value);    break;
                        case 1: Assert.assertTrue(value);    break;
                        case 2: Assert.assertTrue(value);    break;
                        case 3: Assert.assertTrue(value);    break;
                        case 4: Assert.assertTrue(value);    break;
                        default:    throw new IllegalStateException("Unexpected row: " + i);
                    }
                }
            });
        });
    }



    @Test(dataProvider="denseOrSparse")
    public void ints(boolean sparse) {
        var builder = DataFrame.builder(String.class, String.class).capacity(100, 100);
        if (sparse) builder.setLoadFactor(c -> 0.2f);
        var frame = builder
            .putInt("R0", "C0", 5)
            .putInt("R1", "C1", 10)
            .putInt("R2", "C2", 20)
            .putInt("R3", "C3", 30)
            .putInt("R4", "C4", 40)
            .build();

        frame.out().print();

        Assert.assertEquals(frame.rowCount(), 5);
        Assert.assertEquals(frame.colCount(), 5);
        Assert.assertEquals(frame.rows().keyClass(), String.class);
        Assert.assertEquals(frame.cols().keyClass(), String.class);
        frame.cols().forEach(v -> Assert.assertEquals(v.dataClass(), Integer.class));

        IntStream.range(0, frame.rowCount()).forEach(i -> {
            IntStream.range(0, frame.colCount()).forEach(j -> {
                var value = frame.getIntAt(i, j);
                if (i != j) {
                    Assert.assertEquals(value, 0);
                } else {
                    switch (i) {
                        case 0: Assert.assertEquals(value, 5);    break;
                        case 1: Assert.assertEquals(value, 10);    break;
                        case 2: Assert.assertEquals(value, 20);    break;
                        case 3: Assert.assertEquals(value, 30);    break;
                        case 4: Assert.assertEquals(value, 40);    break;
                        default:    throw new IllegalStateException("Unexpected row: " + i);
                    }
                }
            });
        });
    }


    @Test(dataProvider="denseOrSparse")
    public void longs(boolean sparse) {
        var builder = DataFrame.builder(String.class, String.class).capacity(100, 100);
        if (sparse) builder.setLoadFactor(c -> 0.2f);
        var frame = builder
            .putLong("R0", "C0", 5L)
            .putLong("R1", "C1", 10L)
            .putLong("R2", "C2", 20L)
            .putLong("R3", "C3", 30L)
            .putLong("R4", "C4", 40L)
            .build();

        frame.out().print();

        Assert.assertEquals(frame.rowCount(), 5);
        Assert.assertEquals(frame.colCount(), 5);
        Assert.assertEquals(frame.rows().keyClass(), String.class);
        Assert.assertEquals(frame.cols().keyClass(), String.class);
        frame.cols().forEach(v -> Assert.assertEquals(v.dataClass(), Long.class));

        IntStream.range(0, frame.rowCount()).forEach(i -> {
            IntStream.range(0, frame.colCount()).forEach(j -> {
                var value = frame.getLongAt(i, j);
                if (i != j) {
                    Assert.assertEquals(value, 0);
                } else {
                    switch (i) {
                        case 0: Assert.assertEquals(value, 5L);    break;
                        case 1: Assert.assertEquals(value, 10L);    break;
                        case 2: Assert.assertEquals(value, 20L);    break;
                        case 3: Assert.assertEquals(value, 30L);    break;
                        case 4: Assert.assertEquals(value, 40L);    break;
                        default:    throw new IllegalStateException("Unexpected row: " + i);
                    }
                }
            });
        });
    }



    @Test(dataProvider="denseOrSparse")
    public void doubles(boolean sparse) {
        var builder = DataFrame.builder(String.class, String.class).capacity(100, 100);
        if (sparse) builder.setLoadFactor(v -> 0.2f);
        var frame = builder
            .putDouble("R0", "C0", 1.1)
            .putDouble("R1", "C1", 2.2)
            .putDouble("R2", "C2", 3.3)
            .putDouble("R3", "C3", 4.4)
            .putDouble("R4", "C4", 5.5)
            .build();

        frame.out().print();

        Assert.assertEquals(frame.rowCount(), 5);
        Assert.assertEquals(frame.colCount(), 5);
        Assert.assertEquals(frame.rows().keyClass(), String.class);
        Assert.assertEquals(frame.cols().keyClass(), String.class);
        frame.cols().forEach(v -> Assert.assertEquals(v.dataClass(), Double.class));

        IntStream.range(0, frame.rowCount()).forEach(i -> {
            IntStream.range(0, frame.colCount()).forEach(j -> {
                var value = frame.getDoubleAt(i, j);
                if (i != j) {
                    Assert.assertEquals(value, Double.NaN, 0.000001d);
                } else {
                    switch (i) {
                        case 0: Assert.assertEquals(value, 1.1d, 0.000001d);    break;
                        case 1: Assert.assertEquals(value, 2.2d, 0.000001d);    break;
                        case 2: Assert.assertEquals(value, 3.3d, 0.000001d);    break;
                        case 3: Assert.assertEquals(value, 4.4d, 0.000001d);    break;
                        case 4: Assert.assertEquals(value, 5.5d, 0.000001d);    break;
                        default:    throw new IllegalStateException("Unexpected row: " + i);
                    }
                }
            });
        });
    }


    @Test(dataProvider="denseOrSparse")
    public void strings(boolean sparse) {
        var builder = DataFrame.builder(String.class, String.class).capacity(100, 100);
        if (sparse) builder.setLoadFactor(v -> 0.2f);
        var frame = builder
            .putValue("R0", "C0", "1.1")
            .putValue("R1", "C1", "2.2")
            .putValue("R2", "C2", "3.3")
            .putValue("R3", "C3", "4.4")
            .putValue("R4", "C4", "5.5")
            .build();

        frame.out().print();

        Assert.assertEquals(frame.rowCount(), 5);
        Assert.assertEquals(frame.colCount(), 5);
        Assert.assertEquals(frame.rows().keyClass(), String.class);
        Assert.assertEquals(frame.cols().keyClass(), String.class);
        frame.cols().forEach(v -> Assert.assertEquals(v.dataClass(), String.class));

        IntStream.range(0, frame.rowCount()).forEach(i -> {
            IntStream.range(0, frame.colCount()).forEach(j -> {
                var value = frame.<String>getValueAt(i, j);
                if (i != j) {
                    Assert.assertNull(value);
                } else {
                    switch (i) {
                        case 0: Assert.assertEquals(value, "1.1");    break;
                        case 1: Assert.assertEquals(value, "2.2");    break;
                        case 2: Assert.assertEquals(value, "3.3");    break;
                        case 3: Assert.assertEquals(value, "4.4");    break;
                        case 4: Assert.assertEquals(value, "5.5");    break;
                        default:    throw new IllegalStateException("Unexpected row: " + i);
                    }
                }
            });
        });
    }


    @Test()
    public void mixed() {
        var expected = DataFrame.read("/csv/cars93.csv").csv();
        var builder = DataFrame.builder(expected.rows().keyClass(), expected.cols().keyClass());
        expected.forEach(v -> builder.putValue(v.rowKey(), v.colKey(), v.getValue()));
        var result = builder.build();
        result.out().print();
        Assert.assertEquals(expected.rowCount(), result.rowCount());
        Assert.assertEquals(expected.colCount(), result.colCount());
        Assert.assertEquals(expected.rows().keyClass(), result.rows().keyClass());
        Assert.assertEquals(expected.cols().keyClass(), result.cols().keyClass());
        Assert.assertEquals(builder.rowCount(), expected.rowCount());
        Assert.assertEquals(builder.colCount(), expected.colCount());
        expected.cols().forEach(c -> Assert.assertEquals(c.dataClass(), result.col(c.key()).dataClass()));
        DataFrameAsserts.assertEqualsByIndex(result, expected);
    }


    @Test()
    public void threadSafe() {
        var expected = DataFrame.read("/csv/cars93.csv").csv();
        var builder = DataFrame.builder(expected.rows().keyClass(), expected.cols().keyClass()).threadSafe();
        Assert.assertTrue(builder.isThreadSafe());
        expected.parallel().forEach(v -> builder.putValue(v.rowKey(), v.colKey(), v.getValue()));
        Assert.assertEquals(builder.rowCount(), expected.rowCount());
        Assert.assertEquals(builder.colCount(), expected.colCount());
        var result = builder.build();
        result.out().print();
        DataFrameAsserts.assertEqualsByIndex(result, expected);
    }


    @Test()
    public void toBuilder() {
        var expected = DataFrame.read("/csv/cars93.csv").csv();
        var result = expected.toBuilder().build();
        result.out().print();
        DataFrameAsserts.assertEqualsByIndex(result, expected);
    }


    @Test()
    public void replaceRowKey() {
        var builder = DataFrame.builder(String.class, String.class);
        builder.putDouble("R0", "C0", Math.random());
        builder.putDouble("R1", "C1", Math.random());
        builder.putDouble("R2", "C2", Math.random());
        builder.replaceRowKey("R1", "X1");
        Assert.assertTrue(builder.hasRow("X1"));
        Assert.assertFalse(builder.hasRow("R1"));
        Assert.assertEquals(builder.rowKeys().collect(Collectors.toList()), List.of("R0", "X1", "R2"));
        Assert.assertEquals(builder.colKeys().collect(Collectors.toList()), List.of("C0", "C1", "C2"));
        var frame = builder.build();
        frame.out().print();
        Assert.assertEquals(frame.rowCount(), 3);
        Assert.assertEquals(frame.colCount(), 3);
        Assert.assertTrue(frame.rows().contains("R0"));
        Assert.assertTrue(frame.rows().contains("X1"));
        Assert.assertTrue(frame.rows().contains("R2"));
    }


    @Test()
    public void replaceColKey() {
        var builder = DataFrame.builder(String.class, String.class);
        builder.putDouble("X0", "C0", Math.random());
        builder.putDouble("X1", "C1", Math.random());
        builder.putDouble("X2", "C2", Math.random());
        builder.replaceColKey("C1", "X1");
        Assert.assertTrue(builder.hasColumn("X1"));
        Assert.assertFalse(builder.hasColumn("C1"));
        Assert.assertEquals(builder.rowKeys().collect(Collectors.toList()), List.of("X0", "X1", "X2"));
        Assert.assertEquals(builder.colKeys().collect(Collectors.toList()), List.of("C0", "C2", "X1"));
        var frame = builder.build();
        frame.out().print();
        Assert.assertEquals(frame.rowCount(), 3);
        Assert.assertEquals(frame.colCount(), 3);
        Assert.assertTrue(frame.cols().contains("C0"));
        Assert.assertTrue(frame.cols().contains("X1"));
        Assert.assertTrue(frame.cols().contains("C2"));
    }


    @Test()
    public void plusInts() {
        var frame = DataFrame.builder(String.class, String.class)
            .putInt("R0", "C0", 10)
            .putInt("R1", "C1", 20)
            .putInt("R2", "C2", 30)
            .plusInt("R1", "C1", 50)
            .build();

        frame.out().print();
        Assert.assertEquals(frame.getInt("R0", "C0"), 10);
        Assert.assertEquals(frame.getInt("R1", "C1"), 70);
        Assert.assertEquals(frame.getInt("R2", "C2"), 30);
    }

    @Test()
    public void plusLongs() {
        var frame = DataFrame.builder(String.class, String.class)
            .putLong("R0", "C0", 10L)
            .putLong("R1", "C1", 20L)
            .putLong("R2", "C2", 30L)
            .plusLong("R1", "C1", 50L)
            .build();

        frame.out().print();
        Assert.assertEquals(frame.getLong("R0", "C0"), 10L);
        Assert.assertEquals(frame.getLong("R1", "C1"), 70L);
        Assert.assertEquals(frame.getLong("R2", "C2"), 30L);
    }


    @Test()
    public void plusDoubles() {
        var frame = DataFrame.builder(String.class, String.class)
            .putDouble("R0", "C0", 10)
            .putDouble("R1", "C1", 20)
            .putDouble("R2", "C2", 30)
            .plusDouble("R1", "C1", 50)
            .build();

        frame.out().print();
        Assert.assertEquals(frame.getDouble("R0", "C0"), 10d, 0.000001d);
        Assert.assertEquals(frame.getDouble("R1", "C1"), 70d, 0.000001d);
        Assert.assertEquals(frame.getDouble("R2", "C2"), 30d, 0.000001d);
    }


    @Test()
    public void isNull() {
        var frame = DataFrame.builder(String.class, String.class);
        frame.capacity(100, 100);
        frame.addColumn("C1", double.class);
        frame.addColumn("C2", LocalDate.class);
        frame.addColumn("C3", String.class);
        IntStream.range(0, 20).forEach(i -> frame.putDouble("R" + i, "C1", Double.NaN));
        Assert.assertTrue(IntStream.range(0, 20).allMatch(i -> frame.isNull("R" + i, "C1")));
        Assert.assertTrue(IntStream.range(0, 20).allMatch(i -> frame.isNull("R" + i, "C2")));
        Assert.assertTrue(IntStream.range(0, 20).allMatch(i -> frame.isNull("R" + i, "C3")));
    }


    @Test(enabled = false)
    public void performance() {
        var rowCount = 1000000;
        var colCount = 10;
        IntStream.range(0, 10).forEach(x -> {
            var result = StopWatch.time(() -> {
                var builder = DataFrame.builder(Integer.class, String.class).capacity(rowCount, colCount);
                for (int i=0; i<rowCount; ++i) {
                    for (int j=0; j<colCount; ++j) {
                        builder.putDouble(i, "C" + j, Math.random());
                    }
                }
                return builder.build();
            });
            IO.println("\n\nBuilt DataFrame " + result.getValue().orElse(null) + " in " + result.getMillis() + " millis");
            result.getValue().ifPresent(v -> v.out().print());
        });
    }

}
