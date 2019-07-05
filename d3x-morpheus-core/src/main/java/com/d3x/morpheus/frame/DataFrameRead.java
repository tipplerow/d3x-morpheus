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
package com.d3x.morpheus.frame;

import java.util.function.Consumer;

import com.d3x.morpheus.csv.CsvSource;

/**
 * An interface used to read a DataFrame stored in various formats from some underlying storage devices.
 *
 * <p>This is open source software released under the <a href="http://www.ap`ache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public interface DataFrameRead {

    /**
     * Loads underlying resource into a DataFrame with sequence of integers for row keys
     * @return      the data frame result containing CSV data
     * @throws DataFrameException   if fails to parse resource
     */
    DataFrame<Integer,String> csv();

    /**
     * Loads underlying resource into a DataFrame with using a configured column for row keys
     * @param rowType       the row type
     * @param configurator  the options configurator
     * @return      the data frame result containing CSV data
     */
    <R> DataFrame<R,String> csv(Class<R> rowType, Consumer<CsvSource.Options> configurator);


}
