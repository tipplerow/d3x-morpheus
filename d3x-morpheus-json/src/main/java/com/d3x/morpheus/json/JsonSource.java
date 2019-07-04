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
     * Returns a json source for the style specified
     * @param style the json style
     * @param <R>   the row key type
     * @param <C>   the column key type
     * @return      the newly created source
     */
    static <R,C> JsonSource<R,C> create(JsonStyle style) {
        switch (style) {
            case DEFAULT:   return new JsonSourceDefault<>();
            case COLUMNS:   return new JsonSourceColumns<>();
            case SPLIT:     return new JsonSourceSplit<>();
            default:        throw new IllegalArgumentException("Unsupported style specified: " + style);
        }
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
            options.resource(Resource.of(file));
            options.style(style);
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
            options.resource(Resource.of(url));
            options.style(style);
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
            options.resource(Resource.of(is));
            options.style(style);
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
            options.resource(Resource.of(resource));
            options.style(style);
        });
    }


    /**
     * Returns a DataFrame loaded from JSON defined by configured options
     * @param consumer  the consumer to configure options
     * @return          the loaded DataFrame
     * @throws DataFrameException   if frame fails to load from json
     */
    default DataFrame<R,C> read(Consumer<Options.OptionsBuilder<R,C>> consumer) {
        return read(Options.create(consumer));
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
    @lombok.Builder()
    @lombok.ToString()
    @lombok.NoArgsConstructor()
    @lombok.AllArgsConstructor()
    class Options<R,C> {

        /** The Json style for resource */
        @lombok.NonNull @lombok.Builder.Default
        private JsonStyle style = JsonStyle.DEFAULT;
        /** The resource to load frame from */
        @lombok.NonNull
        private Resource resource;
        /** The formats used to parse JSON values */
        @lombok.NonNull @lombok.Builder.Default
        private Formats formats = new Formats();
        /** The character encoding for content */
        @lombok.NonNull @lombok.Builder.Default
        private Charset charset = StandardCharsets.UTF_8;
        /** The row key parser */
        private Parser<R> rowKeyParser;
        /** The col key parser */
        private Parser<C> colKeyParser;
        /** The optional row predicate to filter rows */
        private Predicate<R> rowPredicate;
        /** The optional column predicate to filter columns */
        private Predicate<C> colPredicate;


        /**
         * Returns new options initialized by consumer
         * @param consumer  the consumer reference
         * @return          the new options
         */
        public static <R,C> Options<R,C> create(Consumer<Options.OptionsBuilder<R,C>> consumer) {
            var builder = Options.<R,C>builder();
            consumer.accept(builder);
            return builder.build();
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
            return JsonSource.<R,C>create(options.getStyle()).read(options);
        }
    }

}
