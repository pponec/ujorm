/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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

package org.ujorm.tools;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Pavel Ponec
 */
public class XmlElementTest {

    @Test
    public void testAdd() {
        System.out.println("add");

        final XmlElement root = new XmlElement("root");
        new XmlElement("childA")
                .addAttrib("x", 1)
                .addAttrib("y", 2)
                .addTo(root);
        new XmlElement("childB")
                .addAttrib("x", 3)
                .addAttrib("y", 4)
                .addChild("A text message <&\">")
                .addTo(root);
        root.addXmlCode("\n<rawXml/>\n");

        String result = root.toString();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "\n<root>"
                + "\n<childA x=\"1\" y=\"2\"/>"
                + "\n<childB x=\"3\" y=\"4\">A text message &lt;&#38;&#34;&gt;</childB>"
                + "\n<rawXml/>"
                + "\n</root>";
        assertNotNull(result);
        assertEquals(expected, result);
    }
}
