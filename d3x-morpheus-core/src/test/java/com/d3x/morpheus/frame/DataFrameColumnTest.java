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

import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.vector.D3xVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DataFrameColumnTest extends DataFrameTestBase {
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testSeriesInnerProduct() {
        DataFrame<RowKey, ColKey> frame =
                DataFrame.ofDoubles(List.of(row1, row2, row3), col1, D3xVector.wrap(2.0, 3.0, 4.0));

        DoubleSeries<RowKey> series =
                DoubleSeries.build(RowKey.class, List.of(row2, row3, row4), D3xVector.wrap(10.0, 20.0, 30.0));

        assertEquals(frame.col(col1).innerProduct(series, 0.0), 110.0, TOLERANCE);
        assertEquals(frame.col(col1).innerProduct(series, 1.0), 112.0, TOLERANCE);
        assertTrue(Double.isNaN(frame.col(col1).innerProduct(series, Double.NaN)));
    }

    @Test
    public void testVectorInnerProduct() {
        DataFrame<RowKey, ColKey> frame = DataFrame.ofDoubles(
                List.of(row1, row2),
                List.of(col1, col2, col3),
                D3xMatrix.byrow(2, 3,
                        1.0, 2.0, 3.0,
                        4.0, 5.0, 6.0));

        assertEquals(frame.col(col1).innerProduct(frame.col(col1)), 17.0, TOLERANCE);
        assertEquals(frame.col(col1).innerProduct(frame.col(col2)), 22.0, TOLERANCE);
        assertEquals(frame.col(col1).innerProduct(frame.col(col3)), 27.0, TOLERANCE);
        assertEquals(frame.col(col2).innerProduct(frame.col(col1)), 22.0, TOLERANCE);
        assertEquals(frame.col(col2).innerProduct(frame.col(col2)), 29.0, TOLERANCE);
        assertEquals(frame.col(col2).innerProduct(frame.col(col3)), 36.0, TOLERANCE);
        assertEquals(frame.col(col3).innerProduct(frame.col(col1)), 27.0, TOLERANCE);
        assertEquals(frame.col(col3).innerProduct(frame.col(col2)), 36.0, TOLERANCE);
        assertEquals(frame.col(col3).innerProduct(frame.col(col3)), 45.0, TOLERANCE);
    }
}
