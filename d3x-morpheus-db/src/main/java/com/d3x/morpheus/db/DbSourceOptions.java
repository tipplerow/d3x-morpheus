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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.sql.DataSource;

import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.Try;
import com.d3x.morpheus.util.sql.SQLExtractor;

/**
 * A DataFrameRequest used to load a DataFrame from a SQL Database.
 *
 * @param <R>   the row key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
@lombok.Data()
public class DbSourceOptions<R> {

    private String sql;
    private int rowCapacity;
    private int fetchSize;
    private List<Object> parameters;
    private boolean autoCommit = true;
    private boolean readOnly = false;
    private Set<String> excludeColumnSet;
    private Function<ResultSet,R> rowKeyMapper;
    private Function<String,String> colKeyMapper;
    private Map<String,SQLExtractor> extractorMap;

    /**
     * Constructor
     */
    @SuppressWarnings("unchecked")
    DbSourceOptions() {
        this.rowCapacity = 1000;
        this.fetchSize = 1000;
        this.parameters = new ArrayList<>();
        this.excludeColumnSet = new HashSet<>();
        this.extractorMap = new HashMap<>();
        this.colKeyMapper = v -> v;
        this.rowKeyMapper = (ResultSet rs) -> {
            try {
                return (R)rs.getObject(1);
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to read row key from SQL ResultSet", ex);
            }
        };
    }


    /**
     * Sets the extractor to use for the column name
     * @param colName   the JDBC column name
     * @param extractor the extractor to use for column
     * @return          this reader
     */
    public DbSourceOptions<R> withExtractor(String colName, SQLExtractor extractor) {
        Objects.requireNonNull(colName, "The column name cannot be null");
        Objects.requireNonNull(extractor, "The database extractor");
        this.extractorMap.put(colName, extractor);
        return this;
    }


    SQLExtractor getExtractor(String colName, SQLExtractor fallback) {
        return extractorMap.getOrDefault(colName, fallback);
    }

}
