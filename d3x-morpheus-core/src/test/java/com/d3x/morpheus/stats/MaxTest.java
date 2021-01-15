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
package com.d3x.morpheus.stats;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class MaxTest {
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testOf() {
        assertTrue(Double.isNaN(Max.of()));
        assertTrue(Double.isNaN(Max.of(new double[] {})));

        assertEquals(Max.of(1.0), 1.0, TOLERANCE);
        assertEquals(Max.of(1.0, 4.0, 3.0, 2.0), 4.0, TOLERANCE);

        assertEquals(Max.of(new double[] { 1.0 }), 1.0, TOLERANCE);
        assertEquals(Max.of(new double[] { 1.0, 4.0, 33.0, 4.0 }), 33.0, TOLERANCE);
    }
}
