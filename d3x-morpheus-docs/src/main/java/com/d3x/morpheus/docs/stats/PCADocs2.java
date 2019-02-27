/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.docs.stats;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

import org.apache.commons.math3.linear.RealMatrix;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFramePCA;
import com.d3x.morpheus.frame.DataFramePCA.Field;
import com.d3x.morpheus.jama.Matrix;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.Collect;
import com.d3x.morpheus.viz.chart.Chart;

public class PCADocs2 {


    @Test()
    public void test() {
        var frame = DataFrame.ofImage(getClass().getResource("/poppet.jpg"));
        Stream.of(DataFramePCA.Solver.EVD_COV, DataFramePCA.Solver.SVD).forEach(solver -> {
            frame.transpose().pca().apply(true, solver, model -> {
                model.getEigenValues().out().print();
                model.getEigenVectors().out().print();
                model.getProjection(10).out().print();
                return Optional.empty();
            });
        });
    }


    @Test()
    public void processImage() throws Exception {

        //Load image from classpath
        var url = getClass().getResource("/poppet.jpg");

        //Re-create PCA reduced image while retaining different number of principal components
        Array.of(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 360).forEach(nComp -> {

            //Initialize the **transpose** of image as we need nxp frame where n >= p
            var rgbFrame = DataFrame.ofImage(url).transpose();

            //Create 3 frames from RGB data, one for red, green and blue
            var red = rgbFrame.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
            var green = rgbFrame.mapToDoubles(v -> (v.getInt() >> 8) & 0xFF);
            var blue = rgbFrame.mapToDoubles(v -> v.getInt() & 0xFF);

            //Perform PCA on each color frame, and project using only first N principal components
            Stream.of(red, green, blue).parallel().forEach(color -> {
                color.pca().apply(true, model -> {
                    var projection = model.getProjection(nComp);
                    projection.cap(true).doubles(0, 255);  //cap values between 0 and 255
                    color.update(projection, false, false);
                    return null;
                });
            });

            //Apply reduced RBG values onto the original frame so we don't need to allocate memory
            rgbFrame.applyInts(v -> {
                var i = v.rowOrdinal();
                var j = v.colOrdinal();
                var r = (int)red.getDouble(i,j);
                var g = (int)green.getDouble(i,j);
                var b = (int)blue.getDouble(i,j);
                return ((0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
            });

            //Create reduced image from **transpose** of the DataFrame to get back original orientation
            var width = rgbFrame.rowCount();
            var height = rgbFrame.colCount();
            var transformed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            rgbFrame.forEachValue(v -> {
                var i = v.colOrdinal();
                var j = v.rowOrdinal();
                var rgb = v.getInt();
                transformed.setRGB(j, i, rgb);
            });

            try {
                var outputfile = new File("/Users/witdxav/temp/poppet-" + nComp + ".jpg");
                outputfile.getParentFile().mkdirs();
                ImageIO.write(transformed, "jpg", outputfile);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to record image result", ex);
            }
        });
    }



    @Test()
    public void variancePlot() throws Exception {

        var url = getClass().getResource("/poppet.jpg");
        var rgbFrame = DataFrame.ofImage(url);
        var rowKeys = Range.of(0, rgbFrame.rowCount());

        var result = DataFrame.ofDoubles(rowKeys, Array.of("Red", "Green", "Blue"));
        Collect.<String,DataFrame<Integer,Integer>>asMap(mapping -> {
            mapping.put("Red", rgbFrame.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF));
            mapping.put("Green", rgbFrame.mapToDoubles(v -> (v.getInt() >> 8) & 0xFF));
            mapping.put("Blue", rgbFrame.mapToDoubles(v -> v.getInt() & 0xFF));
        }).forEach((name, color) -> {
            color.transpose().pca().apply(true, model -> {
                var eigenFrame = model.getEigenValues();
                var varPercent = eigenFrame.cols().select(Field.VAR_PERCENT);
                result.update(varPercent.cols().mapKeys(k -> name), false, false);
                return Optional.empty();
            });
        });

        var chartData = result.rows().select(c -> c.ordinal() < 10).copy();
        Chart.create().withBarPlot(chartData.rows().mapKeys(r -> String.valueOf(r.ordinal())), false, chart -> {
            chart.plot().style("Red").withColor(Color.RED);
            chart.plot().style("Green").withColor(Color.GREEN);
            chart.plot().style("Blue").withColor(Color.BLUE);
            chart.plot().axes().range(0).label().withText("Percent of Variance");
            chart.plot().axes().domain().label().withText("Principal Component");
            chart.title().withText("Eigen Spectrum (Percent of Explained Variance)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/pca/poppet-explained-variance.png"), 700, 400, true);
            chart.show();
        });

        Thread.currentThread().join();
    }



    Matrix toJama(DataFrame<?,?> frame) {
        var matrix = new Matrix(frame.rowCount(), frame.colCount());
        frame.forEachValue(v -> matrix.set(v.rowOrdinal(), v.colOrdinal(), v.getDouble()));
        return matrix;
    }


    @Test()
    public void filesizes() throws Exception {
        var home = new File("/Users/witdxav");
        var rowKeys = Range.of(1, 61);
        var frame = DataFrame.ofDoubles(rowKeys, Array.of("Size"), v -> {
            var file = new File(home, "cutedog-" + v.rowKey() + ".jpg");
            return (double)file.length();
        });

        Chart.create().withBarPlot(frame, false, chart -> {
            chart.plot().data().at(0).withLowerDomainInterval(v -> v + 1);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void scores1() {
        var url = getClass().getResource("/poppet.jpg");
        var image = DataFrame.ofImage(url).transpose();
        var red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
        red.pca().apply(true, model -> {
            var scores = model.getScores();
            Assert.assertEquals(scores.rowCount(), 504);
            Assert.assertEquals(scores.colCount(), 360);
            scores.out().print();
            return Optional.empty();
        });
    }


    @Test()
    public void scores2() {
        var url = getClass().getResource("/poppet.jpg");
        var image = DataFrame.ofImage(url).transpose();
        var red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
        red.pca().apply(true, model -> {
            var scores = model.getScores(10);
            Assert.assertEquals(scores.rowCount(), 504);
            Assert.assertEquals(scores.colCount(), 10);
            scores.out().print();
            return Optional.empty();
        });
    }

    @Test()
    public void projection1() {
        var url = getClass().getResource("/poppet.jpg");
        var image = DataFrame.ofImage(url).transpose();
        var red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
        red.pca().apply(true, model -> {
            var projection = model.getProjection(10);
            Assert.assertEquals(projection.rowCount(), 504);
            Assert.assertEquals(projection.colCount(), 360);
            projection.out().print();
            return Optional.empty();
        });
    }


    /**
     * Returns a DataFrame representation of a matrix
     * @param matrix    the matrix reference
     * @return          the DataFrame representation
     */
    DataFrame<Integer,Integer> toDataFrame(RealMatrix matrix) {
        var rowKeys = Range.of(0, matrix.getRowDimension());
        var colKeys = Range.of(0, matrix.getColumnDimension());
        return DataFrame.ofDoubles(rowKeys, colKeys, v -> matrix.getEntry(v.rowOrdinal(), v.colOrdinal()));
    }


    @Test()
    public void shift() {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(255 >> 4);
        System.out.println(0xFF);
    }


}
