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

import java.io.InputStreamReader;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * A JsonSource implementation that can load a DataFrame from Pandas compatible JSON with "columns" orientation
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author Xavier Witdouck
 */
class JsonSourceColumns<R,C> implements JsonSource<R,C> {

    private JsonReader reader;

    @Override
    public synchronized DataFrame<R,C> read(Options<R,C> options) throws DataFrameException {
        try {
            this.reader = new JsonReader(new InputStreamReader(options.getResource().toInputStream()));
            this.reader.beginObject();
            while (reader.hasNext()) {
                final String name = reader.nextName();
                final C colKey = options.getColKeyParser().apply(name);
                this.reader.beginObject();
                while (true) {
                    final JsonToken token = reader.peek();
                    if (token == JsonToken.END_OBJECT) {
                        reader.endObject();
                        break;
                    } else if (token == JsonToken.NAME) {
                        final String rowLabel = reader.nextName();
                        final R rowKey = options.getRowKeyParser().apply(rowLabel);

                    } else {
                        switch (token) {
                            case NAME:  break;
                            case BOOLEAN:   break;
                            case NUMBER:    break;
                            case STRING:    break;
                            case NULL:      break;
                        }
                    }
                }
            }
            reader.endObject();
            return null;
        } catch (Exception ex) {
            throw new DataFrameException("Failed to load DataFrame from JSON: " + options, ex);
        }
    }
}
