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
package org.ujorm.tools.xml.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Check;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.builder.XmlBuilder;
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
        this(new StringBuilder(512));
    }

    /** Writer constructor with a zero offset */
    public XmlWriter(@NotNull final Appendable out) {
        super(out, XmlConfig.ofDefault());
    }

    /**
     * A writer constructor
     * @param out A writer
     * @param indentationSpace String for a one level offset.
     */
    public XmlWriter(@NotNull final Appendable out, @NotNull final CharSequence indentationSpace) {
        super(out, createConfig(indentationSpace));
    }

    /** Create a config */
    private static XmlConfig createConfig(@NotNull final CharSequence indentationSpace) {
        final DefaultXmlConfig config = XmlConfig.ofDefault();
        config.setIndentationSpace(indentationSpace);
        return config;
    }

    /** Render the XML code without header */
    @NotNull
    public final XmlWriter write(final int level, @NotNull final XmlModel element) throws IOException {
        return write(level, element.getName(), element.attributes, element.children, element);
    }

    /** Render the XML code without header
     * @param level Element nesting level.
     * @param name Name of element where the {@code null} is allowed for an AJAX responses
     * @param attributes Attributes of the element
     * @param children Childern of the element including {@code null} items
     * @param element Original element
     * @return This
     */
    @NotNull
    protected XmlWriter write(final int level
            , @Nullable final CharSequence name
            , @Nullable final Map<String, Object> attributes
            , @Nullable final List<Object> children
            , @NotNull final XmlModel element) throws IOException {

        final boolean validName = name != XmlBuilder.HIDDEN_NAME;
        if (validName) {
            out.append(XML_LT);
            out.append(name);

            if (Check.hasLength(attributes)) {
                assert attributes != null; // For static analyzer only
                for (String key : attributes.keySet()) {
                    out.append(SPACE);
                    out.append(key);
                    out.append('=');
                    out.append(XML_2QUOT);
                    writeValue(attributes.get(key), element, key);
                    out.append(XML_2QUOT);
                }
            }
        }
        if (Check.hasLength(children)) {
            assert children != null; // For static analyzer only
            if (validName) {
                out.append(XML_GT);
            }
            boolean writeNewLine = validName;
            for (Object child : children) {
                if (child instanceof XmlModel) {
                    final XmlModel xmlChild = (XmlModel) child;
                    if (writeNewLine && xmlChild.name != XmlBuilder.HIDDEN_NAME) {
                        writeNewLine(level);
                    } else {
                        writeNewLine = validName;
                    }
                    write(level + 1, xmlChild);
                } else if (child instanceof XmlModel.RawEnvelope) {
                    writeRawValue(((XmlModel.RawEnvelope) child).get().toString(), element);
                    writeNewLine = false;
                } else {
                    writeValue(child, element, null);
                    writeNewLine = false;
                }
            }
            if (indentationEnabled && writeNewLine && level >= 0) {
                writeNewLine(level - 1);
            }
            if (validName) {
                out.append(XML_LT);
                out.append(FORWARD_SLASH);
                out.append(name);
            }
        } else if (validName) {
            out.append(FORWARD_SLASH);
        }
        if (validName) {
            out.append(XML_GT);
        }
        return this;
    }
}

