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
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;

import com.d3x.morpheus.csv.CsvSourceOptions;

/**
 * An interface used to read a DataFrame stored in various formats from some underlying storage devices.
 *
 * <p>This is open source software released under the <a href="http://www.ap`ache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public interface DataFrameRead {

    /**
     * Reads a DataFrame from a CSV file
     * @param file      the input file
     * @param <R>       the row key type
     * @return          the resulting DataFrame
     */
    <R> DataFrame<R,String> csv(File file);

    /**
     * Reads a DataFrame from a CSV file
     * @param url       the input url
     * @param <R>       the row key type
     * @return          the resulting DataFrame
     */
    <R> DataFrame<R,String> csv(URL url);

    /**
     * Reads a DataFrame from a CSV file
     * @param is        the input stream to read from
     * @param <R>       the row key type
     * @return          the resulting DataFrame
     */
    <R> DataFrame<R,String> csv(InputStream is);

    /**
     * Reads a DataFrame from a CSV resource
     * @param resource      a file name or URL
     * @param <R>           the row key type
     * @return              the resulting DataFrame
     */
    <R> DataFrame<R,String> csv(String resource);

    /**
     * Reads a DataFrame from a CSV resource based on the options configurator
     * @param configurator  the configurator for CSV options
     * @param <R>           the row key type
     * @return              the resulting DataFrame
     */
    <R> DataFrame<R,String> csv(Consumer<CsvSourceOptions<R>> configurator);

}
