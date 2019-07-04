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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Try;
import com.d3x.morpheus.util.sql.SQL;
import com.d3x.morpheus.util.sql.SQLExtractor;
import com.d3x.morpheus.util.sql.SQLPlatform;
import com.d3x.morpheus.util.sql.SQLType;

/**
 * A DataFrameSource designed to handle read DataFrames from a SQL data store
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.AllArgsConstructor()
public class DbSource {

    @lombok.NonNull
    private Connection connection;

    /**
     * Constructor
     * @param dataSource    the data source to get connection
     */
    public DbSource(DataSource dataSource) {
        this.connection = Try.call(dataSource::getConnection);
    }

    public <R> DataFrame<R,String> read(Consumer<DbSourceOptions<R>> configurator) throws DataFrameException {
        try {
            var options = new DbSourceOptions<R>();
            configurator.accept(options);
            this.connection.setAutoCommit(options.isAutoCommit());
            this.connection.setReadOnly(options.isReadOnly());
            var fetchSize = options.getFetchSize();
            var sql = SQL.of(options.getSql(), options.getParameters().toArray());
            return sql.executeQuery(connection, fetchSize, rs -> read(rs, options));
        } catch (SQLException ex) {
            throw new DataFrameException("Failed to load data from db: " + ex.getMessage(), ex);

        } finally {
            IO.close(connection);
        }
    }


    /**
     * Reads all data from the sql ResultSet into a Morpheus DataFrame
     * @param resultSet     the result set to extract data from
     * @param options       the request descriptor
     * @return              the newly created DataFrame
     * @throws DataFrameException if data frame construction from result set fails
     */
    @SuppressWarnings("unchecked")
    private <R> DataFrame<R,String> read(ResultSet resultSet, DbSourceOptions<R> options) throws DataFrameException {
        try {
            var rowCapacity = options.getRowCapacity();
            var platform = getPlatform(resultSet);
            var metaData = resultSet.getMetaData();
            var columnList = getColumnInfo(metaData, platform, options);
            var rowKeyMapper = options.getRowKeyMapper();
            if (!resultSet.next()) {
                var rowKeys = (Index<R>)Index.empty();
                return createFrame(rowKeys, columnList, options);
            } else {
                var rowKey = rowKeyMapper.apply(resultSet);
                var rowKeyType = (Class<R>) rowKey.getClass();
                var rowKeyBuilder = ArrayBuilder.of(rowCapacity, rowKeyType);
                while (true) {
                    rowKey = rowKeyMapper.apply(resultSet);
                    rowKeyBuilder.add(rowKey);
                    for (ColumnInfo colInfo : columnList) {
                        colInfo.apply(resultSet);
                    }
                    if (!resultSet.next()) {
                        break;
                    }
                }
                var rowKeys = rowKeyBuilder.toArray();
                return createFrame(rowKeys, columnList, options);
            }
        } catch (DataFrameException ex) {
            throw ex;
        } catch (Throwable t) {
            throw new DataFrameException("Failed to initialize DataFrame from ResultSet: " + t.getMessage(), t);
        } finally {
            close(resultSet);
        }
    }


    /**
     * Returns the database platform type from the ResultSet
     * @param resultSet the result set
     * @return          the database type
     */
    private SQLPlatform getPlatform(ResultSet resultSet) {
        try {
            var metaData = resultSet.getStatement().getConnection().getMetaData();
            var driverClassName = metaData.getDriverName();
            return SQLPlatform.getPlatform(driverClassName);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to detect database platform type, please use withPlatform() on request", ex);
        }
    }


    /**
     * Returns a newly created DataFrame from the arguments specified
     * @param rowKeys       the row keys
     * @param columnList    the column list
     * @return              the newly created DataFrame
     */
    private <R> DataFrame<R,String> createFrame(Iterable<R> rowKeys, List<ColumnInfo> columnList, DbSourceOptions<R> options) {
        var colMapper = options.getColKeyMapper();
        return DataFrame.of(rowKeys, String.class, columns -> {
            for (ColumnInfo colInfo : columnList) {
                var colName = colInfo.name;
                var mapped = colMapper.apply(colName);
                var values = colInfo.array.toArray();
                columns.add(mapped, values);
            }
        });
    }


    /**
     * Returns the array of column information from the result-set meta-data
     * @param metaData      the result set meta data
     * @param platform      the database platform
     * @param options       the request descriptor
     * @return              the array of column information
     * @throws SQLException if there is a database access error
     */
    private <R> List<ColumnInfo> getColumnInfo(ResultSetMetaData metaData, SQLPlatform platform, DbSourceOptions<R> options) throws SQLException {
        var rowCapacity = options.getRowCapacity();
        var columnCount = metaData.getColumnCount();
        var columnInfoList = new ArrayList<ColumnInfo>(columnCount);
        var typeResolver = SQLType.getTypeResolver(platform);
        for (int i=0; i<columnCount; ++i) {
            var colIndex = i+1;
            var colName = metaData.getColumnName(colIndex);
            if (!options.getExcludeColumnSet().contains(colName)) {
                var typeCode = metaData.getColumnType(colIndex);
                var typeName = metaData.getColumnTypeName(colIndex);
                var sqlType = typeResolver.getType(typeCode, typeName);
                var extractor = options.getExtractor(colName, SQLExtractor.with(sqlType.typeClass(), platform));
                columnInfoList.add(new ColumnInfo(i, colIndex, colName, rowCapacity, extractor));
            }
        }
        return columnInfoList;
    }


    /**
     * Safely closes the JDBC resource
     * @param closeable the closeable
     */
    private void close(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    /**
     * A class used to capture the meta-data for a column
     */
    private class ColumnInfo {

        private int index;
        private int ordinal;
        private String name;
        private Class<?> type;
        private ArrayType typeCode;
        private SQLExtractor extractor;
        private ArrayBuilder<Object> array;


        /**
         * Constructor
         * @param ordinal   the DataFrame column ordinal
         * @param index     the JDBC column index
         * @param name      the JDBC column name
         * @param capacity  the initial capacity for column
         */
        @SuppressWarnings("unchecked")
        ColumnInfo(int ordinal, int index, String name, int capacity, SQLExtractor extractor) {
            this.index = index;
            this.ordinal = ordinal;
            this.name = name;
            this.type = extractor.getDataType();
            this.typeCode = ArrayType.of(type);
            this.array = (ArrayBuilder<Object>)ArrayBuilder.of(capacity, type);
            this.extractor = extractor;
        }

        /**
         * Applies the ResultSet to this column for current row
         * @param rs    the ResultSet reference
         */
        final void apply(ResultSet rs) {
            try {
                switch (typeCode) {
                    case BOOLEAN:   array.addBoolean(extractor.getBoolean(rs, index));  break;
                    case INTEGER:   array.addInt(extractor.getInt(rs, index));          break;
                    case LONG:      array.addLong(extractor.getLong(rs, index));        break;
                    case DOUBLE:    array.addDouble(extractor.getDouble(rs, index));    break;
                    default:        array.add(extractor.getValue(rs, index));           break;
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to extract data for column " + name, ex);
            }
        }
    }


}
