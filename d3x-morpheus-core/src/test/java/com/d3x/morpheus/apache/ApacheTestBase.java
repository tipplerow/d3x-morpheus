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
package com.d3x.morpheus.apache;

import java.util.List;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.DoubleComparator;

public class ApacheTestBase {
    protected final DataFrame<String, String> finalFrame = newFrame();

    protected static final List<String> rowKeys = List.of("row1", "row2", "row3");
    protected static final List<String> colKeys = List.of("col1", "col2", "col3", "col4");

    protected static final double TOLERANCE = 1.0E-12;
    protected static final DoubleComparator comparator = DoubleComparator.DEFAULT;

    protected static final double[][] finalData =
        new double[][] {
                { 11.0, 12.0, 13.0, 14.0 },
                { 21.0, 22.0, 23.0, 24.0 },
                { 31.0, 32.0, 33.0, 34.0 } };

    protected static DataFrame<String, String> newFrame() {
        DataFrame<String, String> frame = DataFrame.ofDoubles(rowKeys, colKeys);

        for (int irow = 0; irow < frame.rowCount(); irow++)
            for (int jcol = 0; jcol < frame.colCount(); jcol++)
                frame.setDoubleAt(irow, jcol, finalData[irow][jcol]);

        return frame;
    }
}
