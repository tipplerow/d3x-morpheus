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
import org.yaml.snakeyaml.parser.ParserImpl;

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
        assertTrue(actual.apply(Integer.class, series).equalsSeries(expected.apply(Integer.class, series)));

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
    public void testLog() {
        assertScanned("log()", DataPipeline.log);
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
    public void testTrim() {
        assertScanned("trim(0.05)", DataPipeline.trim(0.05));
    }
}
