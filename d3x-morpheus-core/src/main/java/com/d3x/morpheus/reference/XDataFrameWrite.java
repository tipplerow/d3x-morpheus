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

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import com.d3x.morpheus.csv.CsvSink;
import com.d3x.morpheus.csv.CsvSinkDefault;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameWrite;
import com.d3x.morpheus.util.Resource;

/**
 * The reference implementation of the DataFrameWrite interface to enable DataFrames to be written out to a storage device.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.AllArgsConstructor()
class XDataFrameWrite<R,C> implements DataFrameWrite<R,C> {

    private DataFrame<R,C> frame;

    @Override
    public CsvSink<R, C> csv(File file) {
        return new CsvSinkDefault<>(Resource.of(file), frame);
    }

    @Override
    public CsvSink<R, C> csv(URL url) {
        return new CsvSinkDefault<>(Resource.of(url), frame);
    }

    @Override
    public CsvSink<R, C> csv(InputStream is) {
        return new CsvSinkDefault<>(Resource.of(is), frame);
    }

    @Override
    public CsvSink<R, C> csv(String resource) {
        return new CsvSinkDefault<>(Resource.of(resource), frame);
    }
}
