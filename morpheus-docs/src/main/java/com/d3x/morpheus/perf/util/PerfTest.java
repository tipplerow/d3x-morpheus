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

package com.d3x.morpheus.perf.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class PerfTest {

    public static void main(String[] args) {

        final double[] values = new double[50000000];
        for (int i=0; i<values.length; ++i) {
            values[i] = Math.random();
        }

        sum1(values);
        sum2(values, false);
        sum2(values, true);
    }

    /**
     * Compute sum of array, do it five times to check variance
     * @param values    the array of random double values
     */
    static void sum1(double[] values) {
        for (int i=0; i<5; ++i) {
            final long t1 = System.nanoTime();
            double sum = 0d;
            for (int j=0; j<values.length; ++j) {
                sum += values[j];
            }
            final long t2 = System.nanoTime();
            System.out.println("sum1() in " + ((t2-t1)/1000000) + " millis, sum=" + sum);
        }
    }

    /**
     * Compute sum of array, do it five times to check variance
     * @param values    the array of random double values
     * @param shuffle   if true, traverse the array in a random order
     */
    static void sum2(double[] values, boolean shuffle) {
        final int[] indexes = IntStream.range(0, values.length).toArray();
        if (shuffle) {
            shuffle(indexes);
        }
        for (int i=0; i<5; ++i) {
            final long t1 = System.nanoTime();
            double sum = 0d;
            for (int j=0; j<values.length; ++j) {
                sum += values[indexes[j]];
            }
            final long t2 = System.nanoTime();
            System.out.println("sum2() in " + ((t2-t1)/1000000) + " millis, sum=" + sum + ", shuffle=" + shuffle);
        }
    }

    /**
     * Shuffles an int array
     * @param array the array to shuffle
     */
    static void shuffle(int[] array) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }
}
