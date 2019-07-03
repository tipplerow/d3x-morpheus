/*
 * Copyright (C) 2018-2019 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.csv;

import java.util.function.Consumer;

import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.printer.Printer;

/**
 * Interface to a component that can emit the contents of a DataFrame to CSV format
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public interface CsvSink<R,C> {

    /**
     * Writes the data frame out to CSV
     */
    void apply();

    /**
     * Writes the data frame out to CSV
     * @param configurator the options configurator
     */
    void apply(Consumer<Options<R,C>> configurator);


    /**
     * The options for the CsvSink
     * @param <R>   the row key type
     */
    @lombok.Data()
    class Options<R,C> {

        /** The title row row header column */
        private String title;
        /** The text to print for a null value */
        private String nullText;
        /** The formats used to render data types */
        private Formats formats;
        /** The separator for row values */
        private String separator;
        /** True to include a column with row keys */
        private boolean includeRowHeader;
        /** True to include a row with column keys */
        private boolean includeColumnHeader;
        /** The printer used to render row keys */
        private Printer<R> rowKeyPrinter;
        /** The printer used to render column keys */
        private Printer<C> colKeyPrinter;

        /**
         * Constructor
         */
        public Options() {
            this.separator = ",";
            this.title = "DataFrame";
            this.formats = new Formats();
            this.includeRowHeader = true;
            this.includeColumnHeader = true;
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
