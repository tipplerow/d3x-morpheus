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
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Consumer;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.Collect;
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

    /** The map of common data types keyed by their label */
    Map<String,Class<?>> typeMap = Collect.asMap(v -> {
        v.put("boolean", Boolean.class);
        v.put("integer", Integer.class);
        v.put("long", Long.class);
        v.put("double", Double.class);
        v.put("string", String.class);
        v.put("date-util", Date.class);
        v.put("date", LocalDate.class);
        v.put("time", LocalTime.class);
        v.put("datetime", LocalDateTime.class);
        v.put("datetime-tz", ZonedDateTime.class);
    });

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
            options.resource(Resource.of(file));
            options.style(style);
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
            options.resource(Resource.of(os));
            options.style(style);
        });
    }


    /**
     * Writes a DataFrame to JSON based on the configured options
     * @param frame         the frame to write
     * @param configurator  the options configurator
     */
    default void write(DataFrame<R,C> frame, Consumer<Options.OptionsBuilder> configurator) {
        write(frame, Options.create(configurator));
    }


    /**
     * Returns the data type implied by the label
     * @param label the label for data type
     * @param <T>   the expected type
     * @return      the class for label
     */
    @SuppressWarnings("unchecked")
    static <T> Class<T> getDataType(String label) {
        try {
            var type = typeMap.get(label);
            if (type != null) {
                return (Class<T>)type;
            } else if (label.startsWith("enum:")) {
                return (Class<T>)Class.forName(label.replace("enum:", ""));
            } else {
                return (Class<T>)Class.forName(label);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to resolve type for label: " + label, ex);
        }
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
    @lombok.Builder()
    @lombok.ToString()
    @lombok.NoArgsConstructor()
    @lombok.AllArgsConstructor()
    class Options {

        /** The resource to load the frame from */
        @lombok.NonNull
        private Resource resource;
        /** The JSON style to output */
        @lombok.NonNull @lombok.Builder.Default
        private JsonStyle style = JsonStyle.DEFAULT;
        /** The formats to parse JSON values */
        @lombok.NonNull @lombok.Builder.Default
        private Formats formats = new Formats();
        /** The charset encoding for content */
        @lombok.NonNull @lombok.Builder.Default
        private String encoding = "UTF-8";
        /** True to pretty print json */
        @lombok.Builder.Default
        private boolean pretty = false;
        /** True to serialize nulls json */
        @lombok.Builder.Default
        private boolean nulls = true;


        /**
         * Returns new options initialized by consumer
         * @param consumer  the consumer reference
         * @return          the new options
         */
        public static Options create(Consumer<Options.OptionsBuilder> consumer) {
            var builder = Options.builder();
            consumer.accept(builder);
            return builder.build();
        }
    }

}

