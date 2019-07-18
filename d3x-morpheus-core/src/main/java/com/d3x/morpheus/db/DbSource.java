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
package com.d3x.morpheus.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.IO;
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
     * Returns a DataFrame loaded from the underlying database using the options provided
     * @param configurator  the options configurator
     * @param <R>           the row key type
     * @return              the resulting frame
     * @throws DataFrameException   if fails to load data frame
     */
    public <R> DataFrame<R,String> apply(Consumer<Options> configurator) throws DataFrameException {
        var options = new Options();
        configurator.accept(options);
        var sql = options.getSql();
        if (sql == null) {
            throw new IllegalArgumentException("No SQL specified in options, call setSql()");
        } else {
            try {
                var stmt = connection.prepareStatement(sql);
                stmt.setMaxRows(options.getMaxRows());
                stmt.setQueryTimeout((int)options.getQueryTimeout().toSeconds());
                stmt.setFetchSize(options.getFetchSize());
                var rs = stmt.executeQuery();
                return read(rs, options);
            } catch (SQLException ex) {
                throw new DataFrameException("Failed to load data from db: " + ex.getMessage(), ex);
            } finally {
                IO.close(connection);
            }
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
    private <R> DataFrame<R,String> read(ResultSet resultSet, Options options) throws DataFrameException {
        try {
            var platform = getPlatform(resultSet);
            var metaData = resultSet.getMetaData();
            var columns = getColumns(metaData, platform, options);
            if (!resultSet.next()) {
                var rowKeys = (Index<R>)Index.empty();
                return createFrame(rowKeys, columns, options);
            } else {
                var counter = 1;
                var t1 = System.currentTimeMillis();
                for (ColumnInfo column : columns) {
                    column.apply(resultSet);
                }
                while (resultSet.next()) {
                    for (ColumnInfo column : columns) {
                        column.apply(resultSet);
                    }
                    if (++counter % options.getLogRowCount() == 0) {
                        var time = System.currentTimeMillis() - t1;
                        IO.println("Extracted " + counter + " rows in " + time + " millis");
                    }
                }
                if (options.getRowIndexColumnName() == null) {
                    var rowKeys = (Array<R>)Range.of(0, counter).toArray();
                    return createFrame(rowKeys, columns, options);
                } else {
                    var name = options.getRowIndexColumnName();
                    var column = columns.stream().filter(v -> v.name.equalsIgnoreCase(name)).findFirst().orElse(null);
                    if (column == null) {
                        throw new IllegalArgumentException("No column matches row index column name: " + name);
                    } else {
                        var rowKeys = (Array<R>)column.array.toArray();
                        var data = columns.stream().filter(v -> !v.name.equalsIgnoreCase(name)).collect(Collectors.toList());
                        return createFrame(rowKeys, data, options);
                    }
                }
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
    private <R> DataFrame<R,String> createFrame(Iterable<R> rowKeys, List<ColumnInfo> columnList, Options options) {
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
    private List<ColumnInfo> getColumns(ResultSetMetaData metaData, SQLPlatform platform, Options options) throws SQLException {
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
                var extractor = SQLExtractor.with(sqlType.typeClass(), platform);
                columnInfoList.add(new ColumnInfo(colIndex, colName, rowCapacity, extractor));
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
        private String name;
        private Class<?> type;
        private ArrayType typeCode;
        private SQLExtractor extractor;
        private ArrayBuilder<?> array;


        /**
         * Constructor
         * @param index     the JDBC column index
         * @param name      the JDBC column name
         * @param capacity  the initial capacity for column
         */
        ColumnInfo(int index, String name, int capacity, SQLExtractor extractor) {
            this.index = index;
            this.name = name;
            this.type = extractor.getDataType();
            this.typeCode = ArrayType.of(type);
            this.array = ArrayBuilder.of(capacity, type);
            this.extractor = extractor;
        }

        /**
         * Applies the ResultSet to this column for current row
         * @param rs    the ResultSet reference
         */
        final void apply(ResultSet rs) {
            try {
                switch (typeCode) {
                    case BOOLEAN:   array.appendBoolean(extractor.getBoolean(rs, index));  break;
                    case INTEGER:   array.appendInt(extractor.getInt(rs, index));          break;
                    case LONG:      array.appendLong(extractor.getLong(rs, index));        break;
                    case DOUBLE:    array.appendDouble(extractor.getDouble(rs, index));    break;
                    default:        array.append(extractor.getValue(rs, index));           break;
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to extract data for column " + name, ex);
            }
        }
    }


    /**
     * The options for this source
     */
    @lombok.Data()
    public static class Options {

        private String sql;
        private int maxRows;
        private int fetchSize = 100;
        private int rowCapacity = 1000;
        private int logRowCount = Integer.MAX_VALUE;
        private boolean readOnly = false;
        private boolean autoCommit = true;
        private String rowIndexColumnName;
        private Duration queryTimeout = Duration.ofSeconds(0);
        private List<Object> parameters = new ArrayList<>();
        private Set<String> excludeColumnSet = new HashSet<>();
        private Function<String,String> colKeyMapper = v -> v;

    }



}
