/*
 * Copyright 2018-2022 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlElement.java
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

import org.ujorm.tools.xml.model.*;

/**
 * XML element <strong>model</strong> to rendering a XML file.
 * The main benefits are:
 * <ul>
 *     <li>secure building well-formed XML documents  by the Java code</li>
 *     <li>a simple API built on a single XmlElement class</li>
 *     <li>creating XML components by a subclass is possible</li>
 *     <li>great performance and small memory footprint</li>
 * </ul>¨
 * <h3>How to use the class:</h3>
 * <pre class="pre">
 *  XmlElement root = new XmlElement("root");
 *  root.addElement("childA")
 *          .setAttrib("x", 1)
 *          .setAttrib("y", 2);
 *  root.addElement("childB")
 *          .setAttrib("x", 3)
 *          .setAttrib("y", 4)
 *          .addText("A text message &lt;&\"&gt;");
 *  root.addRawText("\n&lt;rawXml/&gt;\n");
 *  root.addCDATA("A character data &lt;&\"&gt;");
 *  String result = root.toString();
 * </pre>
 *
 * @see HtmlElement
 * @since 1.86
 * @author Pavel Ponec
 * @deprecated Use the {@link XmlModel} rather.
 */
@Deprecated
public class XmlElement extends XmlModel {

    public XmlElement(String name) {
        super(name);
    }

    public XmlElement(String name, XmlModel parent) {
        super(name, parent);
    }

}