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
package com.d3x.morpheus.reference;

import java.util.HashMap;
import java.util.Map;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameOptions;
import com.d3x.morpheus.frame.DataFrameRank;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

/**
 * The reference implementation of the DataFrameRank interface
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class XDataFrameRank<R,C> implements DataFrameRank<R,C> {

    private static final Map<Object,Map<DataFrameOptions,Object>> optionsMap = new HashMap<>();

    /**
     * Static initializer
     */
    static {
        try {
            optionsMap.put(NaNStrategy.class, new HashMap<>());
            optionsMap.put(TiesStrategy.class, new HashMap<>());
            optionsMap.get(NaNStrategy.class).put(DataFrameOptions.MINIMUM, NaNStrategy.MINIMAL);
            optionsMap.get(NaNStrategy.class).put(DataFrameOptions.MAXIMUM, NaNStrategy.MAXIMAL);
            optionsMap.get(TiesStrategy.class).put(DataFrameOptions.MINIMUM, TiesStrategy.MINIMUM);
            optionsMap.get(TiesStrategy.class).put(DataFrameOptions.MAXIMUM, TiesStrategy.MAXIMUM);
            optionsMap.get(TiesStrategy.class).put(DataFrameOptions.AVERAGE, TiesStrategy.AVERAGE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private XDataFrame<R,C> frame;

    /**
     * Constructor
     * @param frame     the frame to operate on
     */
    XDataFrameRank(XDataFrame<R,C> frame) {
        this.frame = frame;
    }

    /**
     * Returns the rank array for the values specified
     * @param values    the values to rank
     * @return          the ranks of input array
     */
    static double[] rank(double[] values) {
        final NaNStrategy nanStrategy = (NaNStrategy)optionsMap.get(NaNStrategy.class).get(DataFrameOptions.getNanStrategy());
        final TiesStrategy tieStrategy = (TiesStrategy)optionsMap.get(TiesStrategy.class).get(DataFrameOptions.getTieStrategy());
        if (nanStrategy == null) throw new DataFrameException("Unsupported NaN strategy specified: " + DataFrameOptions.getNanStrategy());
        if (tieStrategy == null) throw new DataFrameException("Unsupported tie strategy specified: " + DataFrameOptions.getTieStrategy());
        final NaturalRanking ranking = new NaturalRanking(nanStrategy, tieStrategy);
        return ranking.rank(values);
    }

    @Override
    public DataFrame<R,C> ofRows() throws DataFrameException {
        try {
            final Array<R> rowKeys = frame.rows().keyArray();
            final Array<C> colKeys = frame.cols().keyArray();
            final DataFrame<R,C> result = DataFrame.ofDoubles(rowKeys, colKeys);
            final double[] buffer = new double[colKeys.length()];
            this.frame.rows().forEach(row -> {
                row.forEachValue(v -> buffer[v.colOrdinal()] = v.getDouble());
                final double[] ranks = XDataFrameRank.rank(buffer);
                result.row(row.key()).applyDoubles(v -> ranks[v.colOrdinal()]);
            });
            return result;
        } catch (Throwable t) {
            throw new DataFrameException("DataFrame rank ofRows() failed ", t);
        }
    }


    @Override()
    public DataFrame<R,C> ofColumns() throws DataFrameException {
        try {
            final int rowCount = frame.rowCount();
            final Array<R> rowKeys = frame.rows().keyArray();
            final Array<C> colKeys = frame.cols().keyArray();
            final DataFrame<R,C> result = DataFrame.ofDoubles(rowKeys, colKeys);
            final double[] buffer = new double[rowKeys.length()];
            this.frame.cols().forEach(column -> {
                column.forEachValue(v -> buffer[v.rowOrdinal()] = v.getDouble());
                final double[] ranks = XDataFrameRank.rank(buffer);
                result.col(column.key()).applyDoubles(v -> ranks[v.rowOrdinal()]);
            });
            return result;
        } catch (Throwable t) {
            throw new DataFrameException("DataFrame rank ofColumns() failed ", t);
        }
    }

}
