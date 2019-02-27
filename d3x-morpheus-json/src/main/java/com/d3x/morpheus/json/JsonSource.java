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
package com.d3x.morpheus.json;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.parser.Parser;

/**
 * A source used to load a DataFrame from JSON content
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public interface JsonSource<R,C> {


    /**
     * Returns the standard Json source
     * @param <R>   the row key type
     * @param <C>   the column key type
     * @return      the newly created source
     */
    static <R,C> JsonSource<R,C> create() {
        return new Standard<>();
    }


    /**
     * Returns a DataFrame loaded from JSON in the resource specified
     * @param style the JSON style
     * @param file  the resource to load from
     * @return      the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    default DataFrame<R,C> read(JsonStyle style, File file) throws DataFrameException {
        return read(options -> {
            options.setFile(file);
            options.setStyle(style);
        });
    }


    /**
     * Returns a DataFrame loaded from JSON in the resource specified
     * @param style the JSON style
     * @param url   the resource to load from
     * @return      the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    default DataFrame<R,C> read(JsonStyle style, URL url) throws DataFrameException {
        return read(options -> {
            options.setURL(url);
            options.setStyle(style);
        });
    }


    /**
     * Returns a DataFrame loaded from JSON in the resource specified
     * @param style the JSON style
     * @param is    the resource to load from
     * @return      the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    default DataFrame<R,C> read(JsonStyle style, InputStream is) throws DataFrameException {
        return read(options -> {
            options.setInputStream(is);
            options.setStyle(style);
        });
    }


    /**
     * Returns a DataFrame loaded from JSON in the resource specified
     * @param style     the JSON style
     * @param resource  the resource to load from
     * @return      the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    default DataFrame<R,C> read(JsonStyle style, String resource) throws DataFrameException {
        return read(options -> {
            options.setResource(Resource.of(resource));
            options.setStyle(style);
        });
    }


    /**
     * Returns a DataFrame loaded from JSON defined by configured options
     * @param consumer  the consumer to configure options
     * @return          the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    default DataFrame<R,C> read(Consumer<Options<R,C>> consumer) {
        Options<R,C> options = new Options<>();
        consumer.accept(options);
        return read(options);
    }


    /**
     * Returns a DataFrame loaded from JSON as specified by the options
     * @param options   the options to describe how to load frame
     * @return          the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    DataFrame<R,C> read(Options<R,C> options) throws DataFrameException;



    /**
     * The options for this source
     * @param <R>   the row key type
     * @param <C>   the column key type
     */
    @lombok.Data()
    class Options<R,C> {

        /** The Json style for resource */
        private JsonStyle style;
        /** The resource to load frame from */
        private Resource resource;
        /** The formats used to parse JSON values */
        private Formats formats;
        /** The character encoding for content */
        private Charset charset;
        /** The row key parser */
        private Parser<R> rowKeyParser;
        /** The col key parser */
        private Parser<C> colKeyParser;
        /** The optional row predicate to filter rows */
        private Predicate<R> rowPredicate;
        /** The optional column predicate to filter columns */
        private Predicate<C> colPredicate;

        /**
         * Constructor
         */
        public Options() {
            this.formats = new Formats();
            this.charset = StandardCharsets.UTF_8;
        }

        /**
         * Sets the input file for these options
         * @param file  the input file
         */
        public void setFile(File file) {
            this.resource = Resource.of(file);
        }

        /**
         * Sets the input URL for these options
         * @param url   the input url
         */
        public void setURL(URL url) {
            this.resource = Resource.of(url);
        }

        /**
         * Applies to resource to load CSV content from
         * @param inputStream   the input stream to load from
         */
        public void setInputStream(InputStream inputStream) {
            this.resource = Resource.of(inputStream);
        }
    }


    /**
     * The default implementation of the JsonSource
     * @param <R>   the row key type
     * @param <C>   the column key type
     */
    class Standard<R,C> implements JsonSource<R,C> {
        @Override
        public DataFrame<R, C> read(Options<R,C> options) throws DataFrameException {
            switch (options.getStyle()) {
                case DEFAULT:   return new JsonSourceDefault<R,C>().read(options);
                case COLUMNS:   return new JsonSourceDefault<R,C>().read(options);
                case SPLIT:     return new JsonSourceDefault<R,C>().read(options);
                default:        throw new IllegalArgumentException("Unsupported style specified: " + options.getStyle());
            }
        }
    }

}
