/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web.json;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.Html;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Pavel Ponec
 */
public class JsonBuilderTest {

    /**
     * Test of write method, of class JsonWriter.
     */
    @Test
    public void testWrite() throws Exception {
        System.out.println("write");
        CharSequence key = "abc";
        CharSequence value = "def";

        StringBuilder builder = new StringBuilder();
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, value);
        }
        assertEquals("{\"abc\":\"def\"}", builder.toString());

        value = "\b";
        builder.setLength(0);
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, value);
        }
        assertEquals("{\"abc\":\"\\b\"}", builder.toString());

        value = "\"";
        builder.setLength(0);
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, value);
        }
        assertEquals("{\"abc\":\"\\\"\"}", builder.toString());

        value = "'";
        builder.setLength(0);
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, value);
        }
        assertEquals("{\"abc\":\"'\"}", builder.toString());

        value = "\b\f\n\r\t\"'\\$%^";
        builder.setLength(0);
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, value);
        }
        assertEquals("{\"abc\":\"\\b\\f\\n\\r\\t\\\"'\\\\$%^\"}", builder.toString());

        key = "\b";
        value = "\b";
        builder.setLength(0);
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, value);
        }
        assertEquals("{\"\\b\":\"\\b\"}", builder.toString());
    }

    /**
     * Test of write method, of class JsonWriter.
     */
    @Test
    public void testWriteByProvider() throws Exception {
        System.out.println("writeByProvider");
        CharSequence key = "abc";
        CharSequence[] value = {"def"};

        StringBuilder builder = new StringBuilder();
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, e -> e.addText(value[0]));
        }
        assertEquals("{\"abc\":\"def\"}", builder.toString());

        value[0] = "xyz";
        builder.setLength(0);
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, e -> e.addElementIf(false, Html.DIV).addText(value[0]));
        }
        assertEquals("{\"abc\":\"xyz\"}", builder.toString());

        value[0] = "<text>";
        builder.setLength(0);
        try (JsonBuilder writer = JsonBuilder.of(builder)) {
            writer.write(key, e -> e.addElementIf(true, Html.DIV).addText(value[0]));
        }
        assertEquals("{\"abc\":\"<div>&lt;text&gt;</div>\"}", builder.toString());
    }
}
