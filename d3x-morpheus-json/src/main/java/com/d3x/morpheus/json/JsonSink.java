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
import java.io.OutputStream;
import java.util.function.Consumer;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.Initialiser;
import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.util.text.Formats;

/**
 * A DataFrameSink implementation that writes a DataFrame out in a Morpheus specific JSON format.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public interface JsonSink<R,C> {


    /**
     * Returns a newly created JsonSink
     * @param <R>   the row key type
     * @param <C>   the column key type
     * @return      thew newly created sink
     */
    static <R,C> JsonSink<R,C> create() {
        return new Delegate<>();
    }

    /**
     * Writes a DataFrame to JSON based on the configured options
     * @param frame     the frame to write
     * @param style     the JSON style to output
     * @param file      the file to write to
     */
    default void write(DataFrame<R,C> frame, JsonStyle style, File file) {
        this.write(frame, options -> {
            options.setFile(file);
            options.setStyle(style);
        });
    }


    /**
     * Writes a DataFrame to JSON based on the configured options
     * @param frame     the frame to write
     * @param style     the JSON style to output
     * @param os        the output stream to write to
     */
    default void write(DataFrame<R,C> frame, JsonStyle style, OutputStream os) {
        this.write(frame, options -> {
            options.setOutputStream(os);
            options.setStyle(style);
        });
    }


    /**
     * Writes a DataFrame to JSON based on the configured options
     * @param frame         the frame to write
     * @param configurator  the options configurator
     */
    default void write(DataFrame<R,C> frame, Consumer<Options> configurator) {
        write(frame, Initialiser.apply(Options.class, configurator));
    }


    /**
     * Writes a DataFrame to JSON based on the configured options
     * @param frame     the frame to write
     * @param options   the options for output
     */
    void write(DataFrame<R,C> frame, Options options);



    /**
     * A sink that delegates according to the requested style
     * @param <R>       the row key type
     * @param <C>       the column key type
     */
    class Delegate<R,C> implements JsonSink<R,C> {
        @Override
        public void write(DataFrame<R,C> frame, Options options) {
            final JsonStyle style = options.getStyle();
            switch (style) {
                case SPLIT:     new JsonSinkSplit<R,C>().write(frame, options);     break;
                case COLUMNS:   new JsonSinkColumns<R,C>().write(frame, options);   break;
                case DEFAULT:   new JsonSinkDefault<R,C>().write(frame, options);   break;
                default:    throw new IllegalArgumentException("Unsupported JSON style: " + style);
            }
        }
    }



    /**
     * The options for this sink
     */
    @lombok.Data()
    class Options {

        /** The JSON style to output */
        private JsonStyle style;
        /** The formats to parse JSON values */
        private Formats formats;
        /** The resource to load the frame from */
        private Resource resource;
        /** The charset encoding for content */
        private String encoding;

        /**
         * Constructor
         */
        public Options() {
            this.style = JsonStyle.DEFAULT;
            this.formats = new Formats();
            this.encoding = "UTF-8";
        }

        /**
         * Sets the resource output stream for these options
         * @param os    the output stream to write to
         */
        public void setOutputStream(OutputStream os) {
            this.resource = Resource.of(os);
        }

        /**
         * Sets the resource file for these options
         * @param file  the output file to write to
         */
        public void setFile(File file) {
            this.resource = Resource.of(file);
        }

        /**
         * Sets the resource file for these options
         * @param path  the output file path to write to
         */
        public void setFile(String path) {
            this.resource = Resource.of(new File(path));
        }

        /**
         * Sets the formats to use for output to CSV
         * @param configure   the formats to apply
         */
        public void withFormats(Consumer<Formats> configure) {
            if (formats != null) {
                configure.accept(formats);
            } else {
                this.formats = new Formats();
                configure.accept(formats);
            }
        }
    }

}

