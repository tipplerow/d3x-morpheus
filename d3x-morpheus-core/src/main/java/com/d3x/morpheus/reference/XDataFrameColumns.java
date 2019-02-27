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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAxisStats;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameColumns;
import com.d3x.morpheus.frame.DataFrameCursor;
import com.d3x.morpheus.frame.DataFrameEvent;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameGrouping;
import com.d3x.morpheus.frame.DataFrameOptions;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.stats.StatType;
import com.d3x.morpheus.stats.Stats;
import com.d3x.morpheus.util.Asserts;
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
class XDataFrameColumns<R,C> extends XDataFrameAxisBase<C,R,R,C,DataFrameColumn<R,C>,DataFrameColumns<R,C>,DataFrameGrouping.Cols<R,C>> implements DataFrameColumns<R,C> {


    /**
     * Constructor
     * @param frame     the frame to operate on
     * @param parallel  true for parallel implementation
     */
    XDataFrameColumns(XDataFrame<R,C> frame, boolean parallel) {
        super(frame, parallel, false);
    }


    @Override
    public final DataFrameColumns<R,C> parallel() {
        return isParallel() ? this : new XDataFrameColumns<>(frame(), true);
    }


    @Override
    public final XDataFrameColumns<R,C> sequential() {
        return !isParallel() ? this : new XDataFrameColumns<>(frame(), false);
    }


    @Override
    public final DataFrameColumn<R,C> add(C key, Iterable<?> values) {
        return addColumns().andThen(notifyEvent()).andThen(keys -> frame().col(key)).apply(columnMap -> {
            if (!contains(key)) {
                columnMap.put(key, values);
            }
        });
    }


    @Override
    public final DataFrameColumn<R,C> add(C key, Class<?> type) {
        return addColumns().andThen(notifyEvent()).andThen(keys -> frame().col(key)).apply(columnMap -> {
            if (!contains(key)) {
                final int rowCapacity = frame().content().rowCapacity();
                columnMap.put(key, Array.of(type, rowCapacity));
            }
        });
    }


    @Override
    public <T> DataFrame<R,C> add(C key, Class<T> type, Function<DataFrameValue<R,C>,T> seeder) {
        return addColumns().andThen(seed(seeder)).andThen(notifyEvent()).andThen(x -> frame()).apply(columnMap -> {
            if (!contains(key)) {
                final int rowCapacity = frame().content().rowCapacity();
                columnMap.put(key, Array.of(type, rowCapacity));
            }
        });
    }


    @Override
    public final Array<C> addAll(Iterable<C> colKeys, Class<?> type) {
        return addColumns().andThen(notifyEvent()).apply(columnMap -> colKeys.forEach(colKey -> {
            if (!contains(colKey)) {
                columnMap.put(colKey, Array.of(type, frame().rowCount()));
            }
        }));
    }


    @Override
    public final Array<C> addAll(Consumer<Map<C,Iterable<?>>> consumer) {
        return addColumns().andThen(notifyEvent()).apply(consumer);
    }


    @Override
    public final Array<C> addAll(DataFrame<R,C> other) {
        final XDataFrame<R,C> target = frame();
        final XDataFrame<R,C> source = (XDataFrame<R,C>)other;
        final ArrayBuilder<C> builder = ArrayBuilder.of(source.colCount(), source.cols().keyType());
        final boolean ignoreDuplicates = DataFrameOptions.isIgnoreDuplicates();
        source.cols().forEach(column -> {
            final C colKey = column.key();
            final boolean exists = target.cols().contains(colKey);
            if (exists && !ignoreDuplicates) {
                throw new DataFrameException("A column for key already exists in this frame: " + colKey);
            } else if (!exists) {
                final Class<?> type = source.content().colType(colKey);
                final Array<?> colData = Array.of(type, target.rowCount());
                target.cols().add(colKey, colData);
                builder.add(colKey);
            }
        });
        final Array<C> colKeys = builder.toArray();
        if (colKeys.length() > 0) {
            final Array<R> rowKeys = source.rowKeys().intersect(target.rowKeys());
            XDataFrameCopy.apply(source, target, rowKeys, colKeys);
            notifyEvent().apply(colKeys);
        }
        return colKeys;
    }


    @Override
    public final DataFrameAxisStats<C,R,C,C,StatType> stats() {
        return new XDataFrameAxisStats<>(frame(), isParallel(), true);
    }


    @Override
    @SafeVarargs
    public final DataFrame<Double,C> hist(int binCount, C... columns) {
        return hist(binCount, (columns == null || columns.length == 0) ? keyArray() : Array.of(columns));
    }


    @Override
    public final DataFrame<Double,C> hist(int binCount, Iterable<C> columns) {
        Asserts.check(binCount > 0, "The bin count must be > 0");
        final DataFrame<R,C> filter = frame().cols().select(columns);
        final double minValue = filter.stats().min();
        final double maxValue = filter.stats().max();
        final double stepSize = (maxValue - minValue) / binCount;
        final Range<Double> rowKeys = Range.of(minValue, maxValue + stepSize, stepSize);
        final DataFrame<Double,C> hist = DataFrame.ofInts(rowKeys, columns);
        final DataFrameCursor<Double,C> cursor = hist.cursor();
        final XDataFrameColumn<R,C> column = new XDataFrameColumn<>(frame(), false);
        columns.forEach(colKey -> {
            column.moveTo(colKey);
            cursor.atColKey(colKey);
            column.forEachValue(v -> {
                final double value = v.getDouble();
                hist.rows().lowerKey(value).ifPresent(lowerKey -> {
                    final int rowOrdinal = hist.rows().ordinal(lowerKey);
                    final int count = cursor.atRow(rowOrdinal).getInt();
                    cursor.setInt(count + 1);
                });
            });
        });
        return hist;
    }


    @Override @Parallel
    public final DataFrame<R,C> sort(boolean ascending) {
        return XDataFrameSorter.sortCols(frame(), ascending, isParallel());
    }


    @Override @Parallel
    public final DataFrame<R,C> sort(boolean ascending, R key) {
        return XDataFrameSorter.sortCols(frame(), key, ascending, isParallel());
    }


    @Override @Parallel
    public final DataFrame<R,C> sort(boolean ascending, List<R> keys) {
        return XDataFrameSorter.sortCols(frame(), keys, ascending, isParallel());
    }


    @Override @Parallel
    public final DataFrame<R,C> sort(Comparator<DataFrameColumn<R,C>> comparator) {
        return XDataFrameSorter.sortCols(frame(), isParallel(), comparator);
    }

    @Override @Parallel
    public final DataFrame<R,C> apply(Consumer<DataFrameColumn<R,C>> consumer) {
        this.forEach(consumer);
        return frame();
    }

    @Override @Parallel
    public final DataFrame<R,C> demean(boolean inPlace) {
        if (!inPlace) {
            return frame().copy().cols().demean(true);
        } else {
            frame().cols().forEach(column -> {
                final ArrayType type = ArrayType.of(column.typeInfo());
                if (type.isInteger()) {
                    final int mean = column.stats().mean().intValue();
                    column.applyInts(v -> v.getInt() - mean);
                } else if (type.isLong()) {
                    final long mean = column.stats().mean().longValue();
                    column.applyLongs(v -> v.getLong() - mean);
                } else {
                    final double mean = column.stats().mean();
                    column.applyDoubles(v -> v.getDouble() - mean);
                }
            });
            return frame();
        }
    }


    @Override
    public final <X> DataFrame<R,X> mapKeys(Function<DataFrameColumn<R,C>,X> mapper) {
        if (frame().colKeys().isFilter()) {
            throw new DataFrameException("Column axis is immutable for this frame, call copy() first");
        } else {
            final XDataFrameColumn<R,C> column = new XDataFrameColumn<>(frame(), false);
            return frame().mapColKeys((key, ordinal) -> mapper.apply(column.moveTo(ordinal)));
        }
    }


    /**
     * Returns a function to seed values for a set of columns defined by the input keys
     * @param seeder        the function that will seed data for columns
     * @return              the seeder function
     */
    private <T> Function<Array<C>,Array<C>> seed(Function<DataFrameValue<R,C>,T> seeder) {
        final XDataFrameColumn<R,C> column = new XDataFrameColumn<>(frame(), false);
        return keys -> {
            keys.forEach(key -> {
                column.moveTo(key);
                column.applyValues(seeder);
            });
            return keys;
        };
    }

    /**
     * Returns a function that will publish notification events when called
     * @return      the function to send notification events
     */
    private Function<Array<C>,Array<C>> notifyEvent() {
        return keys -> {
            if (keys.length() > 0 && frame().events().isEnabled()) {
                final XDataFrame<R,C> frame = frame();
                final DataFrameEvent<R,C> event = DataFrameEvent.createColumnAdd(frame, keys);
                frame.events().fireDataFrameEvent(event);
            }
            return keys;
        };
    }

    /**
     * Returns a function that will add columns to the frame
     * @return  the function to add columns to the frame
     */
    private Function<Consumer<Map<C,Iterable<?>>>,Array<C>> addColumns() {
        return consumer -> {
            final Class<C> keyType = keyType();
            final Map<C,Iterable<?>> columnMap = new LinkedHashMap<>();
            consumer.accept(columnMap);
            final ArrayBuilder<C> builder = ArrayBuilder.of(columnMap.size(), keyType);
            columnMap.keySet().forEach(colKey -> {
                final Iterable<?> values = columnMap.get(colKey);
                final XDataFrameContent<R,C> content = frame().content();
                final boolean added = content.addColumn(colKey, values);
                if (added) {
                    builder.add(colKey);
                }
            });
            return builder.toArray();
        };
    }


    @Override
    public final DataFrame<C,StatType> describe(StatType... stats) {
        final Array<StatType> statKeys = Array.of(stats);
        final Array<C> colKeys = filter(DataFrameColumn::isNumeric).keyArray();
        final DataFrame<C,StatType> result = DataFrame.ofDoubles(colKeys, statKeys);
        final DataFrameCursor<C,StatType> cursor = result.cursor();
        this.filter(DataFrameColumn::isNumeric).forEach(column -> {
            final C key = column.key();
            cursor.atRowKey(key);
            final Stats<Double> colStats = column.stats();
            for (int j = 0; j < statKeys.length(); ++j) {
                final StatType stat = statKeys.getValue(j);
                final double value = stat.apply(colStats);
                cursor.atCol(j).setDouble(value);
            }
        });
        return result;
    }


    @Override
    public final void forEachValue(C key, Consumer<DataFrameValue<R, C>> consumer) {
        final int rowCount = frame().rowCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atColKey(key);
        for (int i=0; i<rowCount; ++i) {
            cursor.atRow(i);
            consumer.accept(cursor);
        }
    }


    @Override
    public final DataFrame<R,C> applyBooleans(C key, ToBooleanFunction<DataFrameValue<R,C>> function) {
        final int rowCount = frame().rowCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atColKey(key);
        for (int i=0; i<rowCount; ++i) {
            final boolean value = function.applyAsBoolean(cursor.atRow(i));
            cursor.setBoolean(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyInts(C key, ToIntFunction<DataFrameValue<R,C>> function) {
        final int rowCount = frame().rowCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atColKey(key);
        for (int i=0; i<rowCount; ++i) {
            final int value = function.applyAsInt(cursor.atRow(i));
            cursor.setInt(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyLongs(C key, ToLongFunction<DataFrameValue<R,C>> function) {
        final int rowCount = frame().rowCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atColKey(key);
        for (int i=0; i<rowCount; ++i) {
            final long value = function.applyAsLong(cursor.atRow(i));
            cursor.setLong(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyDoubles(C key, ToDoubleFunction<DataFrameValue<R,C>> function) {
        final int rowCount = frame().rowCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atColKey(key);
        for (int i=0; i<rowCount; ++i) {
            final double value = function.applyAsDouble(cursor.atRow(i));
            cursor.setDouble(value);
        }
        return frame();
    }


    @Override
    public final <T> DataFrame<R,C> applyValues(C key, Function<DataFrameValue<R,C>, T> function) {
        final int rowCount = frame().rowCount();
        final DataFrameCursor<R,C> cursor = frame().cursor().atColKey(key);
        for (int i=0; i<rowCount; ++i) {
            final T value = function.apply(cursor.atRow(i));
            cursor.setValue(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R, C> remove(Predicate<DataFrameColumn<R,C>> predicate) {
        return frame().content().isColumnStore() ? select(predicate) : select(predicate).copy();
    }


    @Override
    public final boolean getBoolean(C colKey, int rowOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final boolean getBooleanAt(int colOrdinal, R rowKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final int getInt(C colKey, int rowOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final int getIntAt(int colOrdinal, R rowKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final long getLong(C colKey, int rowOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final long getLongAt(int colOrdinal, R rowKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final double getDouble(C colKey, int rowOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final double getDoubleAt(int colOrdinal, R rowKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValue(C colKey, int rowOrdinal) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValueAt(int colOrdinal, R rowKey) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final boolean setBoolean(C colKey, int rowOrdinal, boolean value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final boolean setBooleanAt(int colOrdinal, R rowKey, boolean value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setInt(C colKey, int rowOrdinal, int value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setIntAt(int colOrdinal, R rowKey, int value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLong(C colKey, int rowOrdinal, long value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLongAt(int colOrdinal, R rowKey, long value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDouble(C colKey, int rowOrdinal, double value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDoubleAt(int colOrdinal, R rowKey, double value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValue(C colKey, int rowOrdinal, V value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinateAt(rowOrdinal);
        final int colIndex = data.colCoordinate(colKey);
        return data.valueAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValueAt(int colOrdinal, R rowKey, V value) {
        final XDataFrameContent<R,C> data = frame().content();
        final int rowIndex = data.rowCoordinate(rowKey);
        final int colIndex = data.colCoordinateAt(colOrdinal);
        return data.valueAt(rowIndex, colIndex, value);
    }
}
