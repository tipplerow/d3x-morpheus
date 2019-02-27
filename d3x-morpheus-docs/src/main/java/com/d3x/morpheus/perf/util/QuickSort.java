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


import java.util.concurrent.ThreadLocalRandom;

public class QuickSort {

    public static void main(String[] args) {

        for (int k=0; k<5; ++k) {
            final double[] values = ThreadLocalRandom.current().doubles(10000000).toArray();
            final long t1 = System.currentTimeMillis();
            quickSort1(values, 0, values.length-1);
            final long t2 = System.currentTimeMillis();
            System.out.println("quickSort1() in " + (t2-t1) + " millis");
        }

        for (int k=0; k<5; ++k) {
            final double[] values = ThreadLocalRandom.current().doubles(10000000).toArray();
            final Swapper swapper = (i, j) -> { double v1 = values[i]; values[i] = values[j]; values[j] = v1; };
            final long t1 = System.currentTimeMillis();
            quickSort2(values, 0, values.length-1, Double::compare, swapper);
            final long t2 = System.currentTimeMillis();
            System.out.println("quickSort2() in " + (t2-t1) + " millis");
        }

    }


    static void quickSort1(double values[], int left, int right) {
        int i = left, j = right;
        double pivot = values[(left + right) / 2];
        while (i <= j) {
            while (values[i] < pivot) i++;
            while (values[j] > pivot) j--;
            if (i <= j) {
                double tmp = values[i];
                values[i] = values[j];
                values[j] = tmp;
                i++;
                j--;
            }
        }
        if (left < i - 1) {
            quickSort1(values, left, i - 1);
        }
        if (i < right) {
            quickSort1(values, i, right);
        }
    }


    static void quickSort2(double[] values, int left, int right, DoubleComparator comparator, Swapper swapper) {
        int i = left, j = right;
        double pivot = values[(left + right) / 2];
        while (i <= j) {
            while (comparator.compare(values[i], pivot) < 0) i++;
            while (comparator.compare(values[j], pivot) > 0) j--;
            if (i <= j) {
                swapper.swap(i, j);
                i++;
                j--;
            }
        }
        if (left < i - 1) {
            quickSort2(values, left, i - 1, comparator, swapper);
        }
        if (i < right) {
            quickSort2(values, i, right, comparator, swapper);
        }
    }


    interface DoubleComparator {

        int compare(double v1, double v2);
    }

    interface Swapper {

        void swap(int i, int j);
    }



}


