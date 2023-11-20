/*
 * Copyright 2018-2023, Talos Trading - All Rights Reserved
 *
 * Licensed under a proprietary end-user agreement issued by D3X Systems.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.d3xsystems.com/static/eula/quanthub-eula.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.util;

import lombok.NonNull;

import java.util.Comparator;
import java.util.List;

/**
 * Represents a point in the (x,y) coordinate plane.
 *
 * @author Scott Shaffer
 */
public record PointXY(double x, double y) {
    /**
     * Orders points by their {@code x}-coordinate.
     */
    public static final Comparator<PointXY> X_COMPARATOR = Comparator.comparingDouble(PointXY::x);

    /**
     * Orders points by their {@code y}-coordinate.
     */
    public static final Comparator<PointXY> Y_COMPARATOR = Comparator.comparingDouble(PointXY::y);

    /**
     * Creates a new point with the specified coordinates.
     *
     * @param x the {@code x}-coordinate.
     * @param y the {@code y}-coordinate.
     *
     * @return a new point with the specified coordinates.
     */
    public static PointXY of(double x, double y) {
        return new PointXY(x, y);
    }

    /**
     * Sorts a list of points (in place) by their {@code x}-coordinate.
     *
     * @param points the points to sort.
     */
    public static void sortX(@NonNull List<PointXY> points) {
        points.sort(X_COMPARATOR);
    }

    /**
     * Sorts a list of points (in place) by their {@code y}-coordinate.
     *
     * @param points the points to sort.
     */
    public static void sortY(@NonNull List<PointXY> points) {
        points.sort(Y_COMPARATOR);
    }

    /**
     * Generates an array containing the {@code x}-coordinates of a list of points.
     *
     * @param points the points to convert.
     *
     * @return an array containing the {@code x}-coordinates of the specified points.
     */
    public static double[] toArrayX(@NonNull List<PointXY> points) {
        return points.stream().mapToDouble(PointXY::x).toArray();
    }

    /**
     * Generates an array containing the {@code y}-coordinates of a list of points.
     *
     * @param points the points to convert.
     *
     * @return an array containing the {@code y}-coordinates of the specified points.
     */
    public static double[] toArrayY(@NonNull List<PointXY> points) {
        return points.stream().mapToDouble(PointXY::y).toArray();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PointXY that && equalsPoint(that);
    }

    private boolean equalsPoint(PointXY that) {
        return DoubleComparator.DEFAULT.equals(this.x, that.x) &&
                DoubleComparator.DEFAULT.equals(this.y, that.y);
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("(x, y) points cannot be used as hash keys.");
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", x, y);
    }
}
