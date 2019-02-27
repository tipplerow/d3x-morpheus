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
package com.d3x.morpheus.csv;

import java.io.File;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Consumer;

import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.util.text.Formats;
import com.d3x.morpheus.util.text.printer.Printer;

/**
 * A class that defines the various options that can be used to control the output of a DataFrame to CSV
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.Data()

public class CsvSinkOptions<R> {

    /** The title row row header column */
    private String title;
    /** The text to print for a null value */
    private String nullText;
    /** The formats used to render data types */
    private Formats formats;
    /** The separator for row values */
    private String separator;
    /** The resource to output to */
    private Resource resource;
    /** True to include a column with row keys */
    private boolean includeRowHeader;
    /** True to include a row with column keys */
    private boolean includeColumnHeader;
    /** The printer used to render row keys */
    private Printer<R> rowKeyPrinter;

    /**
     * Constructor
     */
    CsvSinkOptions() {
        this.separator = ",";
        this.title = "DataFrame";
        this.formats = new Formats();
        this.includeRowHeader = true;
        this.includeColumnHeader = true;
    }


    /**
     * Returns the row key printer for these options
     * @return      the row key printer
     */
    Optional<Printer<R>> getRowKeyPrinter() {
        return Optional.ofNullable(rowKeyPrinter);
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

    /**
     * Sets the file path to write output to
     * @param path  the fully qualified file path
     */
    public void setFile(String path) {
        this.resource = Resource.of(new File(path));
    }

    /**
     * Sets the file handle to write output to
     * @param file  the file handle
     */
    public void setFile(File file) {
        this.resource = Resource.of(file);
    }

    /**
     * Sets the output stream to write output to
     * @param os    the output stream
     */
    public void setOutputStream(OutputStream os) {
        this.resource = Resource.of(os);
    }

}
