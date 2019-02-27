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
package com.d3x.morpheus.wb.source;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameSource;
import com.d3x.morpheus.wb.entity.WBCity;
import com.d3x.morpheus.wb.entity.WBCountry;
import com.d3x.morpheus.wb.entity.WBIncomeLevel;
import com.d3x.morpheus.wb.entity.WBLendingType;
import com.d3x.morpheus.wb.entity.WBRegion;

/**
 * A data source implementation that returns World Bank Country definitions.
 *
 * @see <a href="https://datahelpdesk.worldbank.org/knowledgebase/articles/898590-api-country-queries">World Bank API</a>
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class WBCountrySource implements DataFrameSource<String,WBCountry.Field,WBCountrySource.Options> {


    /**
     * Constructor
     */
    public WBCountrySource() {
        super();
    }


    @Override
    public DataFrame<String, WBCountry.Field> read(Consumer<Options> configurator) throws DataFrameException {
        final List<WBCountry> countryList = WBCountry.getCountries();
        final Array<WBCountry> countries = Array.ofIterable(countryList);
        final Options options = initOptions(new Options(), configurator);
        final WBCountry.Field firstColField = options.useIsoCode ? WBCountry.Field.ID : WBCountry.Field.ISO2;
        final Function<WBCountry,String> rowKey = options.useIsoCode ? WBCountry::getIso2Code : WBCountry::getId;
        final Function<WBCountry,String> firstCol = options.useIsoCode ? WBCountry::getId : WBCountry::getIso2Code;
        final Array<String> ids = countries.map(v -> rowKey.apply(v.getValue()));
        return DataFrame.of(ids, WBCountry.Field.class, columns -> {
            columns.add(firstColField, countries.map(v -> firstCol.apply(v.getValue())));
            columns.add(WBCountry.Field.NAME, countries.map(v -> v.getValue().getName()));
            columns.add(WBCountry.Field.CAPITAL, countries.map(v -> v.getValue().getCapitalCity().map(WBCity::getName).orElse(null)));
            columns.add(WBCountry.Field.LONGITUDE, countries.map(v -> v.getValue().getCapitalCity().map(WBCity::getLongitude).orElse(null)));
            columns.add(WBCountry.Field.LATITUDE, countries.map(v -> v.getValue().getCapitalCity().map(WBCity::getLatitude).orElse(null)));
            columns.add(WBCountry.Field.REGION_ID, countries.map(v -> v.getValue().getRegion().map(WBRegion::getId).orElse(null)));
            columns.add(WBCountry.Field.REGION_NAME, countries.map(v -> v.getValue().getRegion().map(WBRegion::getValue).orElse(null)));
            columns.add(WBCountry.Field.LENDING_TYPE_ID, countries.map(v -> v.getValue().getLendingType().map(WBLendingType::getId).orElse(null)));
            columns.add(WBCountry.Field.LENDING_TYPE_NAME, countries.map(v -> v.getValue().getLendingType().map(WBLendingType::getValue).orElse(null)));
            columns.add(WBCountry.Field.INCOME_LEVEL_ID, countries.map(v -> v.getValue().getIncomeLevel().map(WBIncomeLevel::getId).orElse(null)));
            columns.add(WBCountry.Field.INCOME_LEVEL_NAME, countries.map(v -> v.getValue().getIncomeLevel().map(WBIncomeLevel::getValue).orElse(null)));
        });
    }





    public static class Options {

        private String regex;
        private boolean useIsoCode;

        /**
         * Sets a regex used to match country definitions
         * @param regex     the regex expression
         */
        public void setRegex(String regex) {
            this.regex = regex;
        }


        /**
         * Sets whether to use the iso code as the row index
         * @param useIsoCode    true to use iso code for row index
         */
        public void setUseIsoCode(boolean useIsoCode) {
            this.useIsoCode = useIsoCode;
        }
    }

}
