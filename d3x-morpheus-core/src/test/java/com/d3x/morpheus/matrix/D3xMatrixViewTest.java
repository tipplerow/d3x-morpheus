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

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.vector.D3xVectorTest;

import com.d3x.morpheus.vector.D3xVectorView;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class D3xMatrixViewTest {
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testAnyAll() {
        D3xMatrixView view1 = D3xMatrixView.of(
                new double[][] {
                        { 1.0, 2.0, 3.0 },
                        { 4.0, 5.0, 6.0 }
                });

        D3xMatrixView view2 = D3xMatrixView.of(
                new double[][] {
                        { 1.0, 2.0, Double.NaN },
                        { 4.0, 5.0, 6.0 }
                });

        assertTrue(view1.all(Double::isFinite));
        assertTrue(view1.all(x -> x > 0.0));
        assertFalse(view1.all(x -> x > 1.1));
        assertFalse(view2.all(Double::isFinite));

        assertFalse(view1.any(Double::isNaN));
        assertFalse(view1.any(x -> x < 0.0));
        assertTrue(view1.any(x -> x < 1.1));
        assertTrue(view2.any(Double::isNaN));
    }

    @Test
    public void testFrame() {
        DataFrame<String, String> frame = D3xVectorTest.newFrame();
        D3xMatrixView view = D3xMatrixView.of(frame);

        assertEquals(view.get(0, 0), 11.0, TOLERANCE);
        assertEquals(view.get(0, 1), 12.0, TOLERANCE);
        assertEquals(view.get(0, 2), 13.0, TOLERANCE);
        assertEquals(view.get(0, 3), 14.0, TOLERANCE);

        assertEquals(view.get(1, 0), 21.0, TOLERANCE);
        assertEquals(view.get(1, 1), 22.0, TOLERANCE);
        assertEquals(view.get(1, 2), 23.0, TOLERANCE);
        assertEquals(view.get(1, 3), 24.0, TOLERANCE);

        assertEquals(view.get(2, 0), 31.0, TOLERANCE);
        assertEquals(view.get(2, 1), 32.0, TOLERANCE);
        assertEquals(view.get(2, 2), 33.0, TOLERANCE);
        assertEquals(view.get(2, 3), 34.0, TOLERANCE);

        // The view is updated...
        frame.setDoubleAt(1, 2, -21.0);
        assertEquals(view.get(1, 2), -21.0, TOLERANCE);
    }

    @Test
    public void testArray() {
        double[][] viewed = new double[][] {
                { 11.0, 12.0, 13.0 },
                { 21.0, 22.0, 23.0 }
        };

        D3xMatrixView viewer = D3xMatrixView.of(viewed);
        assertEquals(viewer.nrow(), 2);
        assertEquals(viewer.ncol(), 3);
        assertEquals(viewer.size(), 6);

        assertEquals(viewer.get(0, 0), 11.0, TOLERANCE);
        assertEquals(viewer.get(0, 1), 12.0, TOLERANCE);
        assertEquals(viewer.get(0, 2), 13.0, TOLERANCE);

        assertEquals(viewer.get(1, 0), 21.0, TOLERANCE);
        assertEquals(viewer.get(1, 1), 22.0, TOLERANCE);
        assertEquals(viewer.get(1, 2), 23.0, TOLERANCE);

        // The view is updated...
        viewed[0][0] *= 2.0;
        viewed[0][1] *= 2.0;
        viewed[0][2] *= 2.0;
        viewed[1][0] *= 3.0;
        viewed[1][1] *= 3.0;
        viewed[1][2] *= 3.0;

        assertEquals(viewer.get(0, 0), 22.0, TOLERANCE);
        assertEquals(viewer.get(0, 1), 24.0, TOLERANCE);
        assertEquals(viewer.get(0, 2), 26.0, TOLERANCE);

        assertEquals(viewer.get(1, 0), 63.0, TOLERANCE);
        assertEquals(viewer.get(1, 1), 66.0, TOLERANCE);
        assertEquals(viewer.get(1, 2), 69.0, TOLERANCE);
    }

    @Test
    public void testIsDiagonal() {
        double[][] array1 = new double[][] {
                { 1.0, 0.0, 0.0 },
                { 0.0, 4.0, 0.0 }
        };

        double[][] array2 = new double[][] {
                { 1.0, 0.0 },
                { 0.0, 4.0 },
                { 0.0, 0.0 }
        };

        double[][] array3 = new double[][] {
                { 1.0, 0.0, 0.0 },
                { 0.0, 4.0, 0.0 },
                { 0.0, 0.0, 9.0 }
        };

        D3xMatrixView matrix1 = D3xMatrixView.of(array1);
        D3xMatrixView matrix2 = D3xMatrixView.of(array2);
        D3xMatrixView matrix3 = D3xMatrixView.of(array3);

        assertFalse(matrix1.isDiagonal());
        assertFalse(matrix2.isDiagonal());
        assertTrue(matrix3.isDiagonal());

        array3[1][2] = 0.1;

        assertFalse(matrix3.isDiagonal());
    }

    @Test
    public void testIsSymmetric() {
        double[][] array1 = new double[][] {
                { 10.0, 20.0, 30.0 },
                { 20.0, 40.0, 60.0 }
        };

        double[][] array2 = new double[][] {
                { 10.0, 20.0, 30.0 },
                { 20.0, 40.0, 60.0 },
                { 30.0, 60.0, 90.0 }
        };

        D3xMatrixView matrix1 = D3xMatrixView.of(array1);
        D3xMatrixView matrix2 = D3xMatrixView.of(array2);

        assertFalse(matrix1.isSquare());
        assertTrue(matrix2.isSquare());

        assertFalse(matrix1.isSymmetric());
        assertTrue(matrix2.isSymmetric());

        array2[0][1] = 88.8;

        assertTrue(matrix2.isSquare());
        assertFalse(matrix2.isSymmetric());
    }

    @Test
    public void testColumnView() {
        var matrix = D3xMatrix.byrow(5, 3,
                11.0, 12.0, 13.0,
                21.0, 22.0, 23.0,
                31.0, 32.0, 33.0,
                41.0, 42.0, 43.0,
                51.0, 52.0, 53.0);

        assertEquals(matrix.column(0), D3xVectorView.of(11.0, 21.0, 31.0, 41.0, 51.0));
        assertEquals(matrix.column(1, 2, 3), D3xVectorView.of(32.0, 42.0, 52.0));
        assertEquals(matrix.column(2, 1, 2), D3xVectorView.of(23.0, 33.0));
    }

    @Test
    public void testRowView() {
        var matrix = D3xMatrix.byrow(3, 5,
                11.0, 12.0, 13.0, 14.0, 15.0,
                21.0, 22.0, 23.0, 24.0, 25.0,
                31.0, 32.0, 33.0, 34.0, 35.0);

        assertEquals(matrix.row(0), D3xVectorView.of(11.0, 12.0, 13.0, 14.0, 15.0));
        assertEquals(matrix.row(1, 2, 3), D3xVectorView.of(23.0, 24.0, 25.0));
        assertEquals(matrix.row(2, 1, 2), D3xVectorView.of(32.0, 33.0));
    }
}
