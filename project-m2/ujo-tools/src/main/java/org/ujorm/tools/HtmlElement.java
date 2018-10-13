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

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * XML element model to rendering a XML file
 * @author Pavel Ponec
 */
public class HtmlElement extends XmlElement {

    /** XML header */
    public static final String HEADER = "<!DOCTYPE html>";

    /** Head */
    private final XmlElement head;

    /** Body */
    private final XmlElement body;

    /** Constructor for codepage UTF-8 */
    public HtmlElement(@Nonnull final String title) {
        this(title, UTF_8);
    }

    /** Generaic Constructor */
    public HtmlElement(@Nonnull final String title, Charset charset) {
        super("html");

        addChild(head = new XmlElement("head"));
        addChild(body = new XmlElement("body"));

        head.createChildElement("meta").addAttrib("charset", charset);
        head.createChildElement("title").addChild(title);
    }

    /** Returns header element */
    @Nonnull
    public XmlElement getHead() {
        return head;
    }

    /** Returns body element */
    @Nonnull
    public XmlElement getBody() {
        return body;
    }

    /** Render the HTML code including header */
    @Override @Nonnull
    public String toString() {
        return toString(new CharArrayWriter(512).append(HEADER).append('\n')).toString();
    }

    /** Render the HTML code without header */
    @Nonnull
    public CharArrayWriter toString(@Nonnull final CharArrayWriter out) {
        return super.toString(out);
    }

}