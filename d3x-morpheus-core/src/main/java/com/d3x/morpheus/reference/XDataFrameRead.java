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
package com.d3x.morpheus.reference;

import java.util.function.Consumer;

import com.d3x.morpheus.csv.CsvSource;
import com.d3x.morpheus.csv.CsvSourceDefault;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameRead;
import com.d3x.morpheus.util.Resource;

/**
 * The default implementation of the DataFrame read interface
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.AllArgsConstructor()
class XDataFrameRead implements DataFrameRead {

    @lombok.NonNull
    private Resource resource;


    @Override
    public DataFrame<Integer, String> csv() {
        return new CsvSourceDefault(resource).read();
    }

    @Override
    public <R> DataFrame<R, String> csv(Class<R> rowType, Consumer<CsvSource.Options> configurator) {
        return new CsvSourceDefault(resource).read(rowType, configurator);
    }
}
