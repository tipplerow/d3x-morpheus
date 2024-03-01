/*
 * Copyright (C) 2014-2023 Talos Trading - All Rights Reserved
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
import com.d3x.morpheus.stats.StdDev;
import com.d3x.morpheus.stats.Sum;
import com.d3x.morpheus.stats.SumSquares;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.DoubleInterval;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.DataVector;
import com.d3x.morpheus.vector.DataVectorView;

/**
 * Defines an in-place transformation of vector data.
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
     * Applies the transformation defined by this pipeline to a vector;
     * the vector is modified in place.
     *
     * @param vector the vector to transform.
     *
     * @return the input vector, as modified by this pipeline, for
     * operator chaining.
     *
     * @throws RuntimeException unless this is a size-preserving pipeline.
     */
    D3xVector apply(D3xVector vector);

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
        return switch (margin) {
            case 1 -> byrow(frame);
            case 2 -> bycol(frame);
            default -> throw new MorpheusException("Invalid data frame margin.");
        };
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
     * A local, size-preserving pipeline that replaces each element
     * with its cube root.
     */
    DataPipeline cbrt = local("cbrt()", Math::cbrt);

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
        public D3xVector apply(D3xVector vector) {
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
     * A local, size-preserving pipeline that replaces each element {@code x}
     * with {@code exp(x) - 1.0}.
     */
    DataPipeline expm1 = local("expm1()", Math::expm1);

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
        public D3xVector apply(D3xVector vector) {
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
     * A local, size-preserving pipeline that replaces each element
     * {@code x} with {@code log(1.0 + x)}.
     */
    DataPipeline log1p = local("log1p()", Math::log1p);

    /**
     * A non-local, size-preserving pipeline that rescales the vector
     * so that the elements sum to one.
     */
    DataPipeline normalize = new DataPipeline() {
        @Override
        public <K> DataVector<K> apply(DataVector<K> vector) {
            var sum = new Sum().compute(vector);
            return divide(sum).apply(vector);
        }

        @Override
        public D3xVector apply(D3xVector vector) {
            return vector.normalize();
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
     * A non-local, size-preserving pipeline that ranks elements onto
     * the interval {@code [0.0, 1.0]}.
     */
    DataPipeline rank01 = rank(0.0, 1.0);

    /**
     * A non-local, size-preserving pipeline that ranks elements onto
     * the interval {@code [-1.0, 1.0]}.
     */
    DataPipeline rank11 = rank(-1.0, 1.0);

    /**
     * A local, size-preserving pipeline that replaces each element
     * with its sign: {@code -1.0} if the element is negative (by an
     * amount greater than the default DoubleComparator tolerance),
     * {@code +1.0} if the element is positive (by an amount greater
     * than the default tolerance), or {@code 0.0} if the element is
     * equal to zero (within the default tolerance).
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
        public D3xVector apply(D3xVector vector) {
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
     * A non-local, size-preserving pipeline that rescales the vector
     * into a unit vector (with 2-norm equal to one).
     */
    DataPipeline unitize = new DataPipeline() {
        @Override
        public <K> DataVector<K> apply(DataVector<K> vector) {
            return divide(vector.norm2()).apply(vector);
        }

        @Override
        public D3xVector apply(D3xVector vector) {
            var norm2 = Math.sqrt(new SumSquares().compute(vector));
            return divide(norm2).apply(vector);
        }

        @Override
        public String encode() {
            return "unitize()";
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
     * Returns a local, size-preserving pipeline that annualizes volatility
     * measured over a period less than one year.
     *
     * @param perYear the number of measurement periods in one year.
     *
     * @return a local, size-preserving pipeline that annualizes volatility
     * measured over the specific period.
     */
    static DataPipeline annualize(double perYear) {
        return local(String.format("annualize(%s)", perYear), element -> element * Math.sqrt(perYear));
    }

    /**
     * Returns a local, size-preserving pipeline that bounds each element
     * on a fixed interval.
     *
     * @param interval the bounding interval (acting as a closed interval).
     *
     * @return a bounding pipeline with the specified interval.
     */
    static DataPipeline bound(DoubleInterval interval) {
        return bound(interval.getLower(), interval.getUpper());
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
        return switch (pipelines.size()) {
            case 0 -> identity;
            case 1 -> pipelines.get(0);
            default -> CompositePipeline.of(pipelines);
        };
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
     * Returns a non-local, size-preserving pipeline that performs Huberization:
     * elements are bound between {@code M - c * MAD} and {@code M + c * MAD}
     * where {@code M} is the sample median, {@code MAD} is the median absolute
     * deviation, and {@code c} is the Huberization constant.  This is a robust
     * alternative to Winsorization.
     *
     * @param constant the Huberization constant described above.
     *
     * @return a Huberization pipeline with the specified constant.
     */
    static DataPipeline huber(double constant) {
        return new HuberPipeline(constant);
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
        return new LeverPipeline(target);
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
     * Returns a local, size-preserving pipeline that applies a lower
     * bound to each element.
     *
     * @param lower the lower bound to apply.
     *
     * @return a maximum pipeline with the specified bound.
     */
    static DataPipeline pmax(double lower) {
        return local(String.format("pmax(%s)", lower), element -> Math.max(lower, element));
    }

    /**
     * Returns a local, size-preserving pipeline that applies an upper
     * bound to each element.
     *
     * @param upper the upper bound to apply.
     *
     * @return a minimum pipeline with the specified bound.
     */
    static DataPipeline pmin(double upper) {
        return local(String.format("pmin(%s)", upper), element -> Math.min(upper, element));
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
     * Returns a non-local, size-preserving pipeline that ranks elements
     * onto a continuous interval.
     *
     * @param lower the minimum rank.
     * @param upper the maximum rank.
     *
     * @return a non-local, size-preserving pipeline that ranks elements
     * onto the specified interval.
     *
     * @throws RuntimeException unless the maximum rank is greater than
     * the minimum.
     */
    static DataPipeline rank(double lower, double upper) {
        return new RankPipeline(lower, upper);
    }

    /**
     * Returns a local, size-preserving pipeline that replaces one value
     * with another.
     *
     * @param replaceThis the value to be replaced.
     * @param replacement the replacement value.
     *
     * @return a local, size-preserving pipeline that replaces all values
     * equal to {@code replaceThis} with {@code replacement}.
     */
    static DataPipeline replace(double replaceThis, double replacement) {
        return local(String.format("replace(%s, %s)", replaceThis, replacement),
                element -> DoubleComparator.DEFAULT.equals(element, replaceThis) ? replacement : element);
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
     * Returns a local, size-preserving pipeline that applies a hyperbolic
     * tangent filter {@code tanh((x - center) / width)} to pull outliers
     * toward a central value.
     *
     * @param center the center of the hyperbolic tangent function.
     * @param width  the width of the hyperbolic tangent function.
     *
     * @return a hyperbolic tangent pipeline with the specified parameters.
     *
     * @throws RuntimeException unless the width is positive.
     */
    static DataPipeline tanh(double center, double width) {
        if (DoubleComparator.DEFAULT.isPositive(width)) {
            return local(
                    String.format("tanh(%s, %s)", center, width),
                    x -> Math.tanh((x - center) / width));
        }
        else {
            throw new MorpheusException("Width must be positive.");
        }
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
        return TrimPipeline.of(quantile);
  }

    /**
     * Returns a local, size-preserving pipeline that unsets (assigns NaN)
     * to any element that lies outside an interval of valid values.
     *
     * @param lower the lower bound of the valid interval (inclusive)
     * @param upper the upper bound of the valid interval (inclusive).
     *
     * @return a truncating pipeline with the specified interval.
     *
     * @throws RuntimeException unless the interval is valid (the lower bound
     * is less than or equal to the upper bound).
     */
    static DataPipeline truncate(double lower, double upper) {
        return truncate(DoubleInterval.closed(lower, upper));
    }

    /**
     * Returns a local, size-preserving pipeline that unsets (assigns NaN)
     * to any element that lies outside an interval of valid values.
     *
     * @param interval the interval containing valid values.
     *
     * @return a truncating pipeline with the specified interval.
     */
    static DataPipeline truncate(DoubleInterval interval) {
        return local(
                String.format("truncate(%s, %s)", interval.getLower(), interval.getUpper()),
                element -> interval.contains(element) ? element : Double.NaN);
    }

    /**
     * Returns a non-local, size-preserving pipeline that performs Winsorization:
     * elements are bound between {@code M - c * SD} and {@code M + c * SD} where
     * {@code M} is the sample mean, {@code SD} is the standard deviation, and
     * {@code c} is the Winsorization constant.
     *
     * @param constant the Winsorization constant described above.
     *
     * @return a Winsorization pipeline with the specified constant.
     */
    static DataPipeline winsor(double constant) {
        return new WinsorPipeline(constant);
    }
}
