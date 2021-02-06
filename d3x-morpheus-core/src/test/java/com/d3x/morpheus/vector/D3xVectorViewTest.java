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
package com.d3x.morpheus.vector;

import java.util.List;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class D3xVectorViewTest {
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testArrayView() {
        assertEquals(D3xVectorView.of().length(), 0);

        D3xVectorView view = D3xVectorView.of(1.0, 2.0, 3.0);
        assertEquals(view.length(), 3);
        assertEquals(view.get(0), 1.0, TOLERANCE);
        assertEquals(view.get(1), 2.0, TOLERANCE);
        assertEquals(view.get(2), 3.0, TOLERANCE);
    }

    @Test
    public void testListView() {
        assertEquals(D3xVectorView.of(List.of()).length(), 0);

        D3xVectorView view = D3xVectorView.of(List.of(1.0, 2.0, 3.0));
        assertEquals(view.length(), 3);
        assertEquals(view.get(0), 1.0, TOLERANCE);
        assertEquals(view.get(1), 2.0, TOLERANCE);
        assertEquals(view.get(2), 3.0, TOLERANCE);
    }

    @Test
    public void testEqualsView() {
        D3xVectorView view1 = D3xVectorView.of(1.0, 2.0, 3.0);
        D3xVectorView view2 = D3xVectorView.of(List.of(1.0, 2.0, 3.0));
        D3xVectorView view3 = D3xVectorView.of(1.0, 2.0);
        D3xVectorView view4 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0);

        assertTrue(view1.equalsView(view1));
        assertTrue(view1.equalsView(view2));
        assertTrue(view2.equalsView(view1));
        assertFalse(view1.equalsView(view3));
        assertFalse(view1.equalsView(view4));
    }
}

