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

package org.ujorm.tools.xml.dom;

import org.junit.Test;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.tools.xml.CommonXmlWriter;
import org.ujorm.tools.xml.Html;
import static org.junit.Assert.*;
import static org.ujorm.tools.xml.CommonXmlWriter.*;

/**
 * @author Pavel Ponec
 */
public class XmlElementTest {

    @Test
    public void testXmlBuilding() {
        System.out.println("XmlBuilding");

        XmlElement root = new XmlElement("root");
        root.addElement("childA")
                .setAttrib("x", 1)
                .setAttrib("y", 2);
        root.addElement("childB")
                .setAttrib("x", 3)
                .setAttrib("y", 4)
                .setAttrib("z", "<'&\">")
                .addText("A text message <'&\">");
        root.addRawText("\n<rawXml/>\n");
        root.addCDATA("A character data <'&\">");

        String result = root.toString();
        String expected = String.join("\n"
                , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                , "<root>"
                , "<childA x=\"1\" y=\"2\"/>"
                , "<childB x=\"3\" y=\"4\" z=\"&lt;'&amp;&quot;&gt;\">A text message &lt;'&amp;\"&gt;</childB>"
                , "<rawXml/>"
                , "<![CDATA[A character data <'&\">]]>"
                + "</root>");
        assertEquals(expected, result);
    }

    @Test
    public void testAddCDATA() {
        System.out.println("AddCDATA");

        XmlElement root = new XmlElement("root");
        root.addCDATA(MsgFormatter.format("A{}B{}C", CDATA_BEG, CDATA_END));
        String expected =CommonXmlWriter.XML_HEADER + "\n<root><![CDATA[A<![CDATA[B]]>]]&gt;<![CDATA[C]]></root>";
        assertEquals(expected, root.toString());

        root = new XmlElement("root");
        root.addCDATA(MsgFormatter.format("{}ABC{}", CDATA_BEG, CDATA_END));
        expected =CommonXmlWriter.XML_HEADER + "\n<root><![CDATA[<![CDATA[ABC]]>]]&gt;<![CDATA[]]></root>";
        assertEquals(expected, root.toString());

        root = new XmlElement("root");
        root.addCDATA(MsgFormatter.format("A{}{}C", CDATA_BEG, CDATA_END));
        expected =CommonXmlWriter.XML_HEADER + "\n<root><![CDATA[A<![CDATA[]]>]]&gt;<![CDATA[C]]></root>";
        assertEquals(expected, root.toString());

        root = new XmlElement("root");
        root.addCDATA("");
        expected =CommonXmlWriter.XML_HEADER + "\n<root/>";
        assertEquals(expected, root.toString());
    }

    @Test
    public void testAddAttrib() {
        System.out.println("testaddAttrib");

        String expected1 = CommonXmlWriter.XML_HEADER + "\n<input readonly=\"\"/>";
        String result1 = new XmlElement(Html.INPUT).setAttrib(Html.A_READONLY, "").toString();
        assertEquals(expected1, result1);

        String expected2 = CommonXmlWriter.XML_HEADER + "\n<input/>";
        String result2 = new XmlElement(Html.INPUT).setAttrib(Html.A_READONLY, null).toString();
        assertEquals(expected2, result2);
    }

    @Test
    public void testAddComment() {
        System.out.println("testAddComment");

        XmlElement root = new XmlElement("root");
        root.addComment("Sample text <&\">");

        String expected = CommonXmlWriter.XML_HEADER
                + "\n<root><!-- Sample text <&\"> --></root>";
        assertEquals(expected, root.toString());
    }
}
