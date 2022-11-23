/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.vector;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class DataVectorMergeTest {
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testEmpty() {
        var merge = new DataVectorMerge<String, LocalDate>(false);
        Assert.assertTrue(merge.merge().isEmpty());
    }

    @Test
    public void testOneColumn() {
        var key1 = LocalDate.of(2022, 11, 22);
        var col1 = DataVector.of(Map.of("R1", 1.0, "R2", 2.0));
        var merge = new DataVectorMerge<String, LocalDate>(false);
        var frame = merge.addColumn(key1, col1).merge();

        Assert.assertEquals(frame.rowCount(), 2);
        Assert.assertEquals(frame.colCount(), 1);
        Assert.assertEquals(frame.listRowKeys(), List.of("R1", "R2"));
        Assert.assertEquals(frame.listColumnKeys(), List.of(key1));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testDuplicateColumn() {
        var key1 = LocalDate.of(2022, 11, 22);
        var col1 = DataVector.of(Map.of("R1", 1.0, "R2", 2.0));
        var merge = new DataVectorMerge<String, LocalDate>(false);

        merge.addColumn(key1, col1);
        merge.addColumn(key1, col1);
    }

    @Test
    public void testAllTrue() {
        var key1 = LocalDate.of(2022, 1, 2);
        var key2 = LocalDate.of(2022, 3, 4);
        var key3 = LocalDate.of(2022, 5, 6);

        var col1 = DataVector.of(Map.of("R1", 11.0, "R2", 12.0, "R3", 13.0));
        var col2 = DataVector.of(Map.of("R2", 22.0, "R3", 23.0, "R4", 24.0));
        var col3 = DataVector.of(Map.of("R3", 33.0, "R4", 34.0, "R5", 35.0));

        var merge = new DataVectorMerge<String, LocalDate>(true);
        var frame = merge
                .addColumn(key1, col1)
                .addColumn(key2, col2)
                .addColumn(key3, col3)
                .merge();

        Assert.assertEquals(frame.rowCount(), 5);
        Assert.assertEquals(frame.colCount(), 3);
        Assert.assertEquals(frame.listRowKeys(), List.of("R1", "R2", "R3", "R4", "R5"));
        Assert.assertEquals(frame.listColumnKeys(), List.of(key1, key2, key3));

        Assert.assertEquals(frame.getDouble("R1", key1), 11.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R2", key1), 12.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R3", key1), 13.0, TOLERANCE);
        Assert.assertTrue(Double.isNaN(frame.getDouble("R4", key1)));
        Assert.assertTrue(Double.isNaN(frame.getDouble("R5", key1)));

        Assert.assertTrue(Double.isNaN(frame.getDouble("R1", key2)));
        Assert.assertEquals(frame.getDouble("R2", key2), 22.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R3", key2), 23.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R4", key2), 24.0, TOLERANCE);
        Assert.assertTrue(Double.isNaN(frame.getDouble("R5", key2)));

        Assert.assertTrue(Double.isNaN(frame.getDouble("R1", key3)));
        Assert.assertTrue(Double.isNaN(frame.getDouble("R2", key3)));
        Assert.assertEquals(frame.getDouble("R3", key3), 33.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R4", key3), 34.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R5", key3), 35.0, TOLERANCE);
    }

    @Test
    public void testAllFalse() {
        var key1 = LocalDate.of(2022, 1, 2);
        var key2 = LocalDate.of(2022, 3, 4);
        var key3 = LocalDate.of(2022, 5, 6);

        var col1 = DataVector.of(Map.of("R1", 11.0, "R2", 12.0, "R3", 13.0, "R4", 14.0));
        var col2 = DataVector.of(Map.of("R2", 22.0, "R3", 23.0, "R4", 24.0, "R5", 25.0));
        var col3 = DataVector.of(Map.of("R3", 33.0, "R4", 34.0, "R5", 35.0, "R6", 36.0));

        var merge = new DataVectorMerge<String, LocalDate>(false);
        var frame = merge
                .addColumn(key1, col1)
                .addColumn(key2, col2)
                .addColumn(key3, col3)
                .merge();

        Assert.assertEquals(frame.rowCount(), 2);
        Assert.assertEquals(frame.colCount(), 3);
        Assert.assertEquals(frame.listRowKeys(), List.of("R3", "R4"));
        Assert.assertEquals(frame.listColumnKeys(), List.of(key1, key2, key3));

        Assert.assertEquals(frame.getDouble("R3", key1), 13.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R4", key1), 14.0, TOLERANCE);

        Assert.assertEquals(frame.getDouble("R3", key2), 23.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R4", key2), 24.0, TOLERANCE);

        Assert.assertEquals(frame.getDouble("R3", key3), 33.0, TOLERANCE);
        Assert.assertEquals(frame.getDouble("R4", key3), 34.0, TOLERANCE);
    }
}
