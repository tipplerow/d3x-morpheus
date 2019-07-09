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
package com.d3x.morpheus.array;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.d3x.morpheus.range.Range;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for the ArrayBuilder class.
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class ArrayBuilderTests {

    @DataProvider(name = "sizes")
    public Object[][] sizes() {
        return new Object[][] {{0}, {7}, {100}, {5000}};
    }

    @DataProvider(name = "types")
    public Object[][] types() {
        return new Object[][] {
            { Object.class },
            { Boolean.class },
            { Integer.class },
            { Long.class },
            { Double.class },
            { String.class },
            { Month.class },
            { Currency.class },
            { Date.class },
            { LocalDate.class },
            { LocalTime.class },
            { LocalDateTime.class },
            { ZonedDateTime.class }
        };
    }


    @Test()
    public void testEmpty() {
        final ArrayBuilder<Boolean> builder = ArrayBuilder.of(10);
        final Array<Boolean> empty = builder.toArray();
        Assert.assertEquals(empty.length(), 0, "The array is zero length");
        Assert.assertEquals(empty.typeCode(), ArrayType.OBJECT);
    }


    @Test(dataProvider = "types")
    public void testEmptyWithType(Class<?> type) {
        final ArrayBuilder<?> builder = ArrayBuilder.of(10, type);
        final Array<?> empty = builder.toArray();
        Assert.assertEquals(empty.length(), 0, "The array is zero length");
        Assert.assertEquals(empty.typeCode(), ArrayType.of(type), "The array types match");
    }


    @Test()
    public void testWhenExpansionRequired() {
        final ArrayBuilder<Double> builder = ArrayBuilder.of(10, Double.class);
        Range.of(0, 101).forEach(i -> builder.appendDouble(Math.random()));
        final Array<Double> array = builder.toArray();
        Assert.assertEquals(array.length(), 101, "Array length matches expected");
    }


    @Test()
    public void testWhenContractionRequired() {
        final ArrayBuilder<Double> builder = ArrayBuilder.of(1000, Double.class);
        Range.of(0, 101).forEach(i -> builder.appendDouble(Math.random()));
        final Array<Double> array = builder.toArray();
        Assert.assertEquals(array.length(), 101, "Array length matches expected");
    }


    @Test(dataProvider = "sizes")
    public void testWithBooleans(int initialSize) {
        final Random random = new Random();
        final ArrayBuilder<Boolean> builder = ArrayBuilder.of(initialSize);
        final boolean[] expected = new boolean[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = random.nextBoolean();
            builder.append(expected[i]);
        }
        final Array<Boolean> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.BOOLEAN, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getBoolean(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithIntegers(int initialSize) {
        final Random random = new Random();
        final ArrayBuilder<Integer> builder = ArrayBuilder.of(initialSize);
        var expected = new int[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = random.nextInt();
            builder.append(expected[i]);
        }
        final Array<Integer> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.INTEGER, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getInt(i), expected[i], "The values match at " + i);
        }
        final Array<Integer> collected = IntStream.of(expected).boxed().collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getInt(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithLongs(int initialSize) {
        final Random random = new Random();
        final ArrayBuilder<Long> builder = ArrayBuilder.of(initialSize);
        final long[] expected = new long[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = random.nextLong();
            builder.append(expected[i]);
        }
        final Array<Long> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.LONG, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getLong(i), expected[i], "The values match at " + i);
        }
        final Array<Long> collected = LongStream.of(expected).boxed().collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getLong(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithDoubles(int initialSize) {
        final Random random = new Random();
        final ArrayBuilder<Double> builder = ArrayBuilder.of(initialSize);
        final double[] expected = new double[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = random.nextDouble();
            builder.append(expected[i]);
        }
        final Array<Double> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.DOUBLE, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getDouble(i), expected[i], "The values match at " + i);
        }
        final Array<Double> collected = DoubleStream.of(expected).boxed().collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getDouble(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithStrings(int initialSize) {
        final Random random = new Random();
        final ArrayBuilder<String> builder = ArrayBuilder.of(initialSize);
        final String[] expected = new String[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = "X=" + random.nextDouble();
            builder.append(expected[i]);
        }
        final Array<String> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.STRING, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<String> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithDates(int initialSize) {
        final ArrayBuilder<Date> builder = ArrayBuilder.of(initialSize);
        final Date[] expected = new Date[1000];
        final Calendar calender = Calendar.getInstance();
        for (int i=0; i<expected.length; ++i) {
            calender.add(Calendar.DATE, 1);
            expected[i] = calender.getTime();
            builder.append(expected[i]);
        }
        final Array<Date> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.DATE, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<Date> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithCurrencies(int initialSize) {
        final ArrayBuilder<Currency> builder = ArrayBuilder.of(initialSize);
        final Currency[] expected = Currency.getAvailableCurrencies().stream().toArray(Currency[]::new);
        for (Currency exp : expected) {
            builder.append(exp);
        }
        final Array<Currency> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.CURRENCY, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<Currency> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithTimeZones(int initialSize) {
        final ArrayBuilder<TimeZone> builder = ArrayBuilder.of(initialSize);
        final TimeZone[] expected = Stream.of(TimeZone.getAvailableIDs()).map(TimeZone::getTimeZone).toArray(TimeZone[]::new);
        for (TimeZone exp : expected) {
            builder.append(exp);
        }
        final Array<TimeZone> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.TIME_ZONE, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<TimeZone> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithZonedIds(int initialSize) {
        final ArrayBuilder<ZoneId> builder = ArrayBuilder.of(initialSize);
        final ZoneId[] expected = ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).toArray(ZoneId[]::new);
        for (ZoneId exp : expected) {
            builder.append(exp);
        }
        final Array<ZoneId> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.ZONE_ID, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<ZoneId> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithLocalDates(int initialSize) {
        final ArrayBuilder<LocalDate> builder = ArrayBuilder.of(initialSize);
        final LocalDate[] expected = new LocalDate[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = LocalDate.now().plusDays(i);
            builder.append(expected[i]);
        }
        final Array<LocalDate> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.LOCAL_DATE, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<LocalDate> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithLocalTimes(int initialSize) {
        final ArrayBuilder<LocalTime> builder = ArrayBuilder.of(initialSize);
        final LocalTime[] expected = new LocalTime[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = LocalTime.now().plusMinutes(i);
            builder.append(expected[i]);
        }
        final Array<LocalTime> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.LOCAL_TIME, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<LocalTime> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithLocalDateTimes(int initialSize) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        ArrayBuilder<LocalDateTime> builder = ArrayBuilder.of(initialSize);
        LocalDateTime[] expected = new LocalDateTime[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = now.plusMinutes(i);
            builder.append(expected[i]);
        }
        Array<LocalDateTime> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.LOCAL_DATETIME, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            LocalDateTime x = actual.getValue(i);
            LocalDateTime y = expected[i];
            Assert.assertEquals(x, y, "The values match at " + i);
        }
        final Array<LocalDateTime> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithZonedDateTimes(int initialSize) {
        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        ArrayBuilder<ZonedDateTime> builder = ArrayBuilder.of(initialSize);
        ZonedDateTime[] expected = new ZonedDateTime[1000];
        for (int i=0; i<expected.length; ++i) {
            expected[i] = now.plusMinutes(i);
            builder.append(expected[i]);
        }
        Array<ZonedDateTime> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.ZONED_DATETIME, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<ZonedDateTime> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }


    @Test(dataProvider = "sizes")
    public void testWithMixedTypes(int initialSize) {
        final Random random = new Random();
        final ArrayBuilder<Object> builder = ArrayBuilder.of(initialSize);
        final Object[] expected = new Object[1000];
        for (int i=0; i<100; ++i) {
            expected[i] = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS).plusMinutes(i);
            builder.append(expected[i]);
        }
        for (int i=100; i<200; ++i) {
            expected[i] = random.nextDouble();
            builder.append(expected[i]);
        }
        for (int i=200; i<300; ++i) {
            expected[i] = random.nextLong();
            builder.append(expected[i]);
        }
        for (int i=300; i<400; ++i) {
            expected[i] = random.nextInt();
            builder.append(expected[i]);
        }
        for (int i=400; i<1000; ++i) {
            expected[i] = random.nextBoolean();
            builder.append(expected[i]);
        }
        final Array<Object> actual = builder.toArray();
        Assert.assertEquals(actual.length(), expected.length, "The lengths match");
        Assert.assertEquals(actual.typeCode(), ArrayType.OBJECT, "The array type is as expected");
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(actual.getValue(i), expected[i], "The values match at " + i);
        }
        final Array<Object> collected = Stream.of(expected).collect(ArrayUtils.toArray(expected.length));
        for (int i=0; i<expected.length; ++i) {
            Assert.assertEquals(collected.getValue(i), expected[i], "The values match at " + i);
        }
    }

    @Test()
    public void testConcat() {
        final ArrayBuilder<Double> builder1 = ArrayBuilder.of(20);
        final ArrayBuilder<Double> builder2 = ArrayBuilder.of(20);
        final Array<Double> array1 = Range.of(0, 1000).map(i -> Math.random()).toArray();
        final Array<Double> array2 = Range.of(0, 768).map(i -> Math.random()).toArray();
        Assert.assertEquals(array1.length(), 1000);
        Assert.assertEquals(array2.length(), 768);
        array1.forEachValue(v -> builder1.appendDouble(v.getDouble()));
        array2.forEachValue(v -> builder2.appendDouble(v.getDouble()));
        final Array<Double> combined = builder1.appendAll(builder2).toArray();
        Assert.assertEquals(combined.length(), array1.length() + array2.length());
        for (int i=0; i<array1.length(); ++i) {
            final double v1 = array1.getDouble(i);
            final double v2 = combined.getDouble(i);
            Assert.assertEquals(v2, v1, "Values match at index " + i);
        }
        for (int i=0; i<array2.length(); ++i) {
            final double v1 = array2.getDouble(i);
            final double v2 = combined.getDouble(i + array1.length());
            Assert.assertEquals(v2, v1, "Values match at index " + i);
        }
    }

    @Test()
    public void testCollector1() {
        final Array<Integer> array1 = IntStream.range(0, 1000).boxed().collect(ArrayUtils.toArray(100));
        final Array<Integer> array2 = IntStream.range(0, 1000).boxed().collect(ArrayUtils.toArray(Integer.class, 100));
        final Array<Integer> array3 = IntStream.range(0, 1000).boxed().collect(ArrayUtils.toArray(Integer.class, 100));
        Assert.assertEquals(array1, array2);
        Assert.assertEquals(array1, array3);
    }


    @Test()
    public void testBooleanSetters() {
        var builder = ArrayBuilder.of(20);
        builder.setBoolean(3, true);
        builder.setBoolean(5, true);
        builder.setBoolean(35, true);
        builder.appendBoolean(true);
        var result = builder.toArray();
        Assert.assertEquals(result.length(), 37);
        Assert.assertEquals(result.typeCode(), ArrayType.BOOLEAN);
        result.forEachValue(v -> {
            var index = v.index();
            switch (index) {
                case 3:     Assert.assertTrue(result.getBoolean(index));  break;
                case 5:     Assert.assertTrue(result.getBoolean(index));  break;
                case 35:    Assert.assertTrue(result.getBoolean(index));  break;
                case 36:    Assert.assertTrue(result.getBoolean(index));  break;
                default:    Assert.assertFalse(result.getBoolean(index));  break;
            }
        });
    }


    @Test()
    public void testIntSetters() {
        var builder = ArrayBuilder.of(20);
        builder.setInt(3, 30);
        builder.setInt(5, 50);
        builder.setInt(35, 350);
        builder.appendInt(600);
        var result = builder.toArray();
        Assert.assertEquals(result.length(), 37);
        Assert.assertEquals(result.typeCode(), ArrayType.INTEGER);
        result.forEachValue(v -> {
            var index = v.index();
            switch (index) {
                case 3:     Assert.assertEquals(result.getInt(index), 30);  break;
                case 5:     Assert.assertEquals(result.getInt(index), 50);  break;
                case 35:    Assert.assertEquals(result.getInt(index), 350);  break;
                case 36:    Assert.assertEquals(result.getInt(index), 600);  break;
                default:    Assert.assertEquals(result.getInt(index), 0);  break;
            }
        });
    }


    @Test()
    public void testLongSetters() {
        var builder = ArrayBuilder.of(20);
        builder.setLong(3, 30);
        builder.setLong(5, 50);
        builder.setLong(35, 350);
        builder.appendLong(600);
        var result = builder.toArray();
        Assert.assertEquals(result.length(), 37);
        Assert.assertEquals(result.typeCode(), ArrayType.LONG);
        result.forEachValue(v -> {
            var index = v.index();
            switch (index) {
                case 3:     Assert.assertEquals(result.getLong(index), 30L);  break;
                case 5:     Assert.assertEquals(result.getLong(index), 50L);  break;
                case 35:    Assert.assertEquals(result.getLong(index), 350L);  break;
                case 36:    Assert.assertEquals(result.getLong(index), 600L);  break;
                default:    Assert.assertEquals(result.getLong(index), 0L);  break;
            }
        });
    }



    @Test()
    public void testDoubleSetters() {
        var builder = ArrayBuilder.of(20);
        builder.setDouble(3, 3.3d);
        builder.setDouble(5, 5.5d);
        builder.setDouble(35, 350d);
        builder.appendDouble(600d);
        var result = builder.toArray();
        Assert.assertEquals(result.length(), 37);
        Assert.assertEquals(result.typeCode(), ArrayType.DOUBLE);
        result.forEachValue(v -> {
            var index = v.index();
            switch (index) {
                case 3:     Assert.assertEquals(result.getDouble(index), 3.3d, 0.0000001);  break;
                case 5:     Assert.assertEquals(result.getDouble(index), 5.5d, 0.0000001);  break;
                case 35:    Assert.assertEquals(result.getDouble(index), 350d, 0.0000001);  break;
                case 36:    Assert.assertEquals(result.getDouble(index), 600d, 0.0000001);  break;
                default:    Assert.assertEquals(result.getDouble(index), Double.NaN, 0.000001d);  break;
            }
        });
    }


    @Test()
    public void testValueSetters() {
        var builder = ArrayBuilder.of(20);
        builder.setValue(3, true);
        builder.setInt(5, 50);
        builder.setLong(35, 350L);
        builder.appendDouble(600d);
        builder.append("Hello");
        var result = builder.toArray();
        Assert.assertEquals(result.length(), 38);
        Assert.assertEquals(result.typeCode(), ArrayType.OBJECT);
        result.forEachValue(v -> {
            var index = v.index();
            switch (index) {
                case 3:     Assert.assertTrue(result.getBoolean(index));  break;
                case 5:     Assert.assertEquals(result.getInt(index), 50);  break;
                case 35:    Assert.assertEquals(result.getLong(index), 350L);  break;
                case 36:    Assert.assertEquals(result.getDouble(index), 600d, 0.0000001);  break;
                case 37:    Assert.assertEquals(result.getValue(index), "Hello");  break;
                default:
                    if (index < 20) {
                        Assert.assertEquals(result.getValue(index), Boolean.FALSE);  break;
                    } else {
                        Assert.assertNull(result.getValue(index));  break;
                    }
            }
        });
    }

}
