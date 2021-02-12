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
package com.d3x.morpheus.frame;

import java.util.List;

import com.d3x.morpheus.vector.D3xVector;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Tests default methods implemented in the DataFrameVector interface.
 */
public class DataFrameVectorTest extends DataFrameTestBase {
    @Test
    public void testGetDoubleArray1() {
        double[] rowarr1 = doubleFrame.row(row1).getDoubleArray();
        double[] colarr2 = doubleFrame.col(col2).getDoubleArray();

        assertTrue(comparator.equals(rowarr1, new double[] { 11.0, 12.0, 13.0 }));
        assertTrue(comparator.equals(colarr2, new double[] { 12.0, 22.0 }));
    }

    @Test
    public void testGetDoubleArray2() {
        double[] rowarr1 = doubleFrame.row(row1).getDoubleArray(List.of(col3, col1, col2));
        double[] colarr2 = doubleFrame.col(col2).getDoubleArray(List.of(row2, row1));

        assertTrue(comparator.equals(rowarr1, new double[] { 13.0, 11.0, 12.0 }));
        assertTrue(comparator.equals(colarr2, new double[] { 22.0, 12.0 }));
    }

    @Test
    public void testInnerProduct() {
        DataFrame<RowKey, Integer> rowFrame = DataFrame.ofDoubles(row1, List.of(2, 5, 10), D3xVector.wrap(1.0, 2.0, 3.0));
        DataFrame<Integer, ColKey> colFrame = DataFrame.ofDoubles(List.of(2, 5, 10), col1, D3xVector.wrap(2.0, 4.0, 6.0));

        assertEquals(rowFrame.row(row1).innerProduct(rowFrame.row(row1)), 14.0, 1.0E-12);
        assertEquals(rowFrame.row(row1).innerProduct(colFrame.col(col1)), 28.0, 1.0E-12);
        assertEquals(colFrame.col(col1).innerProduct(rowFrame.row(row1)), 28.0, 1.0E-12);
        assertEquals(colFrame.col(col1).innerProduct(colFrame.col(col1)), 56.0, 1.0E-12);
    }
}
