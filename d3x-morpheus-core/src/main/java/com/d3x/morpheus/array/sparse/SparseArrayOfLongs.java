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
package com.d3x.morpheus.array.sparse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Predicate;

import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayCursor;
import com.d3x.morpheus.array.ArrayException;
import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBase;
import com.d3x.morpheus.array.ArrayStyle;
import com.d3x.morpheus.array.ArrayValue;
import org.eclipse.collections.api.map.primitive.MutableIntLongMap;
import org.eclipse.collections.impl.factory.primitive.IntLongMaps;
import org.eclipse.collections.impl.factory.primitive.LongSets;

/**
 * An Array implementation designed to hold a sparse array of long values
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class SparseArrayOfLongs extends ArrayBase<Long> {

    private static final long serialVersionUID = 1L;

    private int length;
    private MutableIntLongMap values;
    private long defaultValue;

    /**
     * Constructor
     * @param length    the length for this array
     * @param fillPct   the fill percent for array (0.2 implies 20% filled)
     * @param defaultValue  the default value for array
     */
    SparseArrayOfLongs(int length, float fillPct, Long defaultValue) {
        super(Long.class, ArrayStyle.SPARSE, false);
        this.length = length;
        this.defaultValue = defaultValue != null ? defaultValue : 0L;
        this.values = IntLongMaps.mutable.withInitialCapacity((int)Math.max(length * fillPct, 5d));
    }

    /**
     * Constructor
     * @param source    the source array to shallow copy
     * @param parallel  true for the parallel version
     */
    private SparseArrayOfLongs(SparseArrayOfLongs source, boolean parallel) {
        super(source.type(), ArrayStyle.SPARSE, parallel);
        this.length = source.length;
        this.defaultValue = source.defaultValue;
        this.values = source.values;
    }


    @Override
    public final int length() {
        return length;
    }


    @Override()
    public final float loadFactor() {
        return (float)values.size() / (float)length();
    }


    @Override
    public final Long defaultValue() {
        return defaultValue;
    }


    @Override
    public final Array<Long> parallel() {
        return isParallel() ? this : new SparseArrayOfLongs(this, true);
    }


    @Override
    public final Array<Long> sequential() {
        return isParallel() ? new SparseArrayOfLongs(this, false) : this;
    }


    @Override()
    public final Array<Long> copy() {
        try {
            final SparseArrayOfLongs copy = (SparseArrayOfLongs)super.clone();
            copy.values = IntLongMaps.mutable.withAll(values);
            copy.defaultValue = this.defaultValue;
            return copy;
        } catch (Exception ex) {
            throw new ArrayException("Failed to copy Array: " + this, ex);
        }
    }


    @Override()
    public final Array<Long> copy(int[] indexes) {
        var fillPct = (float)values.size() / length();
        var clone = new SparseArrayOfLongs(indexes.length, fillPct, defaultValue);
        for (int i = 0; i < indexes.length; ++i) {
            final long value = getLong(indexes[i]);
            clone.setLong(i, value);
        }
        return clone;
    }


    @Override
    public Array<Long> copy(Array<Integer> indexes) {
        var fillPct = (float)values.size() / length();
        var clone = new SparseArrayOfLongs(indexes.length(), fillPct, defaultValue);
        for (int i = 0; i < indexes.length(); ++i) {
            final long value = getLong(indexes.getInt(i));
            clone.setLong(i, value);
        }
        return clone;
    }


    @Override()
    public final Array<Long> copy(int start, int end) {
        var length = end - start;
        var fillPct = (float)values.size() / length();
        var clone = new SparseArrayOfLongs(length, fillPct, defaultValue);
        for (int i=0; i<length; ++i) {
            final long value = getLong(start+i);
            if (value != defaultValue) {
                clone.setLong(i, value);
            }
        }
        return clone;
    }


    @Override
    protected final Array<Long> sort(int start, int end, int multiplier) {
        return doSort(start, end, (i, j) -> {
            final long v1 = values.getIfAbsent(i, defaultValue);
            final long v2 = values.getIfAbsent(j, defaultValue);
            return multiplier * Long.compare(v1, v2);
        });
    }


    @Override
    public final int compare(int i, int j) {
        return Long.compare(
            values.getIfAbsent(i, defaultValue),
            values.getIfAbsent(j, defaultValue)
        );
    }


    @Override
    public final Array<Long> swap(int i, int j) {
        final long v1 = getLong(i);
        final long v2 = getLong(j);
        this.setLong(i, v2);
        this.setLong(j, v1);
        return this;
    }


    @Override
    public final Array<Long> filter(Predicate<ArrayValue<Long>> predicate) {
        int count = 0;
        var length = this.length();
        final ArrayCursor<Long> cursor = cursor();
        final Array<Long> matches = Array.of(type(), length, loadFactor());  //todo: fix the length of this filter
        for (int i=0; i<length; ++i) {
            cursor.moveTo(i);
            final boolean match = predicate.test(cursor);
            if (match) matches.setLong(count++, cursor.getLong());
        }
        return count == length ? matches : matches.copy(0, count);
    }


    @Override
    public final Array<Long> update(Array<Long> from, int[] fromIndexes, int[] toIndexes) {
        if (fromIndexes.length != toIndexes.length) {
            throw new ArrayException("The from index array must have the same length as the to index array");
        } else {
            for (int i=0; i<fromIndexes.length; ++i) {
                var toIndex = toIndexes[i];
                var fromIndex = fromIndexes[i];
                final long update = from.getLong(fromIndex);
                this.setLong(toIndex, update);
            }
        }
        return this;
    }


    @Override
    public final Array<Long> update(int toIndex, Array<Long> from, int fromIndex, int length) {
        for (int i=0; i<length; ++i) {
            final long update = from.getLong(fromIndex + i);
            this.setLong(toIndex + i, update);
        }
        return this;
    }


    @Override
    public final Array<Long> expand(int newLength) {
        this.length = newLength > length ? newLength : length;
        return this;
    }


    @Override
    public Array<Long> fill(Long value, int start, int end) {
        final long fillValue = value == null ? defaultValue : value;
        if (fillValue == defaultValue) {
            this.values.clear();
        } else {
            for (int i=start; i<end; ++i) {
                this.values.put(i, fillValue);
            }
        }
        return this;
    }


    @Override
    public final boolean isNull(int index) {
        return false;
    }


    @Override
    public final boolean isEqualTo(int index, Long value) {
        return value == null ? isNull(index) : value == values.getIfAbsent(index, defaultValue);
    }


    @Override
    public final long getLong(int index) {
        this.checkBounds(index, length);
        return values.getIfAbsent(index, defaultValue);
    }


    @Override
    public double getDouble(int index) {
        this.checkBounds(index, length);
        return values.getIfAbsent(index, defaultValue);
    }


    @Override
    public final Long getValue(int index) {
        this.checkBounds(index, length);
        return values.getIfAbsent(index, defaultValue);
    }


    @Override
    public final long setLong(int index, long value) {
        this.checkBounds(index, length);
        final long oldValue = getLong(index);
        if (value == defaultValue) {
            this.values.remove(index);
            return oldValue;
        } else {
            this.values.put(index, value);
            return oldValue;
        }
    }


    @Override
    public final Long setValue(int index, Long value) {
        this.checkBounds(index, length);
        final Long oldValue = getValue(index);
        if (value == null) {
            this.values.remove(index);
            return oldValue;
        } else {
            this.values.put(index,value);
            return oldValue;
        }
    }


    @Override
    public final int binarySearch(int start, int end, Long value) {
        int low = start;
        int high = end - 1;
        while (low <= high) {
            var midIndex = (low + high) >>> 1;
            final long midValue = getLong(midIndex);
            var result = Long.compare(midValue, value);
            if (result < 0) {
                low = midIndex + 1;
            } else if (result > 0) {
                high = midIndex - 1;
            } else {
                return midIndex;
            }
        }
        return -(low + 1);
    }


    @Override
    public final Array<Long> distinct(int limit) {
        var capacity = limit < Integer.MAX_VALUE ? limit : 100;
        var set = LongSets.mutable.withInitialCapacity(capacity);
        var builder = ArrayBuilder.of(capacity, Long.class);
        for (int i=0; i<length(); ++i) {
            final long value = getLong(i);
            if (set.add(value)) {
                builder.appendLong(value);
                if (set.size() >= limit) {
                    break;
                }
            }
        }
        return builder.toArray();
    }


    @Override
    public final Array<Long> cumSum() {
        var length = length();
        final Array<Long> result = Array.of(Long.class, length);
        result.setLong(0, values.getIfAbsent(0, defaultValue));
        for (int i=1; i<length; ++i) {
            final long prior = result.getLong(i-1);
            final long current = values.getIfAbsent(i, defaultValue);
            result.setLong(i, prior + current);
        }
        return result;
    }


    @Override
    public final void read(ObjectInputStream is, int count) throws IOException {
        for (int i=0; i<count; ++i) {
            final long value = is.readLong();
            this.setLong(i, value);
        }
    }


    @Override
    public final void write(ObjectOutputStream os, int[] indexes) throws IOException {
        for (int index : indexes) {
            final long value = getLong(index);
            os.writeLong(value);
        }
    }
}
