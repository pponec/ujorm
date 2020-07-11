/*
 * Copyright 2018-2020 Pavel Ponec,
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

package org.ujorm.tools.xml.builder;

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.xml.ApiElement;

/**
 * A XML builder.
 * The main benefits are:
 * <ul>
 *     <li>secure building well-formed XML documents  by the Java code</li>
 *     <li>a simple API built on a single XmlElement class</li>
 *     <li>creating XML components by a subclass is possible</li>
 *     <li>great performance and small memory footprint</li>
 * </ul>¨
 * <h3>How to use the class:</h3>
 * <pre class="pre">
 *  XmlPriter writer = XmlPriter.forXml();
 *  try (XmlBuilder html = new XmlBuilder(Html.HTML, writer)) {
 *      try (XmlBuilder head = html.addElement(Html.HEAD)) {
 *          head.addElement(Html.META, Html.A_CHARSET, UTF_8);
 *          head.addElement(Html.TITLE).addText("Test");
 *      }
 *      ry (XmlBuilder body = html.addElement(Html.BODY)) {
 *          body.addElement(Html.H1).addText("Hello word!");
 *          body.addElement(Html.DIV).addText(null);
 *      }
 *  };
 *  String result = writer.toString();
 * </pre>
 *
 * The XmlElement class implements the {@link Closeable} implementation
 * for an optional highlighting the tree structure in the source code.
 * @see HtmlElement
 * @since 1.86
 * @author Pavel Ponec
 */
public class XmlBuilder extends AbstractXmlBuilder<XmlBuilder> {

    public XmlBuilder(CharSequence name, XmlPrinter writer, int level) {
        super(name, writer, level);
    }

    protected XmlBuilder(CharSequence name, XmlPrinter writer, int level, boolean printName) {
        super(name, writer, level, printName);
    }

    public XmlBuilder(CharSequence name, XmlPrinter writer) {
        super(name, writer);
    }

    /** Create a new {@link XmlBuilder} for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    @Override @Nonnull
    public final XmlBuilder addElement(@Nonnull final String name) {
        XmlBuilder xb = new XmlBuilder(name, writer, level + 1, false);
        return nextChild(xb);
    }

    // --- Factory method ---

    /** Create builder for HTML */
    @Nonnull
    public static XmlBuilder forHtml(@Nonnull Appendable response) {
        return new XmlBuilder(HTML, XmlPrinter.forHtml(response));
    }

    @Nonnull
    public static XmlBuilder forNiceHtml(@Nonnull Appendable response) {
        return new XmlBuilder(HTML, XmlPrinter.forNiceHtml(response));
    }

}