/*
 * Copyright 2018-2020 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlWriter.java
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

package org.ujorm.tools.xml.model;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Check;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.config.XmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig;

/**
 * If you need special formatting, overwrite responsible methods.
 * @see XmlModel
 * @since 1.88
 * @author Pavel Ponec
 */
public class XmlWriter extends AbstractWriter {

    /** Default constructor a zero offset */
    public XmlWriter() {
        this(new CharArrayWriter(512));
    }

    /** Writer constructor with a zero offset */
    public XmlWriter(@Nonnull final Appendable out) {
        super(out, XmlConfig.ofDefault());
    }

    /**
     * A writer constructor
     * @param out A writer
     * @param indentationSpace String for a one level offset.
     */
    public XmlWriter(@Nonnull final Appendable out, @Nonnull final String indentationSpace) {
        super(out, createConfig(indentationSpace));
    }

    /** Create a config */
    private static XmlConfig createConfig(@Nonnull final String indentationSpace) {
        final DefaultXmlConfig config = XmlConfig.ofDefault();
        config.setIndentationSpace(indentationSpace);
        return config;
    }

    /** Render the XML code without header */
    @Nonnull
    public final XmlWriter write(final int level, @Nonnull final XmlModel element) throws IOException {
        return write(level, element.getName(), element.attributes, element.children, element);
    }

    /** Render the XML code without header
     * @param level Element nesting level.
     * @param name Name of element
     * @param attributes Attributes of the element
     * @param children Childern of the element including {@code null} items
     * @param element Original element
     * @return This
     */
    @Nonnull
    protected XmlWriter write(final int level
            , @Nonnull final CharSequence name
            , @Nullable final Map<String, Object> attributes
            , @Nullable final List<Object> children
            , @Nonnull final XmlModel element) throws IOException {
        out.append(XML_LT);
        out.append(name);

        if (Check.hasLength(attributes)) {
            assert attributes != null; // For static analyzer only
            for (String key : attributes.keySet()) {
                out.append(CHAR_SPACE);
                out.append(key);
                out.append('=');
                out.append(XML_2QUOT);
                writeValue(attributes.get(key), element, key);
                out.append(XML_2QUOT);
            }
        }
        if (Check.hasLength(children)) {
            assert children != null; // For static analyzer only
            out.append(XML_GT);
            boolean writeNewLine = true;
            for (Object child : children) {
                if (child instanceof XmlModel) {
                    if (writeNewLine) {
                        writeNewLine(level);
                    } else {
                        writeNewLine = true;
                    }
                    write(level + 1, (XmlModel) child);
                } else if (child instanceof XmlModel.RawEnvelope) {
                    writeRawValue(((XmlModel.RawEnvelope) child).get(), element);
                    writeNewLine = false;
                } else {
                    writeValue(child, element, null);
                    writeNewLine = false;
                }
            }
            if (indentationEnabled && writeNewLine && level >= 0) {
                writeNewLine(level - 1);
            }
            out.append(XML_LT);
            out.append(FORWARD_SLASH);
            out.append(name);
        } else {
            out.append(FORWARD_SLASH);
        }
        out.append(XML_GT);
        return this;
    }
}

