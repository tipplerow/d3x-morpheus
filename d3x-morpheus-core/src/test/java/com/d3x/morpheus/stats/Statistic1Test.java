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

import com.d3x.morpheus.vector.D3xVectorView;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class Statistic1Test {
    private static final Statistic1 max = new Max();
    private static final Statistic1 min = new Min();
    private static final Statistic1 sum = new Sum();

    private static final D3xVectorView vec1 = D3xVectorView.of();
    private static final D3xVectorView vec2 = D3xVectorView.of(1.0);
    private static final D3xVectorView vec3 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0);
    private static final D3xVectorView vec4 = D3xVectorView.of(1.0, -4.0, 33.0, 4.0);

    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testMax() {
        assertTrue(Double.isNaN(max.compute(vec1)));
        assertEquals(max.compute(vec2), 1.0, TOLERANCE);
        assertEquals(max.compute(vec3), 4.0, TOLERANCE);
        assertEquals(max.compute(vec4), 33.0, TOLERANCE);
    }

    @Test
    public void testMin() {
        assertTrue(Double.isNaN(min.compute(vec1)));
        assertEquals(min.compute(vec2), 1.0, TOLERANCE);
        assertEquals(min.compute(vec3), 1.0, TOLERANCE);
        assertEquals(min.compute(vec4), -4.0, TOLERANCE);

    }

    @Test
    public void testSum() {
        assertEquals(sum.compute(vec1), 0.0, TOLERANCE);
        assertEquals(sum.compute(vec2), 1.0, TOLERANCE);
        assertEquals(sum.compute(vec3), 10.0, TOLERANCE);
    }
}
