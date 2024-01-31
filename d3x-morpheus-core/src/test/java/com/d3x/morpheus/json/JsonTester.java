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

import lombok.AllArgsConstructor;
import lombok.NonNull;

import org.testng.Assert;

import java.io.File;
import java.io.IOException;

/**
 * Tests the input and output of JSON data for a class.
 *
 * @author Scott Shaffer
 */
@AllArgsConstructor
public final class JsonTester<T> {
    private final JsonIO<T> io;

    /**
     * Creates a new tester for a given class.
     *
     * @param type the class to test.
     *
     * @return a tester for the class.
     */
    public static <T> JsonTester<T> forClass(@NonNull Class<T> type) {
        return new JsonTester<>(JsonIO.forClass(type));
    }

    /**
     * Tests the input and output of JSON data for the object encoded
     * in a resource file.
     *
     * @param path the path to the resource file.
     */
    public void testIO(@NonNull String path) {
        try {
            var object1 = io.readResource(path);
            var tmpFile = File.createTempFile("json-tester", ".json");
            tmpFile.deleteOnExit();
            io.writeFile(object1, tmpFile);
            var object2 = io.readFile(tmpFile);
            Assert.assertEquals(object1, object2);
        }
        catch (IOException ex) {
            Assert.fail(ex.getMessage(), ex);
        }
    }
}
