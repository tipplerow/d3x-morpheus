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
    private static final D3xVectorView vec1 = D3xVectorView.of();
    private static final D3xVectorView vec2 = D3xVectorView.of(1.0);
    private static final D3xVectorView vec3 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0);
    private static final D3xVectorView vec4 = D3xVectorView.of(1.0, -4.0, 33.0, 4.0);
    private static final D3xVectorView vec5 = D3xVectorView.of(-0.1, -4.0, -0.33, -4.0);

    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testMax() {
        var max = Statistic1.newInstance("Max");
        assertTrue(Double.isNaN(max.compute(vec1)));
        assertEquals(max.compute(vec2), 1.0, TOLERANCE);
        assertEquals(max.compute(vec3), 4.0, TOLERANCE);
        assertEquals(max.compute(vec4), 33.0, TOLERANCE);
        assertEquals(max.compute(vec5), -0.1, TOLERANCE);
    }

    @Test
    public void testMin() {
        var min = Statistic1.newInstance("Min");
        assertTrue(Double.isNaN(min.compute(vec1)));
        assertEquals(min.compute(vec2), 1.0, TOLERANCE);
        assertEquals(min.compute(vec3), 1.0, TOLERANCE);
        assertEquals(min.compute(vec4), -4.0, TOLERANCE);
        assertEquals(min.compute(vec5), -4.0, TOLERANCE);

    }

    @Test
    public void testSum() {
        var sum = Statistic1.newInstance("com.d3x.morpheus.stats.Sum");
        assertEquals(sum.compute(vec1), 0.0, TOLERANCE);
        assertEquals(sum.compute(vec2), 1.0, TOLERANCE);
        assertEquals(sum.compute(vec3), 10.0, TOLERANCE);
        assertEquals(sum.compute(vec5), -8.43, TOLERANCE);
    }

    @Test
    public void testVariance() {
        var variance = Statistic1.newInstance("com.d3x.morpheus.stats.Variance");
        assertTrue(Double.isNaN(variance.compute(vec1)));
        assertEquals(variance.compute(vec2), 0.0, TOLERANCE);
        assertEquals(variance.compute(vec3), 5.0 / 3.0, TOLERANCE);
        assertEquals(variance.compute(vec4), 833.0 / 3.0, TOLERANCE);
    }
}
