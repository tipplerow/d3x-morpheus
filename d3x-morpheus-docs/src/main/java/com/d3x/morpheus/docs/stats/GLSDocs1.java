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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.testng.annotations.Test;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameLeastSquares;
import com.d3x.morpheus.frame.DataFrameLeastSquares.Field;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.stats.StatType;
import com.d3x.morpheus.viz.chart.Chart;

/**
 * Code examples for documentation on Generalized Least Squares
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class GLSDocs1 {


    /**
     * Returns a DataFrame of X & Y values based on the coefficients provided.
     * Noise is added to the dependent variable based on an AR(1) process
     * @param alpha     the intercept term for population process
     * @param beta      the slope term for population process
     * @param rho       the AR(1) coefficient used to generate serially correlated errors
     * @param sigma     the std deviation of the normal distribution for noise
     * @param seed      if true, initialize with a fixed seed for reproducible results
     * @return          the frame of XY values with serially correlated residuals
     */
    private DataFrame<Integer,String> sample(double alpha, double beta, double rho, double sigma, int n, boolean seed) {
        final double startX = 1d;
        final double stepX = 0.5d;
        final RandomGenerator rand = seed ? new Well19937c(1234565) : new Well19937c();
        final RealDistribution noise = new NormalDistribution(rand, 0, sigma);
        final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> startX + v.index() * stepX);
        final Array<Integer> rowKeys = Range.of(0, n).toArray();
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("X", xValues);
            columns.add("Y", Array.of(Double.class, n).applyDoubles(v -> {
                final double xValue = xValues.getDouble(v.index());
                final double yFitted = alpha + beta * xValue;
                if (v.index() == 0) return yFitted + noise.sample();
                else {
                    final double priorX = xValues.getDouble(v.index()-1);
                    final double priorY = v.array().getDouble(v.index()-1);
                    final double priorError = priorY - (alpha + beta * priorX);
                    final double error = rho * priorError + noise.sample();
                    return yFitted + error;
                }
            }));
        });
    }


    @Test()
    public void plotScatter() throws Exception {
        final int n = 100;
        final double rho = 0.5d;
        final double beta = 4d;
        final double alpha = 20d;
        final double sigma = 10d;
        final DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, true);
        Chart.create().withScatterPlot(frame, false, "X", chart -> {
            chart.title().withText("Regression with Serial Correlation");
            chart.subtitle().withText("Artificially generated dataset with AR(1) errors");
            chart.title().withFont(new Font("Arial", Font.BOLD, 16));
            chart.plot().axes().domain().label().withText("X");
            chart.plot().axes().range(0).label().withText("Y");
            chart.plot().style("Y").withColor(Color.RED).withPointsVisible(true);
            chart.plot().trend("Y");
            chart.show();
        });
        Thread.currentThread().join();
    }



    @Test()
    public void acfPlot() throws Exception {
        final int n = 200;
        final double rho = 0.5d;
        final double beta = 4d;
        final double alpha = 20d;
        final double sigma = 10d;
        final DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, true);
        frame.regress().ols("Y", "X", true, model -> {
            Chart.create().withAcf(model, frame.rowCount()/2, 0.05d, chart -> {
                final double rhoHat = model.getResiduals().colAt(0).stats().autocorr(1);
                chart.title().withText("Residual Autocorrelation Plot");
                chart.subtitle().withText(String.format("Autocorrelation Lag 1 = %.3f", rhoHat));
                chart.writerPng(new File("./docs/images/gls/gls-acf.png"), 800, 300, true);
                chart.show(800, 300);
            });
            return Optional.empty();
        });
        Thread.currentThread().join();
    }


    @Test()
    public void plotScatterMany() throws Exception {
        final int n = 100;
        final double rho = 0.5d;
        final double beta = 4d;
        final double alpha = 20d;
        final double sigma = 10d;
        Chart.show(2, IntStream.range(0, 4).mapToObj(i -> {
            DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, false);
            String title = "Sample %s Dataset, Beta: %.2f Alpha: %.2f";
            String subtitle = "Parameter estimates, Beta^: %.3f, Alpha^: %.3f";
            DataFrameLeastSquares<Integer,String> ols = frame.regress().ols("Y", "X", true, Optional::of).get();
            double betaHat = ols.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
            double alphaHat = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
            return Chart.create().withScatterPlot(frame, false, "X", chart -> {
                chart.title().withText(String.format(title, i, beta, alpha));
                chart.title().withFont(new Font("Arial", Font.BOLD, 14));
                chart.subtitle().withText(String.format(subtitle, betaHat, alphaHat));
                chart.plot().style("Y").withColor(Color.RED).withPointsVisible(true);
                chart.plot().trend("Y");
                chart.plot().axes().domain().label().withText("Regressor");
                chart.plot().axes().range(0).label().withText("Regressand");
                chart.writerPng(new File(String.format("./docs/images/gls/gls-sample-%s.png", i)), 500, 400, true);
            });
        }));
        Thread.currentThread().join();
    }



    @Test()
    public void regress() {
        int n = 100;
        double rho = 0.5d;
        double beta = 4d;
        double alpha = 20d;
        double sigma = 10d;
        DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, true);
        frame.regress().ols("Y", "X", true, ols -> {
            System.out.println(ols);
            final double rhoHat = ols.getResiduals().colAt(0).stats().autocorr(1);
            final DataFrame<Integer,Integer> omega = createOmega(n, rhoHat);
            frame.regress().gls("Y", "X", omega, true, gls -> {
                System.out.println(gls);
                return Optional.empty();
            });
            return Optional.empty();
        });
    }


    /**
     * Returns the correlation matrix omega for an AR(1) process
     * @param size      the size for the correlation matrix
     * @param autocorr  the auto correlation coefficient
     * @return          the newly created correlation matrix
     */
    private DataFrame<Integer,Integer> createOmega(int size, double autocorr) {
        final Range<Integer> keys = Range.of(0, size);
        return DataFrame.ofDoubles(keys, keys, v -> {
            return Math.pow(autocorr,  Math.abs(v.rowOrdinal() - v.colOrdinal()));
        });
    }


    @Test()
    public void unbiasedness() throws Exception {
        final int n = 100;
        final double rho = 0.5d;
        final double beta = 4d;
        final double alpha = 20d;
        final double sigma = 20d;
        final int regressionCount = 100000;
        Range<Integer> rows = Range.of(0, regressionCount);
        Array<String> columns = Array.of("Beta", "Alpha");
        DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

        results.rows().parallel().forEach(row -> {
            final DataFrame<Integer,String> frame = sample(alpha, beta, rho, sigma, n, false);
            frame.regress().ols("Y", "X", true, ols -> {
                final double rhoHat = ols.getResiduals().colAt(0).stats().autocorr(1);
                final DataFrame<Integer,Integer> omega = createOmega(frame.rowCount(), rhoHat);
                frame.regress().gls("Y", "X", omega, true, model -> {
                    final double alphaHat = model.getInterceptValue(Field.PARAMETER);
                    final double betaHat = model.getBetaValue("X", Field.PARAMETER);
                    row.setDouble("Alpha", alphaHat);
                    row.setDouble("Beta", betaHat);
                    return Optional.empty();
                });
                return Optional.empty();
            });
        });

        Array.of("Beta", "Alpha").forEach(coeff -> {
            final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
            Chart.create().withHistPlot(coeffResults, 250, chart -> {
                String title = "%s Histogram of %s GLS regressions, Rho = %.3f";
                String subtitle = "%s estimate unbiasedness, Actual: %.2f, Mean: %.2f, Variance: %.2f";
                double actual = coeff.equals("Beta") ? beta : alpha;
                double estimate = coeffResults.col(coeff).stats().mean();
                double variance = coeffResults.col(coeff).stats().variance();
                Color color = coeff.equals("Beta") ? new Color(255, 100, 100) : new Color(102, 204, 255);
                chart.plot().style(coeff).withColor(color);
                chart.plot().axes().domain().label().withText(coeff + " Estimate");
                chart.title().withText(String.format(title, coeff, regressionCount, rho));
                chart.subtitle().withText(String.format(subtitle, coeff, actual, estimate, variance));
                chart.writerPng(new File(String.format("./docs/images/gls/gls-%s-unbiasedness.png", coeff.toLowerCase())), 700, 400, true);
                chart.show(700, 400);
            });
        });

        Thread.currentThread().join();
    }


    @Test()
    public void efficiency() throws Exception {
        final int n = 20;
        final double rho = 0.8d;
        final double beta = 4d;
        final double alpha = 20d;
        final double sigma = 20d;
        final int regressionCount = 100000;
        Range<Integer> rows = Range.of(0, regressionCount);
        Array<String> columns = Array.of("Beta(OLS)", "Alpha(OLS)", "Beta(GLS)", "Alpha(GLS)");
        DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

        AtomicInteger count = new AtomicInteger();

        results.rows().parallel().forEach(row -> {
            DataFrame<Integer,String> data = sample(alpha, beta, rho, sigma, n, false);
            if (count.incrementAndGet() % 1000 == 0) System.out.printf("\nCompleted %s regressions out of %s", count.get(), regressionCount);
            data.regress().ols("Y", "X", true, ols -> {
                final double alphaHatOls = ols.getInterceptValue(Field.PARAMETER);
                final double betaHatOls = ols.getBetaValue("X", Field.PARAMETER);
                row.setDouble("Alpha(OLS)", alphaHatOls);
                row.setDouble("Beta(OLS)", betaHatOls);
                final double rhoHat = ols.getResiduals().colAt(0).stats().autocorr(1);
                final DataFrame<Integer,Integer> omega = createOmega(n, rhoHat);
                data.regress().gls("Y", "X", omega, true, gls -> {
                    double alphaHat = gls.getInterceptValue(Field.PARAMETER);
                    double betaHat = gls.getBetaValue("X", Field.PARAMETER);
                    row.setDouble("Alpha(GLS)", alphaHat);
                    row.setDouble("Beta(GLS)", betaHat);
                    return Optional.empty();
                });
                return Optional.empty();
            });
        });

        Array.of("Alpha", "Beta").forEach(coeff -> {
            final String olsKey = coeff + "(OLS)";
            final String glsKey = coeff + "(GLS)";
            final DataFrame<Integer,String> data = results.cols().select(olsKey, glsKey);
            Chart.create().withHistPlot(data, 350, chart -> {
                double meanOls = results.col(olsKey).stats().mean();
                double stdOls = results.col(olsKey).stats().stdDev();
                double meanWls = results.col(glsKey).stats().mean();
                double stdWls = results.col(glsKey).stats().stdDev();
                double coeffAct = coeff.equals("Alpha") ? alpha : beta;
                String title = "%s Histogram from %s OLS & GLS Regressions (n=%s)";
                String subtitle = "Actual: %.4f, Mean(OLS): %.4f, Std(OLS): %.4f, Mean(GLS): %.4f, Std(GLS): %.4f";
                chart.title().withText(String.format(title, coeff, regressionCount, n));
                chart.title().withFont(new Font("Arial", Font.BOLD, 15));
                chart.subtitle().withText(String.format(subtitle, coeffAct, meanOls, stdOls, meanWls, stdWls));
                chart.plot().axes().domain().label().withText(coeff + " Estimates");
                chart.legend().on().bottom();
                chart.writerPng(new File(String.format("./docs/images/gls/gls-%s-efficiency.png", coeff.toLowerCase())), 700, 400, true);
                chart.show(700, 400);
            });
        });

        Thread.currentThread().join();
    }



    @Test()
    public void consistency() throws Exception {
        final double beta = 4d;
        final double rho = 0.5d;
        final double alpha = 20d;
        final double sigma = 10d;
        final int regressionCount = 100000;
        final Range<Integer> sampleSizes = Range.of(20, 120, 20);
        final Range<Integer> rows = Range.of(0, regressionCount);
        final DataFrame<Integer,String> results = DataFrame.of(rows, String.class, columns -> {
            sampleSizes.forEach(n -> {
                columns.add(String.format("Beta(n=%s)", n), Double.class);
                columns.add(String.format("Alpha(n=%s)", n), Double.class);
            });
        });

        sampleSizes.forEach(n -> {
            System.out.println("Running " + regressionCount + " regressions for n=" + n);
            final String betaKey = String.format("Beta(n=%s)", n);
            final String alphaKey = String.format("Alpha(n=%s)", n);
            results.rows().parallel().forEach(row -> {
                DataFrame<Integer,String> data = sample(alpha, beta, rho, sigma, n, false);
                DataFrame<Integer,Integer> omega = createOmega(n, rho);
                data.regress().gls("Y", "X", omega, true, model -> {
                    final double alphaHat = model.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
                    final double betaHat = model.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
                    row.setDouble(alphaKey, alphaHat);
                    row.setDouble(betaKey, betaHat);
                    return Optional.empty();
                });
            });
        });

        Array.of("Beta", "Alpha").forEach(coeff -> {
            final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
            Chart.create().withHistPlot(coeffResults, 250, true, chart -> {
                chart.plot().axes().domain().label().withText("Coefficient Estimate");
                chart.title().withText(coeff + " Histograms of " + regressionCount + " Regressions");
                chart.subtitle().withText(coeff + " Estimate distribution as sample size increases");
                chart.legend().on().bottom();
                chart.writerPng(new File(String.format("./docs/images/gls/gls-%s-consistency.png", coeff.toLowerCase())), 700, 400, true);
                chart.show(700, 400);
            });
        });

        Array<DataFrame<String,StatType>> variances = Array.of("Beta", "Alpha").map(value -> {
            final String coefficient = value.getValue();
            final Matcher matcher = Pattern.compile(coefficient + "\\(n=(\\d+)\\)").matcher("");
            return results.cols().select(column -> {
                final String name = column.key();
                return matcher.reset(name).matches();
            }).cols().mapKeys(column -> {
                final String name = column.key();
                if (matcher.reset(name).matches()) return matcher.group(1);
                throw new IllegalArgumentException("Unexpected column name: " + column.key());
            }).cols().stats().variance();
        });

        Chart.show(2, Array.of(
            Chart.create().withBarPlot(variances.getValue(0), false, chart -> {
                chart.title().withText("Beta variance with sample size");
                chart.plot().style(StatType.VARIANCE).withColor(new Color(255, 100, 100));
                chart.plot().axes().range(0).label().withText("Beta Variance");
                chart.plot().axes().domain().label().withText("Sample Size");
                chart.writerPng(new File("./docs/images/gls/gls-beta-variance.png"), 350, 200, true);
            }),
            Chart.create().withBarPlot(variances.getValue(1), false, chart -> {
                chart.title().withText("Alpha variance with sample size");
                chart.plot().style(StatType.VARIANCE).withColor(new Color(102, 204, 255));
                chart.plot().axes().range(0).label().withText("Alpha Variance");
                chart.plot().axes().domain().label().withText("Sample Size");
                chart.writerPng(new File("./docs/images/gls/gls-alpha-variance.png"), 350, 200, true);
            }))
        );

        Thread.currentThread().join();
    }



}
