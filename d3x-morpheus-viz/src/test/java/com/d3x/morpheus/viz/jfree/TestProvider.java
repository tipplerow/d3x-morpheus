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

package com.d3x.morpheus.viz.jfree;

import java.time.LocalDate;

import com.d3x.morpheus.frame.DataFrame;


public class TestProvider {

    /**
     * Returns a DataFrame for the ticker specified
     * @param ticker        the ticker reference
     * @return              the DataFrame result
     */
    public static DataFrame<LocalDate,String> getQuotes(String ticker) {
        return DataFrame.read().<LocalDate>csv("/" + ticker + ".csv").read(options -> {
            options.setRowKeyColumnName("Date");
        });
    }

}
