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
package com.d3x.morpheus.series;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.d3x.core.util.Option;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.util.text.Formats;

/**
 * A component used to read a CSV resource into a Morpheus DataSeries
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @param <K>   the key type
 * @param <V>   the value type
 */
@lombok.AllArgsConstructor()
public class DataSeriesRead<K,V,D extends DataSeries<K,V>> {

    @lombok.NonNull
    private Resource resource;


    /**
     * Returns a data series parsed from a CSV resource selecting first two columns
     * @return      the data series result
     */
    public D csv() {
        return csv(o -> {
            o.setKeyColIndex(0);
            o.setValColIndex(1);
        });
    }


    /**
     * Returns a data series parsed from a CSV resource selecting the columns specified
     * @param keyColIndex   the column index for series keys
     * @param valColIndex   the column index for series values
     * @return      the data series result
     */
    public D csv(int keyColIndex, int valColIndex) {
        return csv(o -> {
            o.setKeyColIndex(keyColIndex);
            o.setValColIndex(valColIndex);
        });
    }


    /**
     * Returns a data series parsed from a CSV resource selecting the columns specified
     * @param keyColName   the column name for series keys
     * @param valColName   the column name for series values
     * @return      the data series result
     */
    public D csv(String keyColName, String valColName) {
        return csv(o -> {
            o.setKeyColName(keyColName);
            o.setValColName(valColName);
        });
    }


    /**
     * Returns a data series parsed from a CSV resource
     * @param configurator  the options configurator function
     * @return              the data series result
     */
    public D csv(Consumer<CsvOptions> configurator) {
        var options = new CsvOptions();
        configurator.accept(options);
        return from(DataFrame.read(resource.toInputStream()).csv(Integer.class, o -> {
            options.getHeader().ifPresent(o::setHeader);
            options.getFormats().ifPresent(o::setFormats);
            options.getColumnNames().ifPresent(o::setIncludeColumns);
            options.getColumnIndexes().ifPresent(o::setIncludeColumnIndexes);
        }));
    }


    /**
     * Returns a series extracted from a column in the data frame provided
     * @param frame     the data frame to extract column data from
     * @return          the resulting series
     */
    @SuppressWarnings("unchecked")
    private D from(DataFrame<Integer,?> frame) {
        var keyType = (Class<K>)frame.colAt(0).typeInfo();
        var valueType = (Class<V>)frame.colAt(1).typeInfo();
        var builder = DataSeriesBuilder.builder(keyType, valueType);
        builder.capacity(frame.rowCount());
        if (valueType.equals(Boolean.class)) {
            frame.rows().forEach(row -> {
                var key = (K)row.getValueAt(0);
                var value = row.getBooleanAt(1);
                builder.putBoolean(key, value);
            });
        } else if (valueType.equals(Integer.class)) {
            frame.rows().forEach(row -> {
                var key = (K)row.getValueAt(0);
                var value = row.getIntAt(1);
                builder.putInt(key, value);
            });
        } else if (valueType.equals(Long.class)) {
            frame.rows().forEach(row -> {
                var key = (K)row.getValueAt(0);
                var value = row.getLongAt(1);
                builder.putLong(key, value);
            });
        } else if (valueType.equals(Double.class)) {
            frame.rows().forEach(row -> {
                var key = (K)row.getValueAt(0);
                var value = row.getDoubleAt(1);
                builder.putDouble(key, value);
            });
        } else {
            frame.rows().forEach(row -> {
                var key = (K)row.getValueAt(0);
                var value = (V)row.getValueAt(1);
                builder.putValue(key, value);
            });
        }
        return (D)builder.build();
    }


    /**
     * Options to customized data series CSV parsing
     */
    @lombok.NoArgsConstructor()
    public static class CsvOptions {

        @lombok.Setter
        private Boolean header;
        @lombok.Setter
        private Formats formats;
        @lombok.Setter
        private String keyColName;
        @lombok.Setter
        private String valColName;
        @lombok.Setter
        private Integer keyColIndex;
        @lombok.Setter
        private Integer valColIndex;

        /**
         * Returns the formats for these options
         * @return  the formats
         */
        Option<Formats> getFormats() {
            return Option.of(formats);
        }

        /**
         * Returns if header is included
         * @return  true if header expected
         */
        Option<Boolean> getHeader() {
            return Option.of(header);
        }

        /**
         * Returns the column names to include from CSV
         * @return  the column names
         */
        Option<String[]> getColumnNames() {
            var names = Stream.of(keyColName, valColName).filter(Objects::nonNull).toArray(String[]::new);
            return names.length > 0 ? Option.of(names) : Option.empty();
        }

        /**
         * Returns the column indexes to include from CSV
         * @return  the column indexes
         */
        Option<int[]> getColumnIndexes() {
            var indexes = Stream.of(keyColIndex, valColIndex).filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
            if (indexes.length == 0 && getColumnNames().isEmpty()) {
                return Option.of(new int[] {0, 1});
            } else {
                return indexes.length > 0 ? Option.of(indexes) : Option.empty();
            }
        }
    }
}
