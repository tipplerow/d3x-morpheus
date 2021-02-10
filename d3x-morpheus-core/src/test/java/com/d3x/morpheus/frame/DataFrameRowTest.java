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

import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.vector.D3xVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DataFrameRowTest {
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testScalarProduct() {
        DataFrame<String, String> frame =
                DataFrame.ofDoubles("row", List.of("col1", "col2", "col3"), D3xVector.wrap(2.0, 3.0, 4.0));

        DoubleSeries<String> column =
                DoubleSeries.build(String.class, List.of("col2", "col3", "col4"), D3xVector.wrap(10.0, 20.0, 30.0));

        assertEquals(frame.row("row").scalarProduct(column, 0.0), 110.0, TOLERANCE);
        assertEquals(frame.row("row").scalarProduct(column, 1.0), 112.0, TOLERANCE);
        assertTrue(Double.isNaN(frame.row("row").scalarProduct(column, Double.NaN)));
    }
}
