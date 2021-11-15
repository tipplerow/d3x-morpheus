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
package com.d3x.morpheus.util.functions;

import java.util.Objects;

/**
 * Represents an operation that accepts three input arguments and returns no
 * result, as a generalization of the Java 8 BiConsumer.
 *
 * @author Scott Shaffer
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first argument.
     * @param u the second argument.
     * @param v the third argument.
     */
    void accept(T t, U u, V v);

    /**
     * Returns a composed TriConsumer that performs, in sequence, this operation
     * followed by the after operation.
     *
     * @param after the action to perform after this action.
     *
     * @return a TriConsumer that performs, in sequence, this operation followed
     * by the after operation.
     */
    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);
        return (t, u, v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}
