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

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAxisStats;
import com.d3x.morpheus.frame.DataFrameCursor;
import com.d3x.morpheus.frame.DataFrameEvent;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameGrouping;
import com.d3x.morpheus.frame.DataFrameOptions;
import com.d3x.morpheus.frame.DataFrameRow;
import com.d3x.morpheus.frame.DataFrameRows;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.stats.StatType;
import com.d3x.morpheus.stats.Stats;
import com.d3x.morpheus.util.Parallel;
import com.d3x.morpheus.util.functions.ToBooleanFunction;

/**
 * The reference implementation of DataFrameOperator that operates in the row dimension of the DataFrame.
 *
 * @param <R>       the row key type
 * @param <C>       the column key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class XDataFrameRows<R,C> extends XDataFrameAxisBase<R,C,R,C,DataFrameRow<R,C>,DataFrameRows<R,C>,DataFrameGrouping.Rows<R,C>> implements DataFrameRows<R,C> {

    /**
     * Constructor
     * @param frame     the frame to operate on
     * @param parallel  true for parallel implementation.
     */
    XDataFrameRows(XDataFrame<R,C> frame, boolean parallel) {
        super(frame, parallel, true);
    }


    @Override
    public boolean add(R key) throws DataFrameException {
        return add(key, null);
    }


    @Override
    public Array<R> addAll(Iterable<R> keys) {
        return addAll(keys, null);
    }


    @Override
    public final boolean add(R key, Function<DataFrameValue<R,C>,?> initials) {
        final XDataFrameContent<R,C> content = frame().content();
        final boolean added = content.addRow(key);
        final boolean ignoreDuplicates = DataFrameOptions.isIgnoreDuplicates();
        if (!added && !ignoreDuplicates) {
            throw new DataFrameException("Attempt to add duplicate row key: " + key);
        } else if (added) {
            final XDataFrame<R,C> frame = frame();
            final int ordinal = content.rowKeys().getOrdinalForKey(key);
            if (initials != null) {
                final DataFrameCursor<R,C> value = frame.cursor().atRow(ordinal);
                for (int i=0; i<frame.colCount(); ++i) {
                    value.atCol(i);
                    final Object result = initials.apply(value);
                    value.setValue(result);
                }
            }
            if (frame.events().isEnabled()) {
                final Array<R> keyList = Array.singleton(key);
                final DataFrameEvent event = DataFrameEvent.createRowAdd(frame, keyList);
                frame.events().fireDataFrameEvent(event);
            }
        }
        return added;
    }


    @Override
    public final Array<R> addAll(Iterable<R> keys, Function<DataFrameValue<R,C>,?> initials) {
        final XDataFrameContent<R,C> content = frame().content();
        final Array<R> added = content.addRows(keys);
        if (initials != null) {
            final DataFrameCursor<R,C> cursor = frame().cursor();
            added.forEach(rowKey -> {
                cursor.atRowKey(rowKey);
                for (int i=0; i<frame().colCount(); ++i) {
                    cursor.atCol(i);
                    Object value = initials.apply(cursor);
                    cursor.setValue(value);
                }
            });
        }
        final DataFrame<R,C> frame = frame();
        if (added.length() > 0) {
            if (frame().events().isEnabled()) {
                final DataFrameEvent event = DataFrameEvent.createRowAdd(frame, added);
                frame.events().fireDataFrameEvent(event);
            }
        }
        return added;
    }


    @Override
    public final Array<R> addAll(DataFrame<R,C> other) {
        final XDataFrame<R,C> target = frame();
        final XDataFrame<R,C> source = (XDataFrame<R,C>)other;
        final Array<R> rowKeys = target.content().addRows(source.rowKeys());
        if (rowKeys.length() == 0) {
            return rowKeys;
        } else {
            final Array<C> colKeys = target.colKeys().intersect(source.colKeys());
            XDataFrameCopy.apply(source, target, rowKeys, colKeys);
            if (target.events().isEnabled()) {
                final DataFrameEvent<R,C> event = DataFrameEvent.createRowAdd(target, rowKeys);
                target.events().fireDataFrameEvent(event);
            }
            return rowKeys;
        }
    }


    @Override
    public final DataFrameRows<R,C> parallel() {
        return isParallel() ? this : new XDataFrameRows<>(frame(), true);
    }

    @Override
    public final DataFrameRows<R,C> sequential() {
        return !isParallel() ? this : new XDataFrameRows<>(frame(), false);
    }

    @Override
    public final DataFrameAxisStats<R,R,C,R,StatType> stats() {
        return new XDataFrameAxisStats<>(frame(), isParallel(), false);
    }

    @Override @Parallel
    public final DataFrame<R,C> sort(boolean ascending) {
        return XDataFrameSorter.sortRows(frame(), ascending, isParallel());
    }

    @Override  @Parallel
    public final DataFrame<R,C> sort(boolean ascending, C key) {
        return XDataFrameSorter.sortRows(frame(), key, ascending, isParallel());
    }

    @Override @Parallel
    public final DataFrame<R,C> sort(boolean ascending, List<C> keys) {
        return XDataFrameSorter.sortRows(frame(), keys, ascending, isParallel());
    }

    @Override @Parallel
    public final DataFrame<R,C> sort(Comparator<DataFrameRow<R,C>> comparator) {
        return XDataFrameSorter.sortRows(frame(), isParallel(), comparator);
    }

    @Override @Parallel
    public final DataFrame<R,C> apply(Consumer<DataFrameRow<R,C>> consumer) {
        this.forEach(consumer);
        return frame();
    }

    @Override @Parallel
    public final DataFrame<R,C> demean(boolean inPlace) {
        if (!inPlace) {
            return frame().copy().rows().demean(true);
        } else {
            frame().rows().forEach(row -> {
                final double mean = row.stats().mean();
                row.applyDoubles(v -> v.getDouble() - mean);
            });
            return frame();
        }
    }

    @Override
    public final <X> DataFrame<X,C> mapKeys(Function<DataFrameRow<R,C>,X> mapper) {
        if (frame().rowKeys().isFilter()) {
            throw new DataFrameException("Row axis is immutable for this frame, call copy() first");
        } else {
            final XDataFrameRow<R,C> row = new XDataFrameRow<>(frame(), false);
            return frame().mapRowKeys((key, ordinal) -> mapper.apply(row.moveTo(ordinal)));
        }
    }

    @Override
    public final DataFrame<R,StatType> describe(StatType... stats) {
        final Array<R> rowKeys = filter(DataFrameRow::isNumeric).keyArray();
        final Array<StatType> statKeys = Array.of(StatType.class, stats);
        final DataFrame<R,StatType> result = DataFrame.ofDoubles(rowKeys, statKeys);
        final DataFrameCursor<R,StatType> cursor = result.cursor();
        this.filter(DataFrameRow::isNumeric).forEach(row -> {
            final R key = row.key();
            cursor.atRowKey(key);
            final Stats<Double> rowStats = row.stats();
            for (int j = 0; j < statKeys.length(); ++j) {
                final StatType stat = statKeys.getValue(j);
                final double value = stat.apply(rowStats);
                cursor.atCol(j).setDouble(value);
            }
        });
        return result;
    }


    @Override
    public final void forEachValue(R key, Consumer<DataFrameValue<R, C>> consumer) {
        final int colCount = frame().colCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atRowKey(key);
        for (int j=0; j<colCount; ++j) {
            cursor.atCol(j);
            consumer.accept(cursor);
        }
    }


    @Override
    public final DataFrame<R,C> applyBooleans(R key, ToBooleanFunction<DataFrameValue<R,C>> function) {
        final int colCount = frame().colCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atRowKey(key);
        for (int j=0; j<colCount; ++j) {
            final boolean value = function.applyAsBoolean(cursor.atCol(j));
            cursor.setBoolean(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyInts(R key, ToIntFunction<DataFrameValue<R,C>> function) {
        final int colCount = frame().colCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atRowKey(key);
        for (int j=0; j<colCount; ++j) {
            final int value = function.applyAsInt(cursor.atCol(j));
            cursor.setInt(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyLongs(R key, ToLongFunction<DataFrameValue<R,C>> function) {
        final int colCount = frame().colCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atRowKey(key);
        for (int j=0; j<colCount; ++j) {
            final long value = function.applyAsLong(cursor.atCol(j));
            cursor.setLong(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyDoubles(R key, ToDoubleFunction<DataFrameValue<R,C>> function) {
        final int colCount = frame().colCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atRowKey(key);
        for (int j=0; j<colCount; ++j) {
            final double value = function.applyAsDouble(cursor.atCol(j));
            cursor.setDouble(value);
        }
        return frame();
    }


    @Override
    public final <T> DataFrame<R,C> applyValues(R key, Function<DataFrameValue<R,C>, T> function) {
        final int colCount = frame().colCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atRowKey(key);
        for (int j=0; j<colCount; ++j) {
            final T value = function.apply(cursor.atCol(j));
            cursor.setValue(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R, C> remove(Predicate<DataFrameRow<R,C>> predicate) {
        return frame().content().isColumnStore() ? select(predicate).copy() : select(predicate);
    }

    @Override
    public final boolean getBoolean(R rowKey, int colOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final boolean getBooleanAt(int rowOrdinal, C colKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final int getInt(R rowKey, int colOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final int getIntAt(int rowOrdinal, C colKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final long getLong(R rowKey, int colOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final long getLongAt(int rowOrdinal, C colKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final double getDouble(R rowKey, int colOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final double getDoubleAt(int rowOrdinal, C colKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValue(R rowKey, int colOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValueAt(int rowOrdinal, C colKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final boolean setBoolean(R rowKey, int colOrdinal, boolean value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final boolean setBooleanAt(int rowOrdinal, C colKey, boolean value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setInt(R rowKey, int colOrdinal, int value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setIntAt(int rowOrdinal, C colKey, int value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLong(R rowKey, int colOrdinal, long value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLongAt(int rowOrdinal, C colKey, long value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDouble(R rowKey, int colOrdinal, double value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDoubleAt(int rowOrdinal, C colKey, double value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValue(R rowKey, int colOrdinal, V value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndex(rowKey);
        final int colIndex = data.colIndexAt(colOrdinal);
        return data.valueAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValueAt(int rowOrdinal, C colKey, V value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowIndexAt(rowOrdinal);
        final int colIndex = data.colIndex(colKey);
        return data.valueAt(rowIndex, colIndex, value);
    }
}
