package org.ujorm.tools.xml.model;

import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.XmlConfig;

/**
 * @deprecated Backward compatibility only; prefer {@link XmlBuilder}.
 */
@Deprecated
public class XmlModel<T extends XmlModel<T>> extends XmlBuilder<T> {

    /**
     * Root model: the {@code name} argument is accepted for API compatibility but does not produce an XML element
     * (root is rendered as {@link XmlBuilder#HIDDEN_NAME}).
     */
    public XmlModel(@SuppressWarnings("unused") @NotNull String name) {
        super(HIDDEN_NAME, new XmlPrinter(new StringBuilder(512),
                XmlConfig.ofDoctype(AbstractWriter.XML_HEADER + "\n")));
    }

    protected XmlModel(
            @NotNull String name,
            @NotNull XmlPrinter writer,
            int level,
            boolean printName) {
        super(name, writer, level, printName);
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    protected T createChild(@NotNull final String name) {
        return (T) new XmlModel<>(name, getWriter(), getLevel() + 1, false);
    }

    /**
     * Finishes open elements so the shared {@link XmlPrinter} matches historical XmlModel output (e.g. {@code </table>}).
     */
    @Override
    @NotNull
    public String toString() {
        if (!isClosed()) {
            close();
        }
        return super.toString();
    }
}
