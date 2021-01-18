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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DoubleComparatorTest {
    private final DoubleComparator fixed1 = DoubleComparator.fixed(0.0001);
    private final DoubleComparator fixed2 = DoubleComparator.FIXED_DEFAULT;

    private final DoubleComparator relative1 = DoubleComparator.relative(0.0001);
    private final DoubleComparator relative2 = DoubleComparator.RELATIVE_DEFAULT;

    private final double tinyNeg = -1.0E-14;
    private final double tinyPos = +1.0E-14;

    private final double smallNeg = -1.0E-06;
    private final double smallPos = +1.0E-06;

    @Test
    public void testEqualsArray() {
        double[] x1 = new double[] { 1.0, 2.0, 3.0 };
        double[] x2 = new double[] { 1.0 + 1.0E-14, 2.0 + 1.0E-14, 3.0 + 1.0E-14 };
        double[] x3 = new double[] { 1.0, 2.0, 4.0 };
        double[] x4 = new double[] { 1.0, 2.0 };
        double[] x5 = new double[] { 1.0, 2.0, 3.0, 4.0 };

        assertTrue(fixed2.equals(x1, x2));
        assertFalse(fixed2.equals(x1, x3));
        assertFalse(fixed2.equals(x1, x4));
        assertFalse(fixed2.equals(x1, x5));

        RealVector v1 = new ArrayRealVector(x1);
        RealVector v2 = new ArrayRealVector(x2);
        RealVector v3 = new ArrayRealVector(x3);
        RealVector v4 = new ArrayRealVector(x4);
        RealVector v5 = new ArrayRealVector(x5);

        assertTrue(fixed2.equals(v1, v2));
        assertFalse(fixed2.equals(v1, v3));
        assertFalse(fixed2.equals(v1, v4));
        assertFalse(fixed2.equals(v1, v5));
    }

    @Test
    public void testEqualsMatrix() {
        double[][] x1 = new double[][] {
                { 1.0, 2.0, 3.0 },
                { 4.0, 5.0, 6.0 }
        };

        double[][] x2 = new double[][] {
                { 1.0 + 1.0E-14, 2.0 + 1.0E-14, 3.0 + 1.0E-14 },
                { 4.0 + 1.0E-14, 5.0 + 1.0E-14, 6.0 + 1.0E-14 }
        };

        double[][] x3 = new double[][] {
                { 1.0, 2.0, 3.0 },
                { 4.0, 5.0, 7.0 }
        };

        double[][] x4 = new double[][] {
                { 1.0, 2.0, 3.0, 4.0 },
                { 4.0, 5.0, 6.0 }
        };

        double[][] x5 = new double[][] {
                { 1.0, 2.0 },
                { 3.0, 4.0 },
                { 5.0, 6.0 }
        };

        assertTrue(fixed2.equals(x1, x2));
        assertFalse(fixed2.equals(x1, x3));
        assertFalse(fixed2.equals(x1, x4));
        assertFalse(fixed2.equals(x1, x5));

        RealMatrix m1 = new BlockRealMatrix(x1);
        RealMatrix m2 = new Array2DRowRealMatrix(x2);
        RealMatrix m3 = new BlockRealMatrix(x3);
        RealMatrix m5 = new BlockRealMatrix(x5);

        assertTrue(fixed2.equals(m1, m2));
        assertFalse(fixed2.equals(m1, m3));
        assertFalse(fixed2.equals(m1, m5));
    }

    @Test
    public void testFixedCompare() {
        // The exact value...
        double x = 4.0 / 3.0;

        // Values less than x...
        double y1 = 1.333;
        double y2 = 1.33333;
        double y3 = 1.33333333333333;

        // Values greater than x...
        double z1 = 1.334;
        double z2 = 1.33334;
        double z3 = 1.33333333333334;

        assertTrue(fixed1.compare(x, y1) > 0);
        assertTrue(fixed1.compare(x, y2) == 0);
        assertTrue(fixed1.compare(x, y3) == 0);

        assertTrue(fixed2.compare(x, y1) > 0);
        assertTrue(fixed2.compare(x, y2) > 0);
        assertTrue(fixed2.compare(x, y3) == 0);

        assertTrue(fixed1.compare(x, z1) < 0);
        assertTrue(fixed1.compare(x, z2) == 0);
        assertTrue(fixed1.compare(x, z3) == 0);

        assertTrue(fixed2.compare(x, z1) < 0);
        assertTrue(fixed2.compare(x, z2) < 0);
        assertTrue(fixed2.compare(x, z3) == 0);

        assertFalse(fixed1.equals(x, y1));
        assertTrue(fixed1.equals(x, y2));
        assertTrue(fixed1.equals(x, y3));

        assertFalse(fixed2.equals(x, y1));
        assertFalse(fixed2.equals(x, y2));
        assertTrue(fixed2.equals(x, y3));

        assertFalse(fixed1.equals(x, z1));
        assertTrue(fixed1.equals(x, z2));
        assertTrue(fixed1.equals(x, z3));

        assertFalse(fixed2.equals(x, z1));
        assertFalse(fixed2.equals(x, z2));
        assertTrue(fixed2.equals(x, z3));
    }

    @Test
    public void testRelativeCompare() {
        // The exact value...
        double x = 4.0 / 3.0;

        // Values less than x...
        double y1 = 1.333;
        double y2 = 1.33333;
        double y3 = 1.33333333333333;

        // Values greater than x...
        double z1 = 1.334;
        double z2 = 1.33334;
        double z3 = 1.33333333333334;

        assertTrue(relative1.compare(x, y1) > 0);
        assertTrue(relative1.compare(x, y2) == 0);
        assertTrue(relative1.compare(x, y3) == 0);

        assertTrue(relative2.compare(x, y1) > 0);
        assertTrue(relative2.compare(x, y2) > 0);
        assertTrue(relative2.compare(x, y3) == 0);

        assertTrue(relative1.compare(x, z1) < 0);
        assertTrue(relative1.compare(x, z2) == 0);
        assertTrue(relative1.compare(x, z3) == 0);

        assertTrue(relative2.compare(x, z1) < 0);
        assertTrue(relative2.compare(x, z2) < 0);
        assertTrue(relative2.compare(x, z3) == 0);

        assertFalse(relative1.equals(x, y1));
        assertTrue(relative1.equals(x, y2));
        assertTrue(relative1.equals(x, y3));

        assertFalse(relative2.equals(x, y1));
        assertFalse(relative2.equals(x, y2));
        assertTrue(relative2.equals(x, y3));

        assertFalse(relative1.equals(x, z1));
        assertTrue(relative1.equals(x, z2));
        assertTrue(relative1.equals(x, z3));

        assertFalse(relative2.equals(x, z1));
        assertFalse(relative2.equals(x, z2));
        assertTrue(relative2.equals(x, z3));

        double rescale = 1.0E+12;

        x *= rescale;

        y1 *= rescale;
        y2 *= rescale;
        y3 *= rescale;

        z1 *= rescale;
        z2 *= rescale;
        z3 *= rescale;

        assertTrue(relative1.compare(x, y1) > 0);
        assertTrue(relative1.compare(x, y2) == 0);
        assertTrue(relative1.compare(x, y3) == 0);

        assertTrue(relative2.compare(x, y1) > 0);
        assertTrue(relative2.compare(x, y2) > 0);
        assertTrue(relative2.compare(x, y3) == 0);

        assertTrue(relative1.compare(x, z1) < 0);
        assertTrue(relative1.compare(x, z2) == 0);
        assertTrue(relative1.compare(x, z3) == 0);

        assertTrue(relative2.compare(x, z1) < 0);
        assertTrue(relative2.compare(x, z2) < 0);
        assertTrue(relative2.compare(x, z3) == 0);

        assertFalse(relative1.equals(x, y1));
        assertTrue(relative1.equals(x, y2));
        assertTrue(relative1.equals(x, y3));

        assertFalse(relative2.equals(x, y1));
        assertFalse(relative2.equals(x, y2));
        assertTrue(relative2.equals(x, y3));

        assertFalse(relative1.equals(x, z1));
        assertTrue(relative1.equals(x, z2));
        assertTrue(relative1.equals(x, z3));

        assertFalse(relative2.equals(x, z1));
        assertFalse(relative2.equals(x, z2));
        assertTrue(relative2.equals(x, z3));
    }

    @Test
    public void testIsZero() {
        assertFalse(fixed1.isZero(1.0));
        assertTrue(fixed1.isZero(smallPos));
        assertTrue(fixed1.isZero(tinyPos));
        assertTrue(fixed1.isZero(0.0));
        assertTrue(fixed1.isZero(tinyNeg));
        assertTrue(fixed1.isZero(smallNeg));
        assertFalse(fixed1.isZero(-1.0));

        assertFalse(fixed2.isZero(1.0));
        assertFalse(fixed2.isZero(smallPos));
        assertTrue(fixed2.isZero(tinyPos));
        assertTrue(fixed2.isZero(0.0));
        assertTrue(fixed2.isZero(tinyNeg));
        assertFalse(fixed2.isZero(smallNeg));
        assertFalse(fixed2.isZero(-1.0));
    }

    @Test
    public void testisNonZero() {
        assertTrue(fixed1.isNonZero(1.0));
        assertFalse(fixed1.isNonZero(smallPos));
        assertFalse(fixed1.isNonZero(tinyPos));
        assertFalse(fixed1.isNonZero(0.0));
        assertFalse(fixed1.isNonZero(tinyNeg));
        assertFalse(fixed1.isNonZero(smallNeg));
        assertTrue(fixed1.isNonZero(-1.0));

        assertTrue(fixed2.isNonZero(1.0));
        assertTrue(fixed2.isNonZero(smallPos));
        assertFalse(fixed2.isNonZero(tinyPos));
        assertFalse(fixed2.isNonZero(0.0));
        assertFalse(fixed2.isNonZero(tinyNeg));
        assertTrue(fixed2.isNonZero(smallNeg));
        assertTrue(fixed2.isNonZero(-1.0));
    }

    @Test
    public void testIsNegative() {
        assertFalse(fixed1.isNegative(1.0));
        assertFalse(fixed1.isNegative(smallPos));
        assertFalse(fixed1.isNegative(tinyPos));
        assertFalse(fixed1.isNegative(0.0));
        assertFalse(fixed1.isNegative(tinyNeg));
        assertFalse(fixed1.isNegative(smallNeg));
        assertTrue(fixed1.isNegative(-1.0));

        assertFalse(fixed2.isNegative(1.0));
        assertFalse(fixed2.isNegative(smallPos));
        assertFalse(fixed2.isNegative(tinyPos));
        assertFalse(fixed2.isNegative(0.0));
        assertFalse(fixed2.isNegative(tinyNeg));
        assertTrue(fixed2.isNegative(smallNeg));
        assertTrue(fixed2.isNegative(-1.0));
    }

    @Test
    public void testIsPositive() {
        assertTrue(fixed1.isPositive(1.0));
        assertFalse(fixed1.isPositive(smallPos));
        assertFalse(fixed1.isPositive(tinyPos));
        assertFalse(fixed1.isPositive(0.0));
        assertFalse(fixed1.isPositive(tinyNeg));
        assertFalse(fixed1.isPositive(smallNeg));
        assertFalse(fixed1.isPositive(-1.0));

        assertTrue(fixed2.isPositive(1.0));
        assertTrue(fixed2.isPositive(smallPos));
        assertFalse(fixed2.isPositive(tinyPos));
        assertFalse(fixed2.isPositive(0.0));
        assertFalse(fixed2.isPositive(tinyNeg));
        assertFalse(fixed2.isPositive(smallNeg));
        assertFalse(fixed2.isPositive(-1.0));
    }

    @Test
    public void testIsNonPositive() {
        assertFalse(fixed1.isNonPositive(1.0));
        assertTrue(fixed1.isNonPositive(smallPos));
        assertTrue(fixed1.isNonPositive(tinyPos));
        assertTrue(fixed1.isNonPositive(0.0));
        assertTrue(fixed1.isNonPositive(tinyNeg));
        assertTrue(fixed1.isNonPositive(smallNeg));
        assertTrue(fixed1.isNonPositive(-1.0));

        assertFalse(fixed2.isNonPositive(1.0));
        assertFalse(fixed2.isNonPositive(smallPos));
        assertTrue(fixed2.isNonPositive(tinyPos));
        assertTrue(fixed2.isNonPositive(0.0));
        assertTrue(fixed2.isNonPositive(tinyNeg));
        assertTrue(fixed2.isNonPositive(smallNeg));
        assertTrue(fixed2.isNonPositive(-1.0));
    }

    @Test
    public void testIsNonNegative() {
        assertTrue(fixed1.isNonNegative(1.0));
        assertTrue(fixed1.isNonNegative(smallPos));
        assertTrue(fixed1.isNonNegative(tinyPos));
        assertTrue(fixed1.isNonNegative(0.0));
        assertTrue(fixed1.isNonNegative(tinyNeg));
        assertTrue(fixed1.isNonNegative(smallNeg));
        assertFalse(fixed1.isNonNegative(-1.0));

        assertTrue(fixed2.isNonNegative(1.0));
        assertTrue(fixed2.isNonNegative(smallPos));
        assertTrue(fixed2.isNonNegative(tinyPos));
        assertTrue(fixed2.isNonNegative(0.0));
        assertTrue(fixed2.isNonNegative(tinyNeg));
        assertFalse(fixed2.isNonNegative(smallNeg));
        assertFalse(fixed2.isNonNegative(-1.0));
    }

    @Test
    public void testSign() {
        assertEquals(fixed1.sign(1.0), 1);
        assertEquals(fixed1.sign(smallPos), 0);
        assertEquals(fixed1.sign(tinyPos), 0);
        assertEquals(fixed1.sign(0.0), 0);
        assertEquals(fixed1.sign(tinyNeg), 0);
        assertEquals(fixed1.sign(smallNeg), 0);
        assertEquals(fixed1.sign(-1.0), -1);

        assertEquals(fixed2.sign(1.0), 1);
        assertEquals(fixed2.sign(smallPos), 1);
        assertEquals(fixed2.sign(tinyPos), 0);
        assertEquals(fixed2.sign(0.0), 0);
        assertEquals(fixed2.sign(tinyNeg), 0);
        assertEquals(fixed2.sign(smallNeg), -1);
        assertEquals(fixed2.sign(-1.0), -1);
    }

    @Test
    public void testNaN() {
        DoubleComparator cmp = DoubleComparator.DEFAULT;

        // The IEEE standard defines NaN as greater than all other
        // floating point values and equal to itself.
        assertEquals(cmp.compare(Double.NaN, Double.NEGATIVE_INFINITY), 1);
        assertEquals(cmp.compare(Double.NaN, 0.0), 1);
        assertEquals(cmp.compare(Double.NaN, Double.POSITIVE_INFINITY), 1);
        assertEquals(cmp.compare(Double.NaN, Double.NaN), 0);
    }

    @Test
    public void testInfinity() {
        DoubleComparator cmp = DoubleComparator.DEFAULT;

        assertEquals(cmp.compare(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), 0);
        assertEquals(cmp.compare(Double.NEGATIVE_INFINITY, 0.0), -1);
        assertEquals(cmp.compare(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), -1);

        assertEquals(cmp.compare(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), 1);
        assertEquals(cmp.compare(Double.POSITIVE_INFINITY, 0.0), 1);
        assertEquals(cmp.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), 0);
    }

    @Test
    public void testEpsilon() {
        // The machine tolerance should be something like 2.2E-16...
        assertTrue(DoubleComparator.epsilon() < 1.0E-15);
    }
}
