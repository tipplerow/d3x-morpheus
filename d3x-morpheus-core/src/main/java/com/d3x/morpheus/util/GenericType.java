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
package com.d3x.morpheus.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class used to manufacture types to represent Generic types
 *
 * @author Xavier Witdouck
 */
public class GenericType {


    /**
     * Returns a ParameterizedType of a List of type specified
     * @param typeArg   the parameter type for list
     * @return              the newly created ParameterizedType
     */
    public static ParameterizedType ofList(Type typeArg) {
        return of(List.class, typeArg);
    }


    /**
     * Returns a ParameterizedType of a List of type specified
     * @param typeArg   the parameter type for list
     * @return              the newly created ParameterizedType
     */
    public static ParameterizedType ofSet(Type typeArg) {
        return of(Set.class, typeArg);
    }


    /**
     * Returns a ParameterizedType of a Map with key and value type
     * @param keyType   the key type
     * @param valueType the value type
     * @return          the newly created ParameterizedType
     */
    public static ParameterizedType ofMap(Type keyType, Type valueType) {
        return new MapType(keyType, valueType);
    }


    /**
     * Returns a ParameterizedType for the args provided
     * @param rawType       the raw type
     * @param typeArgs      the type args
     * @return              the newly created ParameterizedType
     */
    public static ParameterizedType of(Type rawType, Type... typeArgs) {
        return new ParamType(rawType, typeArgs);
    }



    /**
     * A ParameterizedType implementation for a single type
     */
    @lombok.ToString()
    @lombok.AllArgsConstructor()
    @lombok.EqualsAndHashCode()
    private static class ParamType implements ParameterizedType {

        @lombok.NonNull
        private Type rawType;
        @lombok.NonNull
        private Type[] typeArgs;

        @Override
        public Type[] getActualTypeArguments() {
            return typeArgs;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }


    /**
     * A ParameterizedType implementation for a Map type
     */
    @lombok.ToString()
    @lombok.AllArgsConstructor()
    @lombok.EqualsAndHashCode()
    private static class MapType implements ParameterizedType {

        @lombok.NonNull
        private Type keyType;
        @lombok.NonNull
        private Type valueType;

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] { keyType, valueType };
        }

        @Override
        public Type getRawType() {
            return Map.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
