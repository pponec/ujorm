/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
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

package org.ujorm.tools.web;

import java.io.IOException;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Check;
import org.ujorm.tools.xml.AbstractElement;
import org.ujorm.tools.xml.dom.XmlElement;

/** A proxy for a HTML element */
public class Element extends AbstractElement<Element> implements Html {

    /** An original XML element */
    protected final AbstractElement origElement;

    /** New element with a parent */
    public Element(@Nonnull final AbstractElement original) {
        super(original.getName());
        this.origElement = original;
    }

    @Override
    public final <T extends Element> T addElement(String name) {
        try {
            return (T) new Element(origElement.addElement(name));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final <T extends Element> T setAttrib(String name, Object data) {
        try {
            origElement.setAttrib(name, data);
            return (T) this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Element> T addText(Object data) throws IllegalStateException {
        try {
            origElement.addText(data);
            return (T) this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Element> T addTextWithSpace(Object data) throws IllegalStateException {
        try {
            origElement.addTextWithSpace(data);
            return (T) this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Element> T addRawText(Object data) throws IllegalStateException {
        try {
            origElement.addRawText(data);
            return (T) this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Element> T addComment(CharSequence comment) throws IllegalStateException {
        try {
            origElement.addComment(comment);
            return (T) this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Element> T addCDATA(CharSequence charData) throws IllegalStateException {
        try {
            origElement.addCDATA(charData);
            return (T) this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws IllegalStateException {
        try {
            origElement.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // -------------- Add TAG -----

    public <T extends Element> T addElement(@Nonnull final String name, @Nonnull final CharSequence... cssClasses) {
        final T result = addElement(name);
        if (Check.hasLength(cssClasses)) {
            result.setAttrib(A_CLASS, String.join(" ", cssClasses));
        }
        return result;
    }

    /** Add new Table with cellpadding a cellspacing values to zero. */
    public <T extends Element> T addTable(@Nonnull final CharSequence... cssClasses) {
        return addElement(TABLE, cssClasses)
                .setAttrib(Element.A_CELLPADDING, 0)
                .setAttrib(Element.A_CELLSPACING, 0);
    }

    /** Add new body element */
    public <T extends Element> T addBody(@Nonnull final CharSequence... cssClasses) {
        return addElement(BODY, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addTitle(@Nonnull final CharSequence... cssClasses) {
        return addElement(TITLE, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addLink(@Nonnull final CharSequence... cssClasses) {
        return addElement(LINK, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addStyle(@Nonnull final CharSequence... cssClasses) {
        return addElement(STYLE, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addScript(@Nonnull final CharSequence... cssClasses) {
        return addElement(SCRIPT, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addDiv(@Nonnull final CharSequence... cssClasses) {
        return addElement(DIV, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addPreformatted(@Nonnull final CharSequence... cssClasses) {
        return addElement(PRE, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addSpan(@Nonnull final CharSequence... cssClasses) {
        return addElement(SPAN, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addParagraph(@Nonnull final CharSequence... cssClasses) {
        return addElement(P, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addForm(@Nonnull final CharSequence... cssClasses) {
        return addElement(FORM, cssClasses);
    }

    /** Add new heading with the required level */
    public <T extends Element> T addHeading(int level, @Nonnull String title, @Nonnull final CharSequence... cssClasses) {
        return addElement(HEADING_PREFIX + level, cssClasses).addText(title);
    }

    /** Add new body element */
    public <T extends Element> T addTableHead(@Nonnull final CharSequence... cssClasses) {
        return addElement(THEAD, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addTableRow(@Nonnull final CharSequence... cssClasses) {
        return addElement(TR, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addTableDetail(@Nonnull final CharSequence... cssClasses) {
        return addElement(TD, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addLabel(@Nonnull final CharSequence... cssClasses) {
        return addElement(LABEL, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addInput(@Nonnull final CharSequence... cssClasses) {
        return addElement(INPUT, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addTextArea(@Nonnull final CharSequence... cssClasses) {
        return addElement(TEXT_AREA, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addSelect(@Nonnull final CharSequence... cssClasses) {
        return addElement(SELECT, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addOption(@Nonnull final CharSequence... cssClasses) {
        return addElement(OPTION, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addButton(@Nonnull final CharSequence... cssClasses) {
        return addElement(BUTTON, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addAnchor(@Nonnull final CharSequence... cssClasses) {
        return addElement(A, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addUnorderedlist(@Nonnull final CharSequence... cssClasses) {
        return addElement(UL, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addOrderedList(@Nonnull final CharSequence... cssClasses) {
        return addElement(OL, cssClasses);
    }

    /** Add new body element */
    public <T extends Element> T addListItem(@Nonnull final CharSequence... cssClasses) {
        return addElement(LI, cssClasses);
    }

    /** Set a CSS class attribute */
    public <T extends Element> T setClass(@Nonnull final CharSequence... cssClasses) {
        if (Check.hasLength(cssClasses)) {
            setAttrib(A_CLASS, String.join(" ", cssClasses));
        }
        return (T) this;
    }

    // ---- Static methods ----

    /** Crate a root element
     * @param cssLinks Nullable CSS link array
     */
    public static Element createHtmlRoot(@Nonnull final Object title, @Nullable final CharSequence... cssLinks) {
        return createHtmlRoot(title, null, cssLinks);
    }


    /** Crate a root element
     * @param charset A charset
     * @param cssLinks Nullable CSS link array
     */
    public static Element createHtmlRoot(@Nonnull final Object title, @Nullable final Charset charset, @Nullable final CharSequence... cssLinks) {
        XmlElement result = new XmlElement(HTML);
        XmlElement head = result.addElement(HEAD);
        head.addElement(META).setAttrib(A_CHARSET, charset);
        head.addElement(TITLE).addText(title);

        if (cssLinks != null) {
            for (CharSequence cssLink : cssLinks) {
                head.addElement(LINK)
                        .setAttrib(A_HREF, cssLink)
                        .setAttrib(A_REL, "stylesheet")
                        .setAttrib(A_TYPE, "text/css");
            }
        }

        return new Element(result);
    }
}
