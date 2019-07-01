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

import java.io.OutputStreamWriter;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.IO;
import com.google.gson.stream.JsonWriter;

/**
 * A convenience base class for JsonSinks that output different json formats
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
public abstract class JsonSinkBase<R,C> implements JsonSink<R,C> {


    @Override
    public synchronized void write(DataFrame<R,C> frame, Options options) {
        JsonWriter writer = null;
        try {
            var encoding = options.getEncoding();
            var os = options.getResource().toOutputStream();
            writer = new JsonWriter(new OutputStreamWriter(os, encoding));
            writer.setIndent(options.isPretty() ? "  " : "");
            writer.setSerializeNulls(options.isNulls());
            this.write(writer, frame, options);
            writer.flush();
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame to JSON output", ex);
        } finally {
            IO.close(writer);
        }
    }


    /**
     * Writes out the data frame using the json writer
     * @param writer        the json writer
     * @param frame         the frame reference
     * @param options       the output options
     */
    public abstract void write(JsonWriter writer, DataFrame<R, C> frame, Options options);

}
