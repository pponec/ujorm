package org.ujorm.tools.xml;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for AbstractWriter using concrete Element class
 * @author Pavel Ponec
 */
class AbstractWriterTest {

    private StringBuilder out;
    private DefaultXmlConfig config;
    private AbstractWriter writer;

    @BeforeEach
    void setUp() {
        out = new StringBuilder();
        config = new DefaultXmlConfig();
        writer = new AbstractWriter(out, config) {};
    }

    /** Helper method to create a real Element instance for testing */
    private Element createElement(String name) {
        var printer = new XmlPrinter(out, config);
        return new Element(name, printer, 0);
    }

    @Test
    void testWriteEscaping() throws IOException {
        var input = "Tag < > & \" '";

        // Text mode
        writer.write(input, false);
        assertEquals("Tag &lt; &gt; &amp; \" '", out.toString());

        out.setLength(0);

        // Attribute mode
        writer.write(input, true);
        assertEquals("Tag &lt; &gt; &amp; &quot; '", out.toString());
    }

    @Test
    void testWriteSpecialSpaces() throws IOException {
        var input = String.valueOf(AbstractWriter.NBSP) + AbstractWriter.NARROW_NBSP;
        writer.write(input, false);
        assertEquals("&#160;&#8239;", out.toString());
    }

    @Test
    void testWriteControlCharacters() throws IOException {
        writer.write("\n\r\t", false);
        assertEquals("&#10;&#13;&#9;", out.toString());
    }

    @Test
    void testWriteRawValue() throws IOException {
        var rawValue = "<un-escaped-content>";
        var element = createElement("test");

        out.setLength(0); // Clear after element creation
        writer.writeRawValue(rawValue, element);
        assertEquals(rawValue, out.toString());
    }

    @Test
    void testWriteNewLine() throws IOException {
        config.setIndentationSpace("  ");
        var localWriter = new AbstractWriter(out, config) {};

        localWriter.writeNewLine(2);

        var expectedResult = config.getNewLine() + "    ";
        assertEquals(expectedResult, out.toString());
    }

    @Test
    void testWriteValue() throws IOException {
        var value = "test-value";
        var element = createElement("test");

        out.setLength(0);
        writer.writeValue(value, element, "attrName");
        assertEquals("test-value", out.toString());
    }

    @Test
    void testGetWriterEscaped() throws IOException {
        var escapedAppendable = writer.getWriterEscaped();
        escapedAppendable.append("<b>");

        assertEquals("&lt;b&gt;", out.toString());
    }

    @Test
    void testWritePart() throws IOException {
        var text = "Prefix <content> Suffix";
        writer.write(text, 7, 16, false);
        assertEquals("&lt;content&gt;", out.toString());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void testConstructorNullCheck() {
        // Ošetření RuntimeException kvůli vaší instrumentaci @NotNull
        assertThrows(RuntimeException.class, () -> new AbstractWriter(null, config) {});
        assertThrows(RuntimeException.class, () -> new AbstractWriter(out, null) {});
    }
}