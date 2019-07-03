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
package com.d3x.morpheus.db;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.util.Asserts;
import com.d3x.morpheus.util.functions.Function1;
import com.d3x.morpheus.util.sql.SQLPlatform;

/**
 * A class that defines all the options that can be configured to write a DataFrame to a SQL data store.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.Data()
public class DbSinkOptions<R,C> {

    private int batchSize;
    private String tableName;
    private SQLPlatform platform;
    private ColumnMappings columnMappings;
    private String autoIncrementColumnName;
    private Function<C,String> columnNames;
    private String rowKeyColumn;
    private Class<?> rowKeySqlClass;
    private Function1<R,?> rowKeyMapper;
    private Function<String,String> colKeyMapper;

    /**
     * Constructor
     */
    DbSinkOptions() {
        this.batchSize = 1000;
        this.columnMappings = new ColumnMappings();
        this.columnNames = v -> v.toString()
            .toLowerCase()
            .replace(" ", "_")
            .replace("%", "pct")
            .replace("-", "_")
            .replace("/", "_");
    }

    /**
     * Sets the row key mapping for these database options
     * @param colName   the column to apply row keys to
     * @param sqlType   the target SQL type for row key column
     * @param mapper    the row key mapper function
     * @param <T>       the mapped row key type
     */
    public <T> void setRowKeyMapping(String colName, Class<T> sqlType, Function1<R,T> mapper) {
        this.rowKeyColumn = Asserts.notNull(colName, "The row key column name cannot be null");
        this.rowKeySqlClass = Asserts.notNull(sqlType, "The row key SQL type cannot be null");
        this.rowKeyMapper = Asserts.notNull(mapper, "The row key mapper cannot be null");
    }


    /**
     * Sets the column mappings for these options
     * @param configurator the configurator for column mappings
     */
    public void setColumnMappings(Consumer<ColumnMappings> configurator) {
        configurator.accept(columnMappings);
    }


    /**
     * A class that maintains a mapping between a DataFrame column type and a JDBC column type
     */
    public class ColumnMappings {

        private Map<Class<?>,Class<?>> sqlTypeMap = new HashMap<>();
        private Map<Class<?>,Function1<DataFrameValue<R,C>,?>> mapperMap = new HashMap<>();

        /**
         * Constructor
         */
        ColumnMappings() {
            this.add(Boolean.class, Boolean.class, Function1.toBoolean(DataFrameValue::getBoolean));
            this.add(Integer.class, Integer.class, Function1.toInt(DataFrameValue::getInt));
            this.add(Long.class, Long.class, Function1.toLong(DataFrameValue::getLong));
            this.add(Double.class, Double.class, Function1.toDouble(DataFrameValue::getDouble));
            this.add(String.class, String.class, Function1.toValue(DataFrameValue::<String>getValue));
            this.add(java.sql.Date.class, java.sql.Date.class, Function1.toValue(DataFrameValue::<Date>getValue));
            this.add(java.sql.Time.class, java.sql.Time.class, Function1.toValue(DataFrameValue::<Time>getValue));
            this.add(java.sql.Timestamp.class, java.sql.Timestamp.class, Function1.toValue(DataFrameValue::<Timestamp>getValue));
            this.add(java.util.Date.class, java.sql.Date.class, Function1.toValue(v -> new Date(v.<java.util.Date>getValue().getTime())));
            this.add(LocalTime.class, Time.class, Function1.toValue(v -> Time.valueOf(v.<LocalTime>getValue())));
            this.add(LocalDate.class, java.sql.Date.class, Function1.toValue(v -> Date.valueOf(v.<LocalDate>getValue())));
            this.add(LocalDateTime.class, Timestamp.class, Function1.toValue(v -> Timestamp.valueOf(v.<LocalDateTime>getValue())));
            this.add(ZonedDateTime.class, Timestamp.class, Function1.toValue(v -> Timestamp.valueOf(v.<ZonedDateTime>getValue().toLocalDateTime())));
        }

        /**
         * Returns the SQL type for the DataFrame type
         * @param dataType  the DataFrame column type
         * @return          the SQL type
         * @throws DataFrameException   if no match for data type
         */
        Class<?> getSqlType(Class<?> dataType) throws DataFrameException {
            final Class<?> sqlType = sqlTypeMap.get(dataType);
            if (sqlType != null) {
                return sqlType;
            } else if (dataType.isEnum()) {
                return String.class;
            } else {
                throw new DataFrameException("No SQL type mapped for data type: " + dataType.getSimpleName());
            }
        }

        /**
         * Returns the mapper function to transform DataFrame type into SQL type
         * @param dataType      the DataFrame column type class
         * @return              the SQL column type class
         */
        Function1<DataFrameValue<R,C>,?> getMapper(Class<?> dataType) {
            final Function1<DataFrameValue<R,C>,?> mapper = mapperMap.get(dataType);
            if (mapper != null) {
                return mapper;
            } else if (dataType.isEnum()) {
                return Function1.toValue(v -> ((Enum)v).name());
            } else {
                throw new DataFrameException("No SQL mapper function for data type: " + dataType.getSimpleName());
            }
        }

        /**
         * Adds a mapping between a DataFrame column type and the approprivate JDBC type
         * @param dataClass  the DataFrame column data type
         * @param sqlClass   the SQL data type supported by JDBC
         * @param mapper    the mapper function to transform A into B
         * @param <A>       the DataFrame column type
         * @param <B>       the JDBC type
         */
        public <A,B> void add(Class<A> dataClass, Class<B> sqlClass, Function1<DataFrameValue<R,C>,B> mapper) {
            Asserts.notNull(dataClass, "The data type cannot be null");
            Asserts.notNull(sqlClass, "The sql type cannot be null");
            Asserts.notNull(mapper, "The sql mapper function cannot be nul");
            this.sqlTypeMap.put(dataClass, sqlClass);
            this.mapperMap.put(dataClass, mapper);
        }
    }
}

