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

package com.d3x.morpheus.viz.examples;

import java.awt.*;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.viz.chart.Chart;

public class WorldBank {


    /**
     * Main line
     * @param args
     */
    public static void main(String[] args) {
        final DataFrame<String,String> frame = createDataset();
        frame.out().print();

        Chart.create().htmlMode();

        Chart.create().withBarPlot(frame, false, chart -> {
            chart.title().withText("World Bank GBP Per Capita measured in USD");
            chart.title().withFont(new Font("Arial", Font.PLAIN, 18));
            chart.plot().axes().domain().label().withText("Region");
            chart.plot().axes().domain().label().withFont(new Font("Arial", Font.PLAIN, 16));
            chart.plot().axes().range(0).label().withText("GBP per capita USD");
            chart.plot().axes().domain().label().withFont(new Font("Arial", Font.ITALIC, 16));
            chart.plot().orient().vertical();
            chart.legend().on();
            chart.show();
        });
    }

    /**
     * Loads the ATP dataset for 2013, finds top 10 players, and computes various stats for those players
     * @return      the dataset to plot
     */
    private static DataFrame<String,String> createDataset() {
        final Array<String> years = Array.of("1980", "1985", "1990", "1995", "2000", "2005", "2010");
        final DataFrame<String,String> frame = DataFrame.read().csv(options -> {
            options.setResource("/worldbank/gdp_per_capita.csv");
            options.setRowKeyParser(String.class, values -> values[0]);
        });
        return frame.cols().select(years).rows().select(Array.of(
                "Brazil", "Germany", "Norway", "Singapore", "Sweden", "United Kingdom", "United States", "World"
        ));
    }

}
