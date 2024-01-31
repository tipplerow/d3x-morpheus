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

import com.google.gson.stream.JsonReader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * @author Scott Shaffer
 */
public class JsonIOTest {
    private static Object readValue(String json) {
        try (var reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(true);
            reader.beginObject();
            reader.nextName();
            var element = JsonIO.nextValue(reader);
            reader.endObject();
            return element;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testNextValue() {
        var s = readValue("{'item': 'ABC'}");
        var i = readValue("{'item': 123}");
        var x = readValue("{'item': 456.0}");
        var b = readValue("{'item': true}");
        var n = readValue("{'item': null}");
        var arr = readValue("{'item': ['ABC', 'DEF']}");
        var obj = readValue("{'item': {'field1': 'F1', 'field2': 'F2'}}");

        Assert.assertTrue(s instanceof String);
        Assert.assertEquals(s, "ABC");

        Assert.assertTrue(i instanceof Double);
        Assert.assertEquals(((Double) i), 123.0, 1.0E-12);

        Assert.assertTrue(x instanceof Double);
        Assert.assertEquals(((Double) x), 456.0, 1.0E-12);

        Assert.assertTrue(b instanceof Boolean);
        Assert.assertTrue((Boolean) b);

        Assert.assertNull(n);

        Assert.assertTrue(arr instanceof List);
        Assert.assertEquals(((List<?>) arr), List.of("ABC", "DEF"));

        Assert.assertTrue(obj instanceof Map);
        Assert.assertEquals(((Map<?, ?>) obj), Map.of("field1", "F1", "field2", "F2"));
    }
}
