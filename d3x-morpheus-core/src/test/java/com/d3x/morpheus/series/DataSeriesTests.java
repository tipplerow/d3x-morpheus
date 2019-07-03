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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for data series
 *
 * @author Xavier Witdouck
 */
public class DataSeriesTests {


    @Test()
    public void csvRead() {
        var path = "/csv/aapl.csv";
        var series = DataSeries.<LocalDate,Double>csv(path).read("Date", "Adj Close");
        Assert.assertEquals(series.dataClass(), Double.class);
        Assert.assertEquals(series.dataType(), ArrayType.DOUBLE);
        Assert.assertEquals(series.keyClass(), LocalDate.class);
        Assert.assertEquals(series.keyType(), ArrayType.LOCAL_DATE);
        Assert.assertEquals(series.size(), 8503);
        Assert.assertEquals(series.firstKey().orNull(), LocalDate.parse("1980-12-12"));
        Assert.assertEquals(series.lastKey().orNull(), LocalDate.parse("2014-08-29"));
        Assert.assertEquals(series.getValue(LocalDate.parse("1980-12-12")), 0.44203d, 0.000001d);
        Assert.assertEquals(series.getValue(LocalDate.parse("2014-08-29")), 101.65627d, 0.000001d);
        Assert.assertEquals(series.toDoubles().stats().min(), 0.16913d, 0.000001d);
        Assert.assertEquals(series.toDoubles().stats().max(), 101.65627d, 0.000001d);
    }


    @Test()
    @SuppressWarnings("unchecked")
    public void jsonIO() {
        var path = "/csv/aapl.csv";
        var series = DataSeries.<LocalDate,Double>csv(path).read("Date", "Adj Close").toDoubles();
        var jsonEngine = DataSeriesJson.registerDefaults(new JsonEngine());
        var jsonIO = jsonEngine.io(DoubleSeries.typeOf(LocalDate.class));
        var jsonString = jsonIO.toString(series);
        var result = ((DataSeries<LocalDate,Double>)jsonIO.fromString(jsonString)).toDoubles();
        Assert.assertEquals(result.size(), series.size());
        Assert.assertEquals(result.dataClass(), series.dataClass());
        Assert.assertEquals(result.dataType(), series.dataType());
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
        var result = series.mapKeys(v -> v.minusDays(1));
        Assert.assertEquals(result.size(), series.size());
        Assert.assertEquals(result.dataClass(), series.dataClass());
        Assert.assertEquals(result.dataType(), series.dataType());
        Assert.assertEquals(result.keyClass(), series.keyClass());
        Assert.assertEquals(result.keyType(), series.keyType());
        series.forEach((date, ordinal, value) -> {
            var mapped = result.getKey(ordinal);
            var expected = date.minusDays(1);
            Assert.assertEquals(mapped, expected);
            Assert.assertEquals(result.getDoubleAt(ordinal), value, 0.000001d);
        });
    }


    @Test()
    public void filterKeys() {
        var path = "/csv/aapl.csv";
        var series = DataSeries.<LocalDate,Double>csv(path).read("Date", "Adj Close").toDoubles();
        var result1 = series.filterKeys(v -> v.getDayOfWeek() == DayOfWeek.MONDAY).toDoubles();
        var result2 = series.filterDoubles((date, ordinal, value) -> date.getDayOfWeek() == DayOfWeek.MONDAY);
        Assert.assertTrue(result1.size() > 0);
        Assert.assertTrue(result1.size() < series.size());
        Assert.assertEquals(result1.size(), result2.size());
        Assert.assertEquals(result1.dataClass(), series.dataClass());
        Assert.assertEquals(result1.dataType(), series.dataType());
        Assert.assertEquals(result1.keyClass(), series.keyClass());
        Assert.assertEquals(result1.keyType(), series.keyType());
        result1.getKeys().forEach(key -> {
            Assert.assertEquals(key.getDayOfWeek(), DayOfWeek.MONDAY);
            Assert.assertEquals(result1.getDouble(key), series.getDouble(key));
            Assert.assertEquals(result1.getDouble(key), result2.getDouble(key));
        });
    }
}
