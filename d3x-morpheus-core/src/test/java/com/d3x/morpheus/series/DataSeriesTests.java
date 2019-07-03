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
package com.d3x.morpheus.series;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.d3x.core.json.JsonEngine;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.util.IO;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for data series
 *
 * @author Xavier Witdouck
 */
public class DataSeriesTests {

    private JsonEngine jsonEngine = DataSeriesJson.registerDefaults(new JsonEngine());


    @Test()
    public void csvRead() {
        var path = "/csv/aapl.csv";
        var series = DataSeries.<LocalDate,Double>csv(path).read("Date", "Adj Close").toDoubles();
        Assert.assertEquals(series.valueClass(), Double.class);
        Assert.assertEquals(series.valueType(), ArrayType.DOUBLE);
        Assert.assertEquals(series.keyClass(), LocalDate.class);
        Assert.assertEquals(series.keyType(), ArrayType.LOCAL_DATE);
        Assert.assertEquals(series.size(), 8503);
        Assert.assertEquals(series.firstKey().orNull(), LocalDate.parse("1980-12-12"));
        Assert.assertEquals(series.lastKey().orNull(), LocalDate.parse("2014-08-29"));
        Assert.assertEquals(series.getDouble(LocalDate.parse("1980-12-12")), 0.44203d, 0.000001d);
        Assert.assertEquals(series.getDouble(LocalDate.parse("2014-08-29")), 101.65627d, 0.000001d);
        Assert.assertEquals(series.toDoubles().stats().min(), 0.16913d, 0.000001d);
        Assert.assertEquals(series.toDoubles().stats().max(), 101.65627d, 0.000001d);
    }


    @Test()
    @SuppressWarnings("unchecked")
    public void jsonIO() {
        var path = "/csv/aapl.csv";
        var series = DataSeries.<LocalDate,Double>csv(path).read("Date", "Adj Close").toDoubles();
        var jsonIO = jsonEngine.io(DoubleSeries.typeOf(LocalDate.class));
        var jsonString = jsonIO.toString(series);
        var result = ((DataSeries<LocalDate,Double>)jsonIO.fromString(jsonString)).toDoubles();
        Assert.assertEquals(result.size(), series.size());
        Assert.assertEquals(result.valueClass(), series.valueClass());
        Assert.assertEquals(result.valueType(), series.valueType());
        Assert.assertEquals(result.keyClass(), series.keyClass());
        Assert.assertEquals(result.keyType(), series.keyType());
        series.getKeys().forEach(key -> {
            var v1 = series.getDouble(key);
            var v2 = result.getDouble(key);
            Assert.assertEquals(v2, v1, 0.000001d);
        });
    }


    @Test()
    public void mapKeys() {
        var path = "/csv/aapl.csv";
        var series = DataSeries.<LocalDate,Double>csv(path).read("Date", "Adj Close").toDoubles();
        var result = series.mapKeys(v -> v.minusDays(1)).toDoubles();
        Assert.assertEquals(result.size(), series.size());
        Assert.assertEquals(result.valueClass(), series.valueClass());
        Assert.assertEquals(result.valueType(), series.valueType());
        Assert.assertEquals(result.keyClass(), series.keyClass());
        Assert.assertEquals(result.keyType(), series.keyType());
        series.forEach(v -> {
            var ordinal = v.ordinal();
            var mapped = result.getKey(ordinal);
            var expected = v.key().minusDays(1);
            Assert.assertEquals(mapped, expected);
            Assert.assertEquals(result.getDoubleAt(ordinal), v.getDouble(), 0.000001d);
        });
    }


    @Test()
    public void filterKeys() {
        var path = "/csv/aapl.csv";
        var series = DataSeries.<LocalDate,Double>csv(path).read("Date", "Adj Close").toDoubles();
        var result1 = series.filter(v -> v.key().getDayOfWeek() == DayOfWeek.MONDAY).toDoubles();
        Assert.assertTrue(result1.size() > 0);
        Assert.assertTrue(result1.size() < series.size());
        Assert.assertEquals(result1.valueClass(), series.valueClass());
        Assert.assertEquals(result1.valueType(), series.valueType());
        Assert.assertEquals(result1.keyClass(), series.keyClass());
        Assert.assertEquals(result1.keyType(), series.keyType());
        result1.getKeys().forEach(key -> {
            Assert.assertEquals(key.getDayOfWeek(), DayOfWeek.MONDAY);
            Assert.assertEquals(result1.getDouble(key), series.getDouble(key));
        });
    }


    @Test()
    public void sortAscending() {
        var path = "/csv/aapl.csv";
        var series = DataSeries.<LocalDate,Double>csv(path).read("Date", "Adj Close").toDoubles();
        var result = series.sort((e1, e2) -> {
            var v1 = e1.getDouble();
            var v2 = e2.getDouble();
            return Double.compare(v1, v2);
        });
        Assert.assertTrue(result.size() > 0);
        Assert.assertEquals(result.size(),  series.size());
        Assert.assertNotEquals(result.firstKey(), series.firstKey());
        Assert.assertEquals(result.lastKey(), series.lastKey());
        Assert.assertEquals(series.firstKey().orNull(), LocalDate.parse("1980-12-12"));
        Assert.assertEquals(series.lastKey().orNull(), LocalDate.parse("2014-08-29"));
        Assert.assertEquals(result.firstKey().orNull(), LocalDate.parse("1982-07-08"));
        Assert.assertEquals(result.lastKey().orNull(), LocalDate.parse("2014-08-29"));
        for (int i=1; i<result.size(); ++i) {
            var v1 = result.getDoubleAt(i-1);
            var v2 = result.getDoubleAt(i);
            if (v1 > v2) {
                throw new RuntimeException("Series not sorted at " + i);
            }
        }
    }

    @Test()
    @SuppressWarnings("unchecked")
    public void strings() {
        var series = DataSeries.<Integer,String>builder(String.class).addValue(1, "Hello").addValue(2, "World").build();
        Assert.assertTrue(series.size() > 0);
        Assert.assertEquals(series.valueClass(), String.class);
        Assert.assertEquals(series.valueType(), ArrayType.STRING);
        Assert.assertEquals(series.keyClass(), Integer.class);
        Assert.assertEquals(series.keyType(), ArrayType.INTEGER);
        var jsonIO = jsonEngine.io(series.type());
        var jsonString = jsonIO.toString(series);
        IO.println(jsonString);
        var result = ((DataSeries<Integer,String>)jsonIO.fromString(jsonString));
        Assert.assertEquals(result.size(), series.size());
        Assert.assertEquals(result.valueClass(), series.valueClass());
        Assert.assertEquals(result.valueType(), series.valueType());
        Assert.assertEquals(result.keyClass(), series.keyClass());
        Assert.assertEquals(result.keyType(), series.keyType());
        series.getKeys().forEach(key -> {
            var v1 = series.getValue(key);
            var v2 = result.getValue(key);
            Assert.assertEquals(v2, v1);
        });
    }

}
