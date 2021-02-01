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
package com.d3x.morpheus.matrix;

import java.util.List;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.D3xVectorTest;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class D3xMatrixTest {
    private final DataFrame<String, String> frame = D3xVectorTest.newFrame();

    private static final double TOLERANCE = 1.0E-12;
    private static final double[][] testArray = new double[][] {
            { 11.0, 12.0, 13.0 },
            { 21.0, 22.0, 23.0 }
    };

    @Test
    public void testByRow() {
        D3xMatrix actual = D3xMatrix.byrow(2, 3, 11.0, 12.0, 13.0, 21.0, 22.0, 23.0);
        assertTrue(actual.equalsArray(testArray));
    }

    @Test
    public void testCopyOfArray() {
        D3xMatrix matrix = D3xMatrix.copyOf(testArray);

        assertEquals(matrix.nrow(), 2);
        assertEquals(matrix.ncol(), 3);
        assertEquals(matrix.size(), 6);

        assertEquals(matrix.get(0, 0), 11.0, TOLERANCE);
        assertEquals(matrix.get(0, 1), 12.0, TOLERANCE);
        assertEquals(matrix.get(0, 2), 13.0, TOLERANCE);

        assertEquals(matrix.get(1, 0), 21.0, TOLERANCE);
        assertEquals(matrix.get(1, 1), 22.0, TOLERANCE);
        assertEquals(matrix.get(1, 2), 23.0, TOLERANCE);

        matrix.set(0, 0, 110.0);
        matrix.set(0, 1, 120.0);
        matrix.set(0, 2, 130.0);

        matrix.set(1, 0, 210.0);
        matrix.set(1, 1, 220.0);
        matrix.set(1, 2, 230.0);

        // The original array is not changed...
        assertEquals(testArray[0][0], 11.0, TOLERANCE);
        assertEquals(testArray[0][1], 12.0, TOLERANCE);
        assertEquals(testArray[0][2], 13.0, TOLERANCE);
        assertEquals(testArray[1][0], 21.0, TOLERANCE);
        assertEquals(testArray[1][1], 22.0, TOLERANCE);
        assertEquals(testArray[1][2], 23.0, TOLERANCE);

        assertEquals(matrix.get(0, 0), 110.0, TOLERANCE);
        assertEquals(matrix.get(0, 1), 120.0, TOLERANCE);
        assertEquals(matrix.get(0, 2), 130.0, TOLERANCE);

        assertEquals(matrix.get(1, 0), 210.0, TOLERANCE);
        assertEquals(matrix.get(1, 1), 220.0, TOLERANCE);
        assertEquals(matrix.get(1, 2), 230.0, TOLERANCE);
    }

    @Test
    public void testCopyFrame() {
        assertTrue(D3xMatrix.copyFrame(frame).equalsArray(D3xVectorTest.finalData));

        D3xMatrix actual = D3xMatrix.copyFrame(frame, List.of("row2", "row1"), List.of("col4", "col2"));
        D3xMatrix expected = D3xMatrix.byrow(2, 2, 24.0, 22.0, 14.0, 12.0);

        assertTrue(actual.equalsMatrix(expected));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCopyFrameInvalid1() {
        D3xMatrix.copyFrame(frame, List.of("col1", "row1"), List.of("col1", "col2"));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCopyColumnInvalid2() {
        D3xMatrix.copyFrame(frame, List.of("row1", "row2"), List.of("col3", "row1"));
    }

    @Test
    public void testCopyOfSelf() {
        D3xMatrix orig = D3xMatrix.copyOf(testArray);
        D3xMatrix copy = orig.copy();

        assertEquals(copy.nrow(), 2);
        assertEquals(copy.ncol(), 3);
        assertEquals(copy.size(), 6);

        assertEquals(copy.get(0, 0), 11.0, TOLERANCE);
        assertEquals(copy.get(0, 1), 12.0, TOLERANCE);
        assertEquals(copy.get(0, 2), 13.0, TOLERANCE);

        assertEquals(copy.get(1, 0), 21.0, TOLERANCE);
        assertEquals(copy.get(1, 1), 22.0, TOLERANCE);
        assertEquals(copy.get(1, 2), 23.0, TOLERANCE);

        copy.set(0, 0, 110.0);
        copy.set(0, 1, 120.0);
        copy.set(0, 2, 130.0);

        copy.set(1, 0, 210.0);
        copy.set(1, 1, 220.0);
        copy.set(1, 2, 230.0);

        // The original array and matrix are not changed...
        assertEquals(testArray[0][0], 11.0, TOLERANCE);
        assertEquals(testArray[0][1], 12.0, TOLERANCE);
        assertEquals(testArray[0][2], 13.0, TOLERANCE);
        assertEquals(testArray[1][0], 21.0, TOLERANCE);
        assertEquals(testArray[1][1], 22.0, TOLERANCE);
        assertEquals(testArray[1][2], 23.0, TOLERANCE);

        assertEquals(orig.get(0, 0), 11.0, TOLERANCE);
        assertEquals(orig.get(0, 1), 12.0, TOLERANCE);
        assertEquals(orig.get(0, 2), 13.0, TOLERANCE);

        assertEquals(orig.get(1, 0), 21.0, TOLERANCE);
        assertEquals(orig.get(1, 1), 22.0, TOLERANCE);
        assertEquals(orig.get(1, 2), 23.0, TOLERANCE);

        assertEquals(copy.get(0, 0), 110.0, TOLERANCE);
        assertEquals(copy.get(0, 1), 120.0, TOLERANCE);
        assertEquals(copy.get(0, 2), 130.0, TOLERANCE);

        assertEquals(copy.get(1, 0), 210.0, TOLERANCE);
        assertEquals(copy.get(1, 1), 220.0, TOLERANCE);
        assertEquals(copy.get(1, 2), 230.0, TOLERANCE);
    }

    @Test
    public void testDense() {
        D3xMatrix matrix = D3xMatrix.dense(2, 3);
        assertEquals(matrix.nrow(), 2);
        assertEquals(matrix.ncol(), 3);
        assertEquals(matrix.size(), 6);

        assertEquals(matrix.get(0, 0), 0.0, TOLERANCE);
        assertEquals(matrix.get(0, 1), 0.0, TOLERANCE);
        assertEquals(matrix.get(0, 2), 0.0, TOLERANCE);

        assertEquals(matrix.get(1, 0), 0.0, TOLERANCE);
        assertEquals(matrix.get(1, 1), 0.0, TOLERANCE);
        assertEquals(matrix.get(1, 2), 0.0, TOLERANCE);

        matrix.set(0, 0, 110.0);
        matrix.set(0, 1, 120.0);
        matrix.set(0, 2, 130.0);

        matrix.set(1, 0, 210.0);
        matrix.set(1, 1, 220.0);
        matrix.set(1, 2, 230.0);

        assertEquals(matrix.get(0, 0), 110.0, TOLERANCE);
        assertEquals(matrix.get(0, 1), 120.0, TOLERANCE);
        assertEquals(matrix.get(0, 2), 130.0, TOLERANCE);

        assertEquals(matrix.get(1, 0), 210.0, TOLERANCE);
        assertEquals(matrix.get(1, 1), 220.0, TOLERANCE);
        assertEquals(matrix.get(1, 2), 230.0, TOLERANCE);
    }

    @Test
    public void testSparse() {
        D3xMatrix matrix = D3xMatrix.sparse(100, 100);
        assertEquals(matrix.nrow(), 100);
        assertEquals(matrix.ncol(), 100);
        assertEquals(matrix.size(), 10000);

        matrix.set(0, 0, 110.0);
        matrix.set(0, 1, 120.0);
        matrix.set(0, 2, 130.0);

        matrix.set(1, 0, 210.0);
        matrix.set(1, 1, 220.0);
        matrix.set(1, 2, 230.0);

        assertEquals(matrix.get(0, 0), 110.0, TOLERANCE);
        assertEquals(matrix.get(0, 1), 120.0, TOLERANCE);
        assertEquals(matrix.get(0, 2), 130.0, TOLERANCE);

        assertEquals(matrix.get(1, 0), 210.0, TOLERANCE);
        assertEquals(matrix.get(1, 1), 220.0, TOLERANCE);
        assertEquals(matrix.get(1, 2), 230.0, TOLERANCE);
    }

    @Test
    public void testSquareBlock() {
        D3xMatrix A = D3xMatrix.byrow(3, 3,
                11.0, 12.0, 13.0,
                21.0, 22.0, 23.0,
                31.0, 32.0, 33.0);

        D3xMatrix B = D3xMatrix.byrow(2, 2,
                44.0, 45.0,
                54.0, 55.0);

        D3xMatrix C = D3xMatrix.byrow(2, 3,
                41.0, 42.0, 43.0,
                51.0, 52.0, 53.0);

        D3xMatrix expected = D3xMatrix.byrow(5, 5,
                11.0, 12.0, 13.0, 41.0, 51.0,
                21.0, 22.0, 23.0, 42.0, 52.0,
                31.0, 32.0, 33.0, 43.0, 53.0,
                41.0, 42.0, 43.0, 44.0, 45.0,
                51.0, 52.0, 53.0, 54.0, 55.0);

        D3xMatrix block = D3xMatrix.squareBlock(A, B, C);
        assertTrue(block.equalsMatrix(expected));
    }

    @Test
    public void testTimesMatrix() {
        double[][] arrayA = new double[][] {
                {  1.0,  0.0, -2.0,  5.0 },
                { -3.0,  4.0,  8.0, -1.0 },
                {  2.0,  7.0, -1.0,  9.0 }
        };

        double[][] arrayB = new double[][] {
                { -3.0,  0.0 },
                {  1.0,  3.0 },
                { -4.0,  1.0 },
                {  8.0, -6.0 }
        };

        double[][] arrayAB = new double[][] {
                {  45.0, -32.0 },
                { -27.0,  26.0 },
                {  77.0, -34.0 }
        };

        D3xMatrix A = D3xMatrix.wrap(arrayA);
        D3xMatrix B = D3xMatrix.wrap(arrayB);

        assertTrue(A.times(B).equalsArray(arrayAB));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testTimesMatrixIncongruent() {
        D3xMatrix.dense(4, 5).times(D3xMatrix.dense(4, 5));
    }

    @Test
    public void testTimesVector() {
        double[][] arrayA = new double[][] {
                {  1.0,  0.0, -2.0,  5.0 },
                { -3.0,  4.0,  8.0, -1.0 },
                {  2.0,  7.0, -1.0,  9.0 }
        };

        double[] arrayX = new double[] { 1.0, 2.0, 3.0, 4.0 };
        double[] arrayAX = new double[] { 15.0, 25.0, 49.0 };

        D3xMatrix A = D3xMatrix.wrap(arrayA);
        D3xVector x = D3xVector.wrap(arrayX);

        assertTrue(A.times(x).equalsArray(arrayAX));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testTimesVectorIncongruent() {
        D3xMatrix.dense(4, 5).times(D3xVector.dense(4));
    }

    @Test
    public void testTranspose() {
        double[][] arrayA = new double[][] {
                {  1.0,  0.0, -2.0,  5.0 },
                { -3.0,  4.0,  8.0, -1.0 },
                {  2.0,  7.0, -1.0,  9.0 }
        };

        double[][] arrayAT = new double[][] {
                {  1.0, -3.0,  2.0 },
                {  0.0,  4.0,  7.0 },
                { -2.0,  8.0, -1.0 },
                {  5.0, -1.0,  9.0 }
        };

        assertTrue(D3xMatrix.wrap(arrayA).transpose().equalsArray(arrayAT));
    }

    @Test
    public void testWrap() {
        double[][] wrapped = new double[][]{
                {11.0, 12.0, 13.0},
                {21.0, 22.0, 23.0}
        };

        D3xMatrix wrapper = D3xMatrix.wrap(wrapped);
        assertEquals(wrapper.nrow(), 2);
        assertEquals(wrapper.ncol(), 3);
        assertEquals(wrapper.size(), 6);

        assertEquals(wrapper.get(0, 0), 11.0, TOLERANCE);
        assertEquals(wrapper.get(0, 1), 12.0, TOLERANCE);
        assertEquals(wrapper.get(0, 2), 13.0, TOLERANCE);

        assertEquals(wrapper.get(1, 0), 21.0, TOLERANCE);
        assertEquals(wrapper.get(1, 1), 22.0, TOLERANCE);
        assertEquals(wrapper.get(1, 2), 23.0, TOLERANCE);

        wrapper.set(0, 0, 110.0);
        wrapper.set(0, 1, 120.0);
        wrapper.set(0, 2, 130.0);

        wrapper.set(1, 0, 210.0);
        wrapper.set(1, 1, 220.0);
        wrapper.set(1, 2, 230.0);

        // The original array is also changed...
        assertEquals(wrapped[0][0], 110.0, TOLERANCE);
        assertEquals(wrapped[0][1], 120.0, TOLERANCE);
        assertEquals(wrapped[0][2], 130.0, TOLERANCE);
        assertEquals(wrapped[1][0], 210.0, TOLERANCE);
        assertEquals(wrapped[1][1], 220.0, TOLERANCE);
        assertEquals(wrapped[1][2], 230.0, TOLERANCE);

        assertEquals(wrapper.get(0, 0), 110.0, TOLERANCE);
        assertEquals(wrapper.get(0, 1), 120.0, TOLERANCE);
        assertEquals(wrapper.get(0, 2), 130.0, TOLERANCE);

        assertEquals(wrapper.get(1, 0), 210.0, TOLERANCE);
        assertEquals(wrapper.get(1, 1), 220.0, TOLERANCE);
        assertEquals(wrapper.get(1, 2), 230.0, TOLERANCE);
    }
}
