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

import java.util.Random;

import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.series.DoubleSeriesBuilder;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PipelineScannerTest {
    private final DoubleSeries<Integer> series;
    private static final int SERIES_LENGTH = 1000;

    private PipelineScannerTest() {
        this.series = createSeries();
    }

    private DoubleSeries<Integer> createSeries() {
        Random random = new Random(20210505);
        DoubleSeriesBuilder<Integer> builder = DoubleSeries.builder(Integer.class);

        for (int index = 0; index < SERIES_LENGTH; ++index)
            builder.plusDouble(index, 1.0 + 10.0 * random.nextDouble());

        return builder.build();
    }

    private void assertScanned(String source, DataPipeline expected) {
        DataPipeline actual = PipelineScanner.scan(source);
        var actualSeries = actual.apply(Integer.class, series);
        var expectedSeries = expected.apply(Integer.class, series);
        assertTrue(actualSeries.equalsSeries(expectedSeries));

        // The encoded pipeline might not match exactly to the source string
        // because of the argument formatting: add(3) is encoded as add(3.0)
        DataPipeline decoded = PipelineScanner.scan(expected.encode());
        assertTrue(decoded.apply(Integer.class, series).equalsSeries(expected.apply(Integer.class, series)));
    }

    @Test
    public void testAbs() {
        assertScanned("abs()", DataPipeline.abs);
    }
    
    @Test
    public void testAdd() {
        assertScanned("add(3)", DataPipeline.add(3.0));
        assertScanned("add(1.23)", DataPipeline.add(1.23));
    }

    @Test
    public void testBound() {
        assertScanned("bound(3, 8)", DataPipeline.bound(3.0, 8.0));
        assertScanned("bound(3.45, 7.89)", DataPipeline.bound(3.45, 7.89));
    }

    @Test
    public void testComposite() {
        assertScanned("add(10.0), sqrt()", DataPipeline.composite(DataPipeline.add(10.0), DataPipeline.sqrt));
        assertScanned("sqrt(), add(10.0)", DataPipeline.composite(DataPipeline.sqrt, DataPipeline.add(10.0)));
    }

    @Test
    public void testDivide() {
        assertScanned("divide(5)", DataPipeline.divide(5));
        assertScanned("divide(7.7)", DataPipeline.divide(7.7));
    }

    @Test
    public void testDemean() {
        assertScanned("demean()", DataPipeline.demean);
    }

    @Test
    public void testExp() {
        assertScanned("exp()", DataPipeline.exp);
    }

    @Test
    public void testFlip() {
        assertScanned("flip()", DataPipeline.flip);
    }

    @Test
    public void testHuber() {
        assertScanned("huber(4)", DataPipeline.huber(4.0));
        assertScanned("huber(5.0)", DataPipeline.huber(5.0));
    }

    @Test
    public void testIdentity() {
        assertScanned("identity()", DataPipeline.identity);
    }

    @Test
    public void testInvert() {
        assertScanned("invert()", DataPipeline.invert);
    }

    @Test
    public void testLever() {
        assertScanned("lever(1.0)", DataPipeline.lever(1.0));
        assertScanned("lever(4.5)", DataPipeline.lever(4.5));
    }

    @Test
    public void testList() {
        var pipelines = PipelineScanner.scanList("add(10.0)");
        System.out.println(pipelines);
        assertEquals(pipelines.size(), 1);
        assertEquals(pipelines.get(0).encode(), "add(10.0)");

        pipelines = PipelineScanner.scanList("add(10.0), sqrt(), lever(2.2)");
        assertEquals(pipelines.size(), 3);
        assertEquals(pipelines.get(0).encode(), "add(10.0)");
        assertEquals(pipelines.get(1).encode(), "sqrt()");
        assertEquals(pipelines.get(2).encode(), "lever(2.2)");
    }

    @Test
    public void testLog() {
        assertScanned("log()", DataPipeline.log);
    }

    @Test
    public void testLog1P() {
        assertScanned("log1p()", DataPipeline.log1p);
    }

    @Test
    public void testMultiply() {
        assertScanned("multiply(5)", DataPipeline.multiply(5));
        assertScanned("multiply(7.7)", DataPipeline.multiply(7.7));
    }

    @Test
    public void testNormalize() {
        assertScanned("normalize()", DataPipeline.normalize);
    }

    @Test
    public void testPow() {
        assertScanned("pow(1.23)", DataPipeline.pow(1.23));
    }

    @Test
    public void testRank() {
        assertScanned("rank01()", DataPipeline.rank01);
        assertScanned("rank11()", DataPipeline.rank11);
        assertScanned("rank(-1.0, 1.0)", DataPipeline.rank(-1.0, 1.0));
        assertScanned("rank(0, 100)", DataPipeline.rank(0, 100));
    }

    @Test
    public void testReplaceNaN() {
        assertScanned("replaceNaN(5)", DataPipeline.replaceNaN(5));
        assertScanned("replaceNaN(7.7)", DataPipeline.replaceNaN(7.7));
    }

    @Test
    public void testSign() {
        assertScanned("sign()", DataPipeline.sign);
    }

    @Test
    public void testSqrt() {
        assertScanned("sqrt()", DataPipeline.sqrt);
    }

    @Test
    public void testSquare() {
        assertScanned("square()", DataPipeline.square);
    }

    @Test
    public void testStandardize() {
        assertScanned("standardize()", DataPipeline.standardize);
    }

    @Test
    public void testSubtract() {
        assertScanned("subtract(5)", DataPipeline.subtract(5));
        assertScanned("subtract(7.7)", DataPipeline.subtract(7.7));
    }

    @Test
    public void testTanh() {
        assertScanned("tanh(1.0, 2.0)", DataPipeline.tanh(1.0, 2.0));
    }

    @Test
    public void testTrim() {
        assertScanned("trim(0.05)", DataPipeline.trim(0.05));
    }

    @Test
    public void testTruncate() {
        assertScanned("truncate(2.0, 10.0)", DataPipeline.truncate(2.0, 10.0));
    }

    @Test
    public void testWinsor() {
        assertScanned("winsor(3)", DataPipeline.winsor(3.0));
        assertScanned("winsor(4.0)", DataPipeline.winsor(4.0));
    }
}
