/*
 * Copyright 2018-2023, Talos Trading - All Rights Reserved
 *
 * Licensed under a proprietary end-user agreement issued by D3X Systems.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.d3xsystems.com/static/eula/quanthub-eula.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.util;

import com.d3x.morpheus.numerictests.NumericTestBase;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Scott Shaffer
 */
public class PointXYTest extends NumericTestBase {
    private final PointXY p14 = PointXY.of(1, 4);
    private final PointXY p23 = PointXY.of(2, 3);
    private final PointXY p32 = PointXY.of(3, 2);
    private final PointXY p41 = PointXY.of(4, 1);

    @Test
    public void testArray() {
        var points = List.of(p14, p23, p32, p41);
        Assert.assertEquals(PointXY.toArrayX(points), new double[] { 1, 2, 3, 4 });
        Assert.assertEquals(PointXY.toArrayY(points), new double[] { 4, 3, 2, 1 });
    }

    @Test
    public void testSort() {
        var points = new ArrayList<PointXY>();
        points.add(p23);
        points.add(p14);
        points.add(p41);
        points.add(p32);

        PointXY.sortX(points);
        Assert.assertEquals(points, List.of(p14, p23, p32, p41));

        PointXY.sortY(points);
        Assert.assertEquals(points, List.of(p41, p32, p23, p14));
    }
}
