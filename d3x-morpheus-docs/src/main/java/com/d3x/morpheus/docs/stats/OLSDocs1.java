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
import java.io.File;
import java.util.Optional;

import org.testng.annotations.Test;

import static com.d3x.morpheus.util.Asserts.assertEquals;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameLeastSquares.Field;
import com.d3x.morpheus.viz.chart.Chart;
import com.d3x.morpheus.viz.chart.ChartShape;

/**
 * Code for OLS related documentation
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class OLSDocs1 {

    /**
     * Returns a DataFrame of motor vehicles features
     * @return  the frame of motor vehicle features
     */
    static DataFrame<Integer,String> loadCarDataset() {
        return DataFrame.read().csv(options -> {
            options.setResource("https://www.d3xsystems.com/public/data/samples/cars93.csv");
            options.setExcludeColumnIndexes(0);
        });
    }


    @Test()
    public void scatterPlot() throws Exception {
        var frame = loadCarDataset();
        var y = "Horsepower";
        var x = "EngineSize";
        var xy = frame.cols().select(y, x);
        Chart.create().withScatterPlot(xy, false, x, chart -> {
            chart.title().withText(y + " vs " + x);
            chart.plot().style(y).withColor(Color.RED);
            chart.plot().style(y).withPointsVisible(true).withPointShape(ChartShape.DIAMOND);
            chart.plot().axes().domain().label().withText(x);
            chart.plot().axes().domain().format().withPattern("0.00;-0.00");
            chart.plot().axes().range(0).label().withText(y);
            chart.plot().axes().range(0).format().withPattern("0;-0");
            chart.writerPng(new File("./docs/images/ols/data-frame-ols1.png"), 845, 450, true);
            chart.show(845, 450);
        });

        Thread.currentThread().join();
    }



    @Test()
    public void ols2() throws Exception {
        var frame = loadCarDataset();
        var regressand = "Horsepower";
        var regressor = "EngineSize";
        frame.regress().ols(regressand, regressor, true, model -> {
            assert (model.getRegressand().equals(regressand));
            assert (model.getRegressors().size() == 1);
            assertEquals(model.getRSquared(), 0.5359992996664269, 0.00001);
            assertEquals(model.getRSquaredAdj(), 0.5309003908715525, 0.000001);
            assertEquals(model.getStdError(), 35.87167658782274, 0.00001);
            assertEquals(model.getFValue(), 105.120393642, 0.00001);
            assertEquals(model.getFValueProbability(), 0, 0.00001);
            assertEquals(model.getBetaValue("EngineSize", Field.PARAMETER), 36.96327914, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.STD_ERROR), 3.60518041, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.T_STAT), 10.25282369, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.P_VALUE), 0.0000, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.CI_LOWER), 29.80203113, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.CI_UPPER), 44.12452714, 0.0000001);
            assertEquals(model.getInterceptValue(Field.PARAMETER), 45.21946716, 0.0000001);
            assertEquals(model.getInterceptValue(Field.STD_ERROR), 10.31194906, 0.0000001);
            assertEquals(model.getInterceptValue(Field.T_STAT), 4.3851523, 0.0000001);
            assertEquals(model.getInterceptValue(Field.P_VALUE), 0.00003107, 0.0000001);
            assertEquals(model.getInterceptValue(Field.CI_LOWER), 24.73604714, 0.0000001);
            assertEquals(model.getInterceptValue(Field.CI_UPPER), 65.70288719, 0.0000001);
            System.out.println(model);
            return Optional.of(model);
        });
    }



    @Test()
    public void regressPlot() throws Exception {
        var frame = loadCarDataset();
        var regressand = "Horsepower";
        var regressor = "EngineSize";
        var xy = frame.cols().select(regressand, regressor);
        Chart.create().withScatterPlot(xy, false, regressor, chart -> {
            chart.title().withFont(new Font("Verdana", Font.BOLD, 16));
            chart.title().withText(regressand + " regressed on " + regressor);
            chart.subtitle().withText("Single Variable Linear Regression");
            chart.plot().style(regressand).withColor(Color.RED);
            chart.plot().trend(regressand).withColor(Color.BLACK);
            chart.plot().axes().domain().label().withText(regressor);
            chart.plot().axes().domain().format().withPattern("0.00;-0.00");
            chart.plot().axes().range(0).label().withText(regressand);
            chart.plot().axes().range(0).format().withPattern("0;-0");
            chart.writerPng(new File("./docs/images/ols/data-frame-ols2.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }



    @Test()
    public void ols3() throws Exception {
        var frame = loadCarDataset();
        var regressand = "Horsepower";
        var regressor = "EngineSize";
        frame.regress().ols(regressand, regressor, false, model -> {
            assert (model.getRegressand().equals(regressand));
            assert (model.getRegressors().size() == 1);
            assertEquals(model.getRSquared(), 0.934821940829, 0.00001);
            assertEquals(model.getRSquaredAdj(), 0.9341134836, 0.000001);
            assertEquals(model.getStdError(), 39.26510834, 0.00001);
            assertEquals(model.getFValue(), 1319.517942852, 0.00001);
            assertEquals(model.getFValueProbability(), 0, 0.00001);
            assertEquals(model.getBetaValue("EngineSize", Field.PARAMETER), 51.708176166, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.STD_ERROR), 1.4234806556, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.T_STAT), 36.32516955, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.P_VALUE), 5.957E-56, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.CI_LOWER), 48.880606712, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.CI_UPPER), 54.53574562, 0.0000001);
            System.out.println(model);
            return Optional.of(model);
        });
    }




}
