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
package com.d3x.morpheus.wb;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.wb.entity.WBIndicator;
import com.d3x.morpheus.wb.source.WBIndicatorSource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A unit test for the World Bank Indicator source
 *
 * @see <a href="https://datahelpdesk.worldbank.org/knowledgebase/articles/898599-api-indicator-queries">World Bank API</a>
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class WBIndicatorTest {

    private WBIndicatorSource source = new WBIndicatorSource();


    @Test(enabled = false)
    public void testLoadIndicators() {
        final List<WBIndicator> indicators = WBIndicator.getIndicators();
        final Map<String,WBIndicator> indicatorMap = new HashMap<>(indicators.size());
        indicators.forEach(indicator -> indicatorMap.put(indicator.getId(), indicator));
        Assert.assertTrue(indicators.size() > 15000, "There are at least 15K indicators");
        Assert.assertTrue(indicatorMap.size() > 15000, "All indicators have a unique key");
    }


    @Test(enabled = false)
    public void testC02() {
        var path = "/indicators/EN.ATM.CO2E.PC.csv";
        var expected = DataFrame.read(path).csv(LocalDate.class, options -> {
            options.setRowKeyColumnIndex(0);
        });
        var actual = source.read(options -> {
            options.setBatchSize(1000);
            options.setIndicator("EN.ATM.CO2E.PC");
            options.setStartDate(LocalDate.of(2000, 1, 1));
            options.setEndDate(LocalDate.of(2013, 1, 1));
            options.setCountries("JP", "US", "DE", "IT", "GB", "FR", "CA", "CN");
        });

        //actual.write().csv(new File("EN.ATM.CO2E.PC.csv")).apply();

        expected.out().print(100);
        actual.out().print(100);
        DataFrameAsserts.assertEqualsByIndex(actual, expected);
    }



    @Test(enabled = false)
    public void testAll() {
        var frame = DataFrame.read("/Users/witdxav/Dropbox/data/world-bank/WDI/WDIData.csv").csv();
        frame.out().print();

        final Set<String> indicatorSet = new HashSet<>();
        frame.rows().forEach(row -> {
            final long nonNullCount = row.values().filter(v -> !v.isNull()).count();
            if (nonNullCount > 0) {
                final String indicator = row.getValue("Indicator Code");
                if (indicator != null) {
                    indicatorSet.add(indicator);
                }
            }
        });

        indicatorSet.forEach(indicator -> {
            System.out.println("Loading data for indicator: " + indicator);
            source.read(options -> {
                options.setBatchSize(10000);
                options.setIndicator(indicator);
                options.setStartDate(LocalDate.of(2014, 1, 1));
                options.setEndDate(LocalDate.of(2015, 1, 1));
            }).out().print();
        });
    }
}
