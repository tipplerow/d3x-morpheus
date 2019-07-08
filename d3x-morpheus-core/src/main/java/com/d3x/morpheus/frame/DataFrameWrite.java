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

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import com.d3x.morpheus.csv.CsvSink;

/**
 * An interface that can be used to write a DataFrame to an output device for storage or network transfer.
 *
 * <p>This is open source software released under the <a href="http://www.ap`ache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public interface DataFrameWrite<R,C> {

    /**
     * Returns a CSV sink to write a DF to CSV
     * @param file      the input file
     * @return              the CSV sink
     */
    CsvSink<R,C> csv(File file);

    /**
     * Returns a CSV sink to write a DF to CSV
     * @param url       the input url
     * @return              the CSV sink
     */
    CsvSink<R,C> csv(URL url);

    /**
     * Returns a CSV sink to write a DF to CSV
     * @param os        the output stream to write to
     * @return              the CSV sink
     */
    CsvSink<R,C> csv(OutputStream os);

    /**
     * Returns a CSV sink to write a DF to CSV
     * @param resource      a file name or URL
     * @return              the CSV sink
     */
    CsvSink<R,C> csv(String resource);

}
