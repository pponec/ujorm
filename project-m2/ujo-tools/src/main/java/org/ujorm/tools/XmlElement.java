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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * XML element model to rendering a XML file
 * @author Pavel Ponec
 */
public class XmlElement {

    /** XML header */
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    /** A special XML character */
    protected static final char XML_GT = '>';
    /** A special XML character */
    protected static final char XML_LT = '<';
    /** A special XML character */
    protected static final char XML_AMP = '&';
    /** A special XML character */
    protected static final char XML_QUOT = '\'';
    /** A special XML character */
    protected static final char XML_2QUOT = '"';
    /** A special XML character */
    protected static final char CHAR_SPACE = ' ';
    /** A CDATA beg markup sequence */
    protected static final String CDATA_BEG = "<![CDATA[";
    /** A CDATA end markup sequence */
    protected static final String CDATA_END = "]]>";

    /** Element name */
    protected final String name;

    /** Attributes */
    private Map<String, Object> attributes;

    /** Child elements */
    private List<Object> childs;

    /** New element */
    public XmlElement(@Nonnull final CharSequence name) {
        this.name = name.toString();
    }

    /** New element with a parent */
    public XmlElement(@Nonnull final CharSequence name, @Nonnull final XmlElement parent) {
        this(name);
        parent.getChilds().add(this);
    }

    /** Return attributes
     * @return This instance */
    @Nonnull
    protected Map<String, Object> getAttribs() {
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }
        return attributes;
    }

    /** Add an attribute
     * @return This instance */
    @Nonnull
    public XmlElement addAttrib(@Nonnull final CharSequence name, @Nonnull final Object value) {
        getAttribs().put(name.toString(), value);
        return this;
    }


    /** Return attributes */
    @Nonnull
    protected List<Object> getChilds() {
        if (childs == null) {
            childs = new ArrayList<>();
        }
        return childs;
    }

    /**
     * Add a child element
     * @param element A child element
     * @return This instance */
    @Nonnull
    public XmlElement addElement(@Nonnull final XmlElement element) {
        getChilds().add(element);
        return this;
    }

    /** Create a new {@link XmlElement} for a required name and add it to children.
     * @param name Name of the new XmlElement.
     * @return The new XmlElement!
     */
    @Nonnull
    public XmlElement addElement(@Nonnull final CharSequence name) {
        return new XmlElement(name, this);
    }

    /**
     * Add a text
     * @param text text
     * @return This instance */
    @Nonnull
    public XmlElement addText(@Nonnull final CharSequence text) {
        getChilds().add(text);
        return this;
    }

    /** Insert an unformatted XML code
     * @return This instance */
    @Nonnull
    public XmlElement addXmlCode(@Nonnull final CharSequence code) {
        getChilds().add(new RawXmlEnvelope(code));
        return this;
    }

    /**
     * Add a character data in {@code CDATA} format
     * @param text The text is not checked for the final DATA sequence
     * @return This instance
     */
    @Nonnull
    public XmlElement addCDATA(@Nonnull final CharSequence text) {
        return addXmlCode(new StringBuilder
                ( CDATA_BEG.length()
                + text.length()
                + CDATA_END.length())
                .append(CDATA_BEG)
                .append(text)
                .append(CDATA_END));
    }

    /** Write escaped value to the output */
    protected void writeValue(@Nonnull final Object value, @Nonnull final CharArrayWriter out) {
        final String text = String.valueOf(value);
        for (int i = 0, max = text.length(); i < max; i++) {
            final char c = text.charAt(i);
            switch (c) {
                case XML_LT:
                    out.append(XML_AMP).append("lt;");
                    break;
                case XML_GT:
                    out.append(XML_AMP).append("gt;");
                    break;
                case XML_AMP:
                    out.append(XML_AMP).append("#38;");
                    break;
                case XML_QUOT:
                    out.append(XML_AMP).append("#39;");
                    break;
                case XML_2QUOT:
                    out.append(XML_AMP).append("#34;");
                    break;
                default: {
                    if (c > 32 || c == CHAR_SPACE) {
                        out.append(c);
                    } else {
                        out.append(XML_AMP).append("#");
                        out.append(Integer.toString(c));
                        out.append(";");
                    }
                }
            }
        }
    }

    /** Render the XML code including header */
    @Override @Nonnull
    public String toString() {
        return toString(new CharArrayWriter(512).append(HEADER).append('\n')).toString();
    }

    /** Render the XML code without header */
    @Nonnull
    public CharArrayWriter toString(@Nonnull final CharArrayWriter out) {
        out.append(XML_LT);
        out.append(name);

        if (attributes != null) {
            for (String key : attributes.keySet()) {
                out.append(CHAR_SPACE);
                out.append(key);
                out.append('=');
                out.append(XML_2QUOT);
                writeValue(attributes.get(key), out);
                out.append(XML_2QUOT);
            }
        }
        if (childs != null) {
            out.append(XML_GT);
            for (Object child : childs) {
                if (child instanceof XmlElement) {
                    out.append('\n');
                    ((XmlElement)child).toString(out);
                } else if (child instanceof RawXmlEnvelope) {
                    out.append(((RawXmlEnvelope) child).getBody());
                } else {
                    writeValue(child, out);
                }
            }
            out.append(XML_LT);
            out.append('/');
            out.append(name);
        } else {
            out.append('/');
        }
        out.append(XML_GT);

        return out;
    }

    // -------- Inner class --------

    /** Raw XML code evelope */
    protected static final class RawXmlEnvelope {
        /** Xml content */
        private final CharSequence body;

        public RawXmlEnvelope(@Nonnull final CharSequence body) {
            this.body = body;
        }

        @Nonnull
        public CharSequence getBody() {
            return body;
        }
    }
}