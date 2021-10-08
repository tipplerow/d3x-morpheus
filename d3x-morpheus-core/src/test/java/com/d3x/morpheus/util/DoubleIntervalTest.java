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
package com.d3x.morpheus.util;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author Scott Shaffer
 */
public class DoubleIntervalTest {
    private static final double LOWER = -2.0;
    private static final double UPPER =  1.0;

    private static final double SMALL = 1.0E-08;
    private static final double TINY  = 1.0E-15;

    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testClosed() {
        DoubleInterval interval = DoubleInterval.closed(LOWER, UPPER);

        assertFalse(interval.contains(LOWER - SMALL));
        assertTrue(interval.contains(LOWER - TINY));
        assertTrue(interval.contains(LOWER));
        assertTrue(interval.contains(LOWER + TINY));
        assertTrue(interval.contains(LOWER + SMALL));

        assertTrue(interval.contains(UPPER - SMALL));
        assertTrue(interval.contains(UPPER - TINY));
        assertTrue(interval.contains(UPPER));
        assertTrue(interval.contains(UPPER + TINY));
        assertFalse(interval.contains(UPPER + SMALL));

        assertTrue(interval.contains(0.0));
        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 3.0, TOLERANCE);
    }

    @Test public void testLeftClosed() {
        DoubleInterval interval = DoubleInterval.leftClosed(LOWER, UPPER);

        assertFalse(interval.contains(LOWER - SMALL));
        assertTrue(interval.contains(LOWER - TINY));
        assertTrue(interval.contains(LOWER));
        assertTrue(interval.contains(LOWER + TINY));
        assertTrue(interval.contains(LOWER + SMALL));

        assertTrue(interval.contains(UPPER - SMALL));
        assertFalse(interval.contains(UPPER - TINY));
        assertFalse(interval.contains(UPPER));
        assertFalse(interval.contains(UPPER + TINY));
        assertFalse(interval.contains(UPPER + SMALL));

        assertTrue(interval.contains(0.0));
        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 3.0, TOLERANCE);
    }

    @Test public void testLeftOpen() {
        DoubleInterval interval = DoubleInterval.leftOpen(LOWER, UPPER);

        assertFalse(interval.contains(LOWER - SMALL));
        assertFalse(interval.contains(LOWER - TINY));
        assertFalse(interval.contains(LOWER));
        assertFalse(interval.contains(LOWER + TINY));
        assertTrue(interval.contains(LOWER + SMALL));

        assertTrue(interval.contains(UPPER - SMALL));
        assertTrue(interval.contains(UPPER - TINY));
        assertTrue(interval.contains(UPPER));
        assertTrue(interval.contains(UPPER + TINY));
        assertFalse(interval.contains(UPPER + SMALL));

        assertTrue(interval.contains(0.0));
        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 3.0, TOLERANCE);
    }

    @Test public void testOpen() {
        DoubleInterval interval = DoubleInterval.open(LOWER, UPPER);

        assertFalse(interval.contains(LOWER - SMALL));
        assertFalse(interval.contains(LOWER - TINY));
        assertFalse(interval.contains(LOWER));
        assertFalse(interval.contains(LOWER + TINY));
        assertTrue(interval.contains(LOWER + SMALL));

        assertTrue(interval.contains(UPPER - SMALL));
        assertFalse(interval.contains(UPPER - TINY));
        assertFalse(interval.contains(UPPER));
        assertFalse(interval.contains(UPPER + TINY));
        assertFalse(interval.contains(UPPER + SMALL));

        assertTrue(interval.contains(0.0));
        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 3.0, TOLERANCE);
    }

    @Test public void testEmpty() {
        DoubleInterval interval = DoubleInterval.EMPTY;

        assertFalse(interval.contains(-1.0E+20));
        assertFalse(interval.contains(0.0));
        assertFalse(interval.contains(+1.0E+20));

        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 0.0, TOLERANCE);
    }

    @Test public void testFractional() {
        DoubleInterval interval = DoubleInterval.FRACTIONAL;

        assertFalse(interval.contains(-0.00000001));
        assertTrue(interval.contains(0.0));
        assertTrue(interval.contains(1.0));
        assertFalse(interval.contains(1.00000001));

        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 1.0, TOLERANCE);
    }

    @Test public void testPercentile() {
        DoubleInterval interval = DoubleInterval.PERCENTILE;

        assertFalse(interval.contains(-0.000001));
        assertTrue(interval.contains(0.0));
        assertTrue(interval.contains(100.0));
        assertFalse(interval.contains(100.000001));

        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 100.0, TOLERANCE);
    }

    @Test public void testInfinite() {
        DoubleInterval interval = DoubleInterval.INFINITE;

        assertTrue(interval.contains(Double.NEGATIVE_INFINITY));
        assertTrue(interval.contains(-1.0E+20));
        assertTrue(interval.contains( 0.0));
        assertTrue(interval.contains(+1.0E+20));
        assertTrue(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));
    }

    @Test public void testNegative() {
        DoubleInterval interval = DoubleInterval.NEGATIVE;

        assertTrue(interval.contains(Double.NEGATIVE_INFINITY));
        assertTrue(interval.contains(-1.0E+20));
        assertTrue(interval.contains(-1.0));
        assertFalse(interval.contains( 0.0));
        assertFalse(interval.contains(+1.0));
        assertFalse(interval.contains(+1.0E+20));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));
    }

    @Test public void testNonNegative() {
        DoubleInterval interval = DoubleInterval.NON_NEGATIVE;

        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(-1.0E+20));
        assertFalse(interval.contains(-1.0));
        assertTrue(interval.contains( 0.0));
        assertTrue(interval.contains(+1.0));
        assertTrue(interval.contains(+1.0E+20));
        assertTrue(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));
    }

    @Test public void testNonPositive() {
        DoubleInterval interval = DoubleInterval.NON_POSITIVE;

        assertTrue(interval.contains(Double.NEGATIVE_INFINITY));
        assertTrue(interval.contains(-1.0E+20));
        assertTrue(interval.contains(-1.0));
        assertTrue(interval.contains( 0.0));
        assertFalse(interval.contains(+1.0));
        assertFalse(interval.contains(+1.0E+20));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));
    }

    @Test public void testPositive() {
        DoubleInterval interval = DoubleInterval.POSITIVE;

        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(-1.0E+20));
        assertFalse(interval.contains(-1.0));
        assertFalse(interval.contains( 0.0));
        assertTrue(interval.contains(+1.0));
        assertTrue(interval.contains(+1.0E+20));
        assertTrue(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));
    }

    @Test public void testValidateOkay() {
        DoubleInterval.POSITIVE.validate(1.0, "test value");
        DoubleInterval.NEGATIVE.validate(-1.0, "test value");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testValidateBad1() {
        DoubleInterval.POSITIVE.validate(-1.0, "test value");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testValidateBad2() {
        DoubleInterval.NEGATIVE.validate(1.0, "test value");
    }
}
