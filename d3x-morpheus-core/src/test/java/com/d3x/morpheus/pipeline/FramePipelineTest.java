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
package com.d3x.morpheus.pipeline;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAxis;
import com.d3x.morpheus.util.DoubleComparator;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Scott Shaffer
 */
public class FramePipelineTest {
    private static DataFrame<String, String> makeFrame() {
        var frame = DataFrame.ofDoubles(List.of("R1", "R2"), List.of("C1", "C2", "C3"));

        frame.setDoubleAt(0, 0, 1.0);
        frame.setDoubleAt(0, 1, 2.0);
        frame.setDoubleAt(0, 2, 3.0);
        frame.setDoubleAt(1, 0, 10.0);
        frame.setDoubleAt(1, 1, 20.0);
        frame.setDoubleAt(1, 2, 30.0);

        return frame;
    }

    @Test
    public void testByRow() {
        var frame = makeFrame();
        var pipeline = FramePipeline.byRow(DataPipeline.demean);

        pipeline.apply(frame);

        assertEquals(frame.listRowKeys(), List.of("R1", "R2"));
        assertEquals(frame.listColumnKeys(), List.of("C1", "C2", "C3"));

        assertTrue(DoubleComparator.DEFAULT.equals(
                frame.getDoubleMatrix(),
                new double[][] {
                        {  -1.0, 0.0,  1.0 },
                        { -10.0, 0.0, 10.0 }
                }));
    }

    @Test
    public void testByColumn() {
        var frame = makeFrame();
        var pipeline = FramePipeline.byColumn(DataPipeline.demean);

        pipeline.apply(frame);

        assertEquals(frame.listRowKeys(), List.of("R1", "R2"));
        assertEquals(frame.listColumnKeys(), List.of("C1", "C2", "C3"));

        assertTrue(DoubleComparator.DEFAULT.equals(
                frame.getDoubleMatrix(),
                new double[][] {
                        { -4.5, -9.0, -13.5 },
                        {  4.5,  9.0,  13.5 }
                }));
    }

    @Test
    public void testParse() {
        var string1 = "ROWS:demean()";
        var string2 = "COLS:standardize()";

        var pipeline1 = FramePipeline.parse(string1);
        var pipeline2 = FramePipeline.parse(string2);

        assertEquals(DataFrameAxis.Type.ROWS, pipeline1.getAxis());
        assertEquals(DataFrameAxis.Type.COLS, pipeline2.getAxis());

        assertEquals(pipeline1.encode(), string1);
        assertEquals(pipeline2.encode(), string2);
    }
}
