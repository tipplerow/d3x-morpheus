/*
 * Copyright 2018-2024, Talos Trading - All Rights Reserved
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
package com.d3x.morpheus.json;

import com.d3x.morpheus.agg.NanPolicy;
import com.d3x.morpheus.util.MorpheusException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Manages the input and output of JSON data for a class.
 *
 * @author Scott Shaffer
 */
@AllArgsConstructor
public final class JsonIO<T> {
    private final Class<T> type;
    private final Function<JsonReader, T> readerFunc;
    private final BiConsumer<T, JsonWriter> writerFunc;

    /**
     * Creates a JSON I/O manager for a specified class.
     *
     * @param type the class to manage.
     *
     * @return a JSON I/O manager for the specified class.
     */
    public static <T> JsonIO<T> forClass(@NonNull Class<T> type) {
        var reader = resolveReaderFunc(type);
        var writer = resolveWriterFunc(type);
        return new JsonIO<>(type, reader, writer);
    }

    /**
     * Opens a JSON file for reading.
     *
     * @param file the JSON file to read.
     *
     * @return a reader for the specified JSON file.
     *
     * @throws RuntimeException if the file cannot be opened.
     */
    public static JsonReader fileReader(@NonNull File file) {
        try {
            return new JsonReader(new FileReader(file));
        }
        catch (IOException ex) {
            throw new MorpheusException("Could not open file [%s] for reading.", file);
        }
    }

    /**
     * Opens a JSON file for writing.
     *
     * @param file the JSON file to write.
     *
     * @return a writer for the specified JSON file.
     *
     * @throws RuntimeException if the file cannot be opened.
     */
    public static JsonWriter fileWriter(@NonNull File file) {
        try {
            var writer = new JsonWriter(new FileWriter(file));
            writer.setIndent("  ");
            return writer;
        }
        catch (IOException ex) {
            throw new MorpheusException("Could not open file [%s] for writing.", file);
        }
    }

    /**
     * Reads the next array from a JSON stream.
     *
     * @param reader the JSON reader to read from.
     *
     * @return the array read from the JSON stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public static List<?> nextArray(@NonNull JsonReader reader) throws IOException {
        var token = reader.peek();

        if (token.equals(JsonToken.NULL)) {
            reader.nextNull();
            return List.of();
        }

        reader.beginArray();
        var array = new ArrayList<>();

        while (reader.hasNext()) {
            var value = nextValue(reader);
            array.add(value);
        }

        reader.endArray();
        return array;
    }

    /**
     * Reads the next object from a JSON stream.
     *
     * @param reader the JSON reader to read from.
     *
     * @return the object read from the JSON stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public static Map<String, ?> nextObject(@NonNull JsonReader reader) throws IOException {
        var token = reader.peek();

        if (token.equals(JsonToken.NULL)) {
            reader.nextNull();
            return null;
        }

        reader.beginObject();
        var object = new LinkedHashMap<String, Object>();

        while (reader.hasNext()) {
            var name = reader.nextName();
            var value = nextValue(reader);
            object.put(name, value);
        }

        reader.endObject();
        return object;
    }

    /**
     * Reads the next array from a JSON stream.
     *
     * @param reader the JSON reader to read from.
     *
     * @return the array read from the JSON stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public static Object nextValue(@NonNull JsonReader reader) throws IOException {
        var token = reader.peek();

        switch (token) {
            case NULL -> {
                reader.nextNull();
                return null;
            }
            case BOOLEAN -> {
                return reader.nextBoolean();
            }
            case NUMBER -> {
                return reader.nextDouble();
            }
            case STRING -> {
                return reader.nextString();
            }
            case BEGIN_ARRAY -> {
                return nextArray(reader);
            }
            case BEGIN_OBJECT -> {
                return nextObject(reader);
            }
            default -> throw new MorpheusException("Unexpected JSON token [%s].", token);
        }
    }

    /**
     * Opens a class resource for reading.
     *
     * @param resourcePath the path to the class resource.
     *
     * @return a reader for the specified resource.
     *
     * @throws RuntimeException if the resource cannot be opened.
     */
    public JsonReader resourceReader(@NonNull String resourcePath) {
        var resource = type.getResourceAsStream(resourcePath);

        if (resource != null) {
            return new JsonReader(new InputStreamReader(resource));
        }
        else {
            throw new MorpheusException("Resource [%s] not found.",  resourcePath);
        }
    }

    /**
     * Reads the object encoded in a JSON file.
     *
     * @param file the JSON file to parse.
     *
     * @return the object encoded in the class resource.
     *
     * @throws RuntimeException if any I/O errors occur.
     */
    public T readFile(@NonNull File file) {
        try (var reader = fileReader(file)) {
            return readerFunc.apply(reader);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Reads the object encoded in a class resource.
     *
     * @param path the path to the class resource.
     *
     * @return the object encoded in the class resource.
     *
     * @throws RuntimeException if any I/O errors occur.
     */
    public T readResource(@NonNull String path) {
        try (var reader = resourceReader(path)) {
            return readerFunc.apply(reader);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Writes an object to a JSON file.
     *
     * @param object the object to write.
     * @param file   the JSON file to write.
     *
     * @throws RuntimeException if any I/O errors occur.
     */
    public void writeFile(@NonNull T object, @NonNull File file) {
        try (var writer = fileWriter(file)) {
            writerFunc.accept(object, writer);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> Function<JsonReader, T> resolveReaderFunc(Class<T> type) {
        try {
            var method = type.getMethod("read", JsonReader.class);
            return reader -> {
                try {
                    return type.cast(method.invoke(null, reader));
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            };
        }
        catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> BiConsumer<T, JsonWriter> resolveWriterFunc(Class<T> type) {
        try {
            var method = type.getMethod("write", JsonWriter.class);
            return (object, writer) -> {
                try {
                    method.invoke(object, writer);
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            };
        }
        catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }
}
