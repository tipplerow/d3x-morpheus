/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.pipeline;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.stats.Mean;
import com.d3x.morpheus.stats.Percentile;
import com.d3x.morpheus.stats.StdDev;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.DataVector;
import com.d3x.morpheus.vector.DataVectorView;

/**
 * Defines an in-place transformation of a DataVector.
 *
 * @author Scott Shaffer
 */
public interface DataPipeline {
    /**
     * Applies the transformation defined by this pipeline to a data
     * vector; the vector is modified in place.
     *
     * @param <K>    the runtime type for the DataVector keys.
     * @param vector the vector to transform.
     *
     * @return the input vector, as modified by this pipeline, for
     * operator chaining.
     */
    <K> DataVector<K> apply(DataVector<K> vector);

    /**
     * Creates a copy of a vector view, applies the transformation defined
     * by this pipeline to the copy, and returns the transformed vector in
     * a new DoubleSeries.
     *
     * @param <K>        the runtime type for the keys.
     * @param keyClass   the runtime class for the keys.
     * @param vectorView the vector view to transform.
     *
     * @return the transformed vector.
     */
    default <K> DoubleSeries<K> apply(Class<K> keyClass, DataVectorView<K> vectorView) {
        return DoubleSeries.copyOf(keyClass, apply(DataVector.copy(vectorView)));
    }

    /**
     * Applies this size-preserving pipeline along a margin of a data
     * frame (in place).
     *
     * @param frame  the frame on which to operate.
     * @param margin the index of the axis of application: 1 for rows,
     *               2 for columns (as in the R apply() function).
     *
     * @return the transformed input frame, for operator chaining.
     *
     * @throws RuntimeException unless this is a size-preserving pipeline
     * and the margin is valid.
     */
    default <R,C> DataFrame<R,C> apply(DataFrame<R,C> frame, int margin) {
        switch (margin) {
            case 1:
                return byrow(frame);

            case 2:
                return bycol(frame);

            default:
                throw new MorpheusException("Invalid data frame margin.");
        }
    }

    /**
     * Applies this size-preserving pipeline to each row in a data frame
     * (in place).
     *
     * @param frame the frame on which to operate.
     *
     * @return the transformed input frame, for operator chaining.
     *
     * @throws RuntimeException unless this is a size-preserving pipeline.
     */
    default <R,C> DataFrame<R,C> byrow(DataFrame<R,C> frame) {
        if (!isSizePreserving())
            throw new MorpheusException("Cannot apply a size-altering pipeline to a DataFrame.");

        frame.rows().stream().forEach(this::apply);
        return frame;
    }

    /**
     * Applies this size-preserving pipeline to each column in a data frame
     * (in place).
     *
     * @param frame the frame on which to operate.
     *
     * @return the transformed input frame, for operator chaining.
     *
     * @throws RuntimeException unless this is a size-preserving pipeline.
     */
    default <R,C> DataFrame<R,C> bycol(DataFrame<R,C> frame) {
        if (!isSizePreserving())
            throw new MorpheusException("Cannot apply a size-altering pipeline to a DataFrame.");

        frame.cols().stream().forEach(this::apply);
        return frame;
    }

    /**
     * Encodes this pipeline in a string.
     *
     * @return a string encoding of this pipeline which may be decoded
     * by a pipeline parser.
     */
    String encode();

    /**
     * Identifies <em>local</em> transformations: the result of an
     * element transformation depends only on the initial value of
     * that element and is independent of all other element values.
     *
     * @return {@code true} iff this is a local pipeline.
     */
    boolean isLocal();

    /**
     * Identifies transformations that do not add or remove elements
     * from the input vector.
     *
     * @return {@code true} iff this is a size-preserving pipeline.
     */
    boolean isSizePreserving();

    /**
     * A local, size-preserving pipeline that replaces each element
     * with its absolute value.
     */
    DataPipeline abs = local("abs()", Math::abs);

    /**
     * A non-local, size-preserving pipeline that subtracts the mean
     * from each element in the DataVector, resulting in a transformed
     * vector with zero mean.
     */
    DataPipeline demean = new DataPipeline() {
        @Override
        public <K> DataVector<K> apply(DataVector<K> vector) {
            double mean = new Mean().compute(vector);
            return subtract(mean).apply(vector);
        }

        @Override
        public String encode() {
            return "demean()";
        }

        @Override
        public boolean isSizePreserving() {
            return true;
        }

        @Override
        public boolean isLocal() {
            return false;
        }
    };

    /**
     * A local, size-preserving pipeline that applies the exponential function
     * to each element.
     */
    DataPipeline exp = local("exp()", Math::exp);

    /**
     * A local, size-preserving pipeline that flips the sign of each element.
     */
    DataPipeline flip = local("flip()", value -> -value);

    /**
     * The identity pipeline: all elements are unchanged.
     */
    DataPipeline identity = new DataPipeline() {
        @Override
        public <K> DataVector<K> apply(DataVector<K> vector) {
            // Do nothing...
            return vector;
        }

        @Override
        public String encode() {
            return "identity()";
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        @Override
        public boolean isSizePreserving() {
            return true;
        }
    };

    /**
     * A local, size-preserving pipeline that replaces each element
     * with its reciprocal.
     */
    DataPipeline invert = local("invert()", x -> 1.0 / x);

    /**
     * A local, size-preserving pipeline that replaces each element
     * with its natural logarithm.
     */
    DataPipeline log = local("log()", Math::log);

    /**
     * A non-local, size-preserving pipeline that rescales the vector
     * into a normalized unit vector.
     */
    DataPipeline normalize = new DataPipeline() {
        @Override
        public <K> DataVector<K> apply(DataVector<K> vector) {
            return divide(vector.norm2()).apply(vector);
        }

        @Override
        public String encode() {
            return "normalize()";
        }

        @Override
        public boolean isSizePreserving() {
            return true;
        }

        @Override
        public boolean isLocal() {
            return false;
        }
    };

    /**
     * A local, size-preserving pipeline that replaces each element
     * with its sign: {@code -1.0} if the element is negative (by an
     * amount greater than the default DoubleComparator tolerance),
     * {@code +1.0} if the element is positive (by an amount greater
     * than the default tolerance), or {@code 0.0} if the element is
     * equal to zero (within the default tolerance.
     */
    DataPipeline sign = local("sign()", DoubleComparator.DEFAULT::sign);

    /**
     * A local, size-preserving pipeline that replaces each element
     * with its square root.
     */
    DataPipeline sqrt = local("sqrt()", Math::sqrt);

    /**
     * A local, size-preserving pipeline that squares each element.
     */
    DataPipeline square = local("square()", x -> x * x);

    /**
     * A non-local, size-preserving pipeline that subtracts the mean
     * from each element in the DataVector and divides each element by
     * the standard deviation, resulting in a transformed vector with
     * zero mean and unit variance.
     */
    DataPipeline standardize = new DataPipeline() {
        @Override
        public <K> DataVector<K> apply(DataVector<K> vector) {
            double sdev = new StdDev(true).compute(vector);
            return composite(demean, divide(sdev)).apply(vector);
        }

        @Override
        public String encode() {
            return "standardize()";
        }

        @Override
        public boolean isSizePreserving() {
            return true;
        }

        @Override
        public boolean isLocal() {
            return false;
        }
    };

    /**
     * Returns a local, size-preserving pipeline that adds a constant
     * value to each element.
     *
     * @param addend the constant value to add to each element.
     *
     * @return a local, size-preserving pipeline that adds the given
     * value to each element.
     */
    static DataPipeline add(double addend) {
        return local(String.format("add(%s)", addend), element -> element + addend);
    }

    /**
     * Returns a local, size-preserving pipeline that bounds each element
     * on a fixed interval.
     *
     * @param lower the lower bound of the interval.
     * @param upper the upper bound of the interval.
     *
     * @return a bounding pipeline with the specified interval.
     *
     * @throws RuntimeException unless the interval is valid (the lower bound
     * is less than or equal to the upper bound).
     */
    static DataPipeline bound(double lower, double upper) {
        if (lower > upper)
            throw new MorpheusException("Invalid bounding interval: [%s, %s].", lower, upper);
        else
            return local(
                    String.format("bound(%s, %s)", lower, upper),
                    element -> Math.max(lower, Math.min(upper, element)));
    }

    /**
     * Returns a composite pipeline composed of a series of individual
     * pipelines.
     *
     * @param pipelines the pipelines to compose the composite.
     *
     * @return the composite of the specified pipelines.
     */
    static DataPipeline composite(DataPipeline... pipelines) {
        return composite(List.of(pipelines));
    }

    /**
     * Returns a composite pipeline composed of a series of individual
     * pipelines.
     *
     * @param pipelines the pipelines to compose the composite.
     *
     * @return the composite of the specified pipelines (the identity
     * pipeline, if the collection is empty; the single pipeline itself
     * if the list contains only a single pipeline).
     */
    static DataPipeline composite(List<DataPipeline> pipelines) {
        switch (pipelines.size()) {
            case 0:
                return identity;

            case 1:
                return pipelines.get(0);

            default:
                return CompositePipeline.of(pipelines);
        }
    }

    /**
     * Returns a local, size-preserving pipeline that divides each
     * element by a constant factor.
     *
     * @param divisor the constant factor to divide each element.
     *
     * @return a local, size-preserving pipeline that divides each
     * element by the given factor.
     */
    static DataPipeline divide(double divisor) {
        return local(String.format("divide(%s)", divisor), element -> element / divisor);
    }

    /**
     * Creates a new non-local, size-preserving pipeline that rescales
     * the elements of a vector to a target <em>leverage</em>: the sum
     * of the absolute values in the vector.
     *
     * @param target the target leverage.
     *
     * @return a leverage pipeline with the specified target leverage.
     *
     * @throws RuntimeException unless the target leverage is positive.
     */
    static DataPipeline lever(double target) {
        if (!DoubleComparator.DEFAULT.isPositive(target))
            throw new MorpheusException("Target leverage must be positive.");

        return new DataPipeline() {
            @Override
            public <K> DataVector<K> apply(DataVector<K> vector) {
                double norm1 = vector.norm1();

                if (DoubleComparator.DEFAULT.isPositive(norm1))
                    return multiply(target / norm1).apply(vector);
                else
                    throw new MorpheusException("Cannot apply target leverage to a vector with zero norm.");
            }

            @Override
            public String encode() {
                return String.format("lever(%s)", target);
            }

            @Override
            public boolean isSizePreserving() {
                return true;
            }

            @Override
            public boolean isLocal() {
                return false;
            }
        };
    }

    /**
     * Creates a new local, size-preserving pipeline for a given operator.
     *
     * @param encoding the string encoding for the pipeline.
     * @param operator the unary function that transforms the element values.
     *
     * @return a new local, size-preserving pipeline with the given operator.
     */
    static DataPipeline local(String encoding, DoubleUnaryOperator operator) {
        return LocalPipeline.of(encoding, operator);
    }

    /**
     * Returns a local, size-preserving pipeline that multiplies each
     * element by a constant factor.
     *
     * @param factor the constant factor to multiply each element.
     *
     * @return a local, size-preserving pipeline that multiplies each
     * element by the given factor.
     */
    static DataPipeline multiply(double factor) {
        return local(String.format("multiply(%s)", factor), element -> element * factor);
    }

    /**
     * Returns a local, size-preserving pipeline that raises each element
     * to a power.
     *
     * @param exponent the exponent of the power function.
     *
     * @return a local, size-preserving pipeline that raises each element
     * to the specified power.
     */
    static DataPipeline pow(double exponent) {
        return local(String.format("pow(%s)", exponent), element -> Math.pow(element, exponent));
    }

    /**
     * Returns a local, size-preserving pipeline that replaces missing
     * ({@code Double.NaN}) values with a fixed value.
     *
     * @param replacement the value to assign missing elements.
     *
     * @return a local, size-preserving pipeline that replaces missing
     * values with the specified replacement.
     */
    static DataPipeline replaceNaN(double replacement) {
        return local(String.format("replaceNaN(%s)", replacement), element -> Double.isNaN(element) ? replacement : element);
    }

    /**
     * Returns a local, size-preserving pipeline that subtracts a constant
     * value from each element.
     *
     * @param subtrahend the constant value to subtract from each element.
     *
     * @return a local, size-preserving pipeline that subtracts the
     * given value from each element.
     */
    static DataPipeline subtract(double subtrahend) {
        return local(String.format("subtract(%s)", subtrahend), element -> element - subtrahend);
    }

    /**
     * Returns a non-local, size-preserving pipeline that pulls outliers
     * into a location defined by a quantile value.  With a quantile value
     * of {@code 0.05}, for example, elements below the 5th percentile will
     * be raised to the 5th percentile and those above the 95th percentile
     * will be lowered to the 95th percentile.
     *
     * @param quantile the (fractional) quantile value that defines the
     *                 lower and upper bounds for the elements.
     *
     * @return a trimming pipeline for the specified quantile.
     *
     * @throws RuntimeException unless the quantile is within the valid range
     * {@code [0.0, 0.5]}.
     */
    static DataPipeline trim(double quantile) {
        DoubleComparator comparator = DoubleComparator.DEFAULT;

        if (comparator.isNegative(quantile))
            throw new MorpheusException("Quantile must be non-negative.");

        if (comparator.isZero(quantile))
            return identity;

        if (comparator.compare(quantile, 0.5) > 0)
            throw new MorpheusException("Quantile must not exceed one-half.");

        return new DataPipeline() {
            @Override
            public <K> DataVector<K> apply(DataVector<K> vector) {
                // Percentile takes fractional (quantile) values...
                double lower = new Percentile(quantile).compute(vector);
                double upper = new Percentile(1.0 - quantile).compute(vector);

                return bound(lower, upper).apply(vector);
            }

            @Override
            public String encode() {
                return String.format("trim(%s)", quantile);
            }

            @Override
            public boolean isSizePreserving() {
                return true;
            }

            @Override
            public boolean isLocal() {
                return false;
            }
        };
    }
}
