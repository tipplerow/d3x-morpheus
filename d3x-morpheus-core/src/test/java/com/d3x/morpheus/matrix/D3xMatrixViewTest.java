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

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class D3xMatrixViewTest {
    private static final double TOLERANCE = 1.0E-12;

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
}
