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
import java.util.RandomAccess;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Tests default methods implemented in the DataFrameAxis interface.
 */
public class DataFrameAxisTest {
    private static final List<String> rowKeys = List.of("row1", "row2");
    private static final List<String> colKeys = List.of("col1", "col2", "col3");
    private static final DataFrame<String, String> frame = DataFrame.ofDoubles(rowKeys, colKeys);

    @Test
    public void testKeyListContents() {
        assertEquals(frame.rows().keyList(), rowKeys);
        assertEquals(frame.cols().keyList(), colKeys);
    }

    @Test
    public void testKeyListRandomAccess() {
        //
        // Ensure efficient indexing...
        //
        assertTrue(frame.rows().keyList() instanceof RandomAccess);
        assertTrue(frame.cols().keyList() instanceof RandomAccess);
    }
}
