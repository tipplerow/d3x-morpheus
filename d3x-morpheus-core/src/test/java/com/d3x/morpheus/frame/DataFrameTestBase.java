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

import com.d3x.morpheus.util.DoubleComparator;

/**
 * Provides common data for tests in the {@code com.d3x.morpheus.frame} package.
 */
public abstract class DataFrameTestBase {
    protected final DataFrame<String, String> intFrame = newIntFrame();
    protected final DataFrame<String, String> doubleFrame = newDoubleFrame();

    protected static final List<String> rowKeys = List.of("row1", "row2");
    protected static final List<String> colKeys = List.of("col1", "col2", "col3");

    protected static final DoubleComparator comparator = DoubleComparator.FIXED_DEFAULT;

    protected static DataFrame<String, String> newIntFrame() {
        DataFrame<String, String> frame = DataFrame.ofInts(rowKeys, colKeys);

        frame.setInt("row1", "col1", 11);
        frame.setInt("row1", "col2", 12);
        frame.setInt("row1", "col3", 13);
        frame.setInt("row2", "col1", 21);
        frame.setInt("row2", "col2", 22);
        frame.setInt("row2", "col3", 23);

        return frame;
    }

    protected static DataFrame<String, String> newDoubleFrame() {
        DataFrame<String, String> frame = DataFrame.ofDoubles(rowKeys, colKeys);

        frame.setDouble("row1", "col1", 11.0);
        frame.setDouble("row1", "col2", 12.0);
        frame.setDouble("row1", "col3", 13.0);
        frame.setDouble("row2", "col1", 21.0);
        frame.setDouble("row2", "col2", 22.0);
        frame.setDouble("row2", "col3", 23.0);

        return frame;
    }
}
