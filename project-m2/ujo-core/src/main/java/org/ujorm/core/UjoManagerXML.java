/*
 *  Copyright 2007-2014 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.UjoTextable;
import static org.ujorm.core.UjoTools.SPACE;

/**
 * Ujo Manager for instances type of UjoTextAccess.
 * <br>Method supports for example persistence type of XML
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 *  Person person = <span class="java-keywords">new</span> <span class="java-layer-method">Person</span>(); <span class="java-block-comment">// Set attributes ...</span>
 *
 *  <span class="java-block-comment">// Save XML:</span>
 *  String defaultXmlHeader = <span class="java-keywords">null</span>;
 *  UjoManagerXML.<span class="java-layer-method">getInstance</span>().<span class="java-layer-method">saveXML</span>(<span class="java-keywords">new</span> <span class="java-layer-method">File</span>(<span class="java-string-literal">"file.xml"</span>), person, defaultXmlHeader, <span class="java-string-literal">"SaveContext"</span>);
 *
 *  <span class="java-block-comment">// Load XML:</span>
 *  person = UjoManagerXML.<span class="java-layer-method">getInstance</span>().<span class="java-layer-method">parseXML</span>(<span class="java-keywords">new</span> <span class="java-layer-method">File</span>(<span class="java-string-literal">"file.xml"</span>), Person.<span class="java-keywords">class</span>, <span class="java-string-literal">"LoadContext"</span>);
 * </pre>
 * @author Pavel Ponec
 */
public class UjoManagerXML extends UjoService<UjoTextable> {

    /** A default XML header: &lt;?xml version="1.0" encoding="UTF-8"?&gt; */
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** A name of Java Class of XML attribute. */
    public static final String ATTR_CLASS = "javaClass";
    /** A name of Java List Class of a XML attribute. */
    public static final String ATTR_LIST = "javaList";
    /** A name of Java Class of XML List attribute. */
    public static final String ATTR_ITEM = "javaItem";

    /** Break XML file */
    protected boolean breakLineEnabled = true;
    /** A CONTEXT of the actionExport */
    protected UjoAction actionExport;
    /** A CONTEXT of the actionExport */
    protected UjoAction actionElement;


    /** Constructor. */
    protected UjoManagerXML() {
        super(UjoTextable.class);
    }

    /** Create Instance */
    public static UjoManagerXML getInstance() {
        return new UjoManagerXML();
    }

    /** Create Ujo from XMl file */
    public <T extends UjoTextable> T parseXML(File inputFile, Class<T> classType, Object context) throws IllegalStateException {
        return parseXML(inputFile, classType, true, context);
    }

    /** Create Ujo from XMl file */
    public <T extends UjoTextable> T parseXML(File inputFile, Class<T> classType, boolean validating, Object context) throws IllegalStateException {
        InputStream bis = null;
        try {
            bis = getInputStream(inputFile);
            return parseXML(bis, classType, validating, context);
        } catch (Exception e) {
            throwsXmlFailed(e, context);
        } finally {
            close(bis, context);
        }
        return null;
    }

    /**
     * An Deserialization of Ujo object.
     */
    public <T extends UjoTextable> T parseXML(InputStream inputStream, Class<T> classType, Object context) throws IllegalStateException {
        return parseXML(inputStream, classType, true, context);
    }

    /**
     * An Deserialization of Ujo object.
     */
    public <T extends UjoTextable> T parseXML(InputStream inputStream, Class<T> classType, boolean validate, Object context) throws IllegalStateException {
        return UjoHandlerXML.parseXML(inputStream, classType, validate, context, getUjoManager());
    }


    /** Write keys to XML including a XML header. A root tag is "body" by default. */
    public void saveXML(File xmlFile, UjoTextable ujo, XmlHeader xmlHeader, Object context) throws IOException {
        final OutputStream os = getOutputStream(xmlFile);
        try {
            saveXML(os, ujo, xmlHeader, context);
        } finally {
            os.close();
        }
    }

    /** Write keys to XML including a XML header. A root tag is "body" by default. */
    public void saveXML(OutputStream outStream, UjoTextable ujo, XmlHeader xmlHeader, Object context) throws IOException {
        final Writer writer = new OutputStreamWriter(outStream, UTF_8);
        try {
            saveXML(writer, ujo, xmlHeader, context);
        } finally {
            writer.close();
        }
    }

    /** Write keys to XML including a XML header. A root tag is "body" by default. */
    public void saveXML(Writer writer, UjoTextable ujo, XmlHeader xmlHeader, Object context) throws IOException {
        saveXML(writer, xmlHeader, ujo, context);
        writer.flush();
    }

    /** Write keys to XML including a XML header. */
    @SuppressWarnings("deprecation")
    public void saveXML(Writer writer, XmlHeader xmlHeader, UjoTextable ujo, Object context) throws IOException {
        if (xmlHeader == null ) {
            xmlHeader = new XmlHeader();
        }
        this.actionExport  = new UjoActionImpl(UjoAction.ACTION_XML_EXPORT , context);
        writer.write(xmlHeader.getHeader());
        if (xmlHeader.getComment() != null) {
            writeNewLine(writer);
            writer.write("<!-- ");
            writer.write(xmlHeader.getComment());
            writer.write(" -->");
        }

        @SuppressWarnings("unchecked")
        Key key = Property.of(xmlHeader.getRootElement(), ujo.getClass());
        printProperty(null, key, null, ujo, writer, false, xmlHeader.getAttributes());
    }

    /** Close an {@link Closeable} object */
    private void close(final Closeable closeable, final Object context) throws IllegalStateException {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            throwsXmlFailed(e, context);
        }
    }

    /** Throws an CSV exception. */
    private void throwsXmlFailed(Throwable e, Object context) throws IllegalStateException {
        throw new IllegalStateException("XML failed for a context: " + context, e);
    }

    /** Print attributes of the tag */
    @SuppressWarnings("unchecked")
    protected void printAttributes
    ( final UjoTextable ujo
    , final Writer writer
    ) throws IOException {

        // Write attributes:
        for (Key key : ujo.readKeys()) {
            Object value = key.of(ujo);

            if (value!=null
            && !key.isTypeOf(Ujo.class)
            &&  getUjoManager().isXmlAttribute(key)
            &&  ujo.readAuthorization(actionExport, key, value)
            && !getUjoManager().isTransient(key)
            ){
                final String valueStr = ujo.readValueString(key, actionExport);
                writer.write(SPACE);
                writer.write(key.getName());
                writer.write("=\"");
                printText2Xml(writer, valueStr);
                writer.write('"');
            }
        }
    }

    /** Write required keys to XML writer. */
    public void printProperties(Writer writer, UjoTextable ujo) throws IOException {
        printProperties(writer, ujo, ujo.readKeys());
    }

    /** Write required keys to a XML writer. */
    @SuppressWarnings("unchecked")
    public void printProperties(final Writer writer, UjoTextable ujo, final KeyList<?> keys) throws IOException {
        Key bodyProperty = getUjoManager().getXmlElementBody(ujo.getClass());

        for (Key key : keys) {
            Object value = key.of(ujo);

            if (value==null
            || !ujo.readAuthorization(actionExport, key, value)
            || getUjoManager().isXmlAttribute(key)
            || (value instanceof List && ((List)value).isEmpty())
            || getUjoManager().isTransient(key)
            ){
                continue;
            }

            if (value instanceof List) {
                final Class itemType = key instanceof ListKey ? ((ListKey)key).getItemType() : null ;

                if (itemType!=null
                && ((ListKey)key).isItemTypeOf(Ujo.class)) {
                    for (Object item : (List) value) {
                        Class itemClass = itemType!=item.getClass() ? item.getClass() : null ;
                        printProperty( ujo, key, itemClass, item, writer, false, null);
                    }
                } else {
                    final Class baseType2 = null; //value.getClass()!=key.getType() ? value.getClass() : null ;
                    printProperty(ujo, key, baseType2, value, writer, true, null);
                }


            } else if (bodyProperty==key) {
                writeNewLine(writer);
                printValue2XML(writer, Object.class, value, ujo, key, true);
            } else {
                final Class baseType = value.getClass()!=key.getType() ? value.getClass() : null ;
                printProperty(ujo, key, baseType, value, writer, false, null);
//          } else if (value instanceof Object[]) {
//                // PoP:TODO - unsupported now
            }
        }
    }

    /**
     * Print one Property
     * @param ujo       Ujo object
     * @param key  Ujo Key
     * @param valueType Is NOT mandatory attribute (can be null).
     * @param listType  Is NOT mandatory attribute (can be null).
     * @param value
     * @param writer
     * @param simpleProperty Item is not type Ujo
     * @param extendedAttributes Extended attributes
     * @throws java.io.IOException
     */
    private void printProperty
    ( final UjoTextable ujo
    , final Key key
    , final Class valueType
    , final Object value
    , final Writer writer
    , final boolean simpleProperty
    , final Map<String, String> extendedAttributes
    ) throws IOException {

        // --------------

        if (value==null
        ||  ujo!=null // NOT Root
        && !ujo.readAuthorization(actionExport, key, value)) {
            return; // listType;
        }

        writeNewLine(writer);
        writer.write('<');
        writer.write(key.getName());

        // Print extended attributes:
        if (extendedAttributes != null) {
            for (String keyName : extendedAttributes.keySet()) {
                writer.write(SPACE);
                writer.write(keyName);
                writer.write("=\"");
                printText2Xml(writer, extendedAttributes.get(keyName));
                writer.write('"');
            }
        }

        if (value instanceof UjoTextable) {
            printAttributes((UjoTextable) value, writer);
        }

        // Attributes: Class of a Value
        if (valueType!=null) {
            writer.write(SPACE);
            writer.write(ATTR_CLASS);
            writer.write("=\"");
            writer.write(valueType.getName());
            writer.write('"');
        }

        writer.write('>');
        if (simpleProperty && key instanceof ListKey) {
            List valueList = (List) value;
            for (int i = 0, max = valueList.size(); i < max; i++) {
                if (i>0) {
                    writer.write("</");
                    writer.write(key.getName());
                    writer.write('>');
                    writeNewLine(writer);
                    writer.write('<');
                    writer.write(key.getName());
                    writer.write('>');
                }
                printText2Xml(writer, getUjoManager().encodeValue(valueList.get(i), false));
            }
        } else {
           printValue2XML(writer, Object.class, value, ujo, key, simpleProperty);
        }
        writer.write("</");
        writer.write(key.getName());
        writer.write('>');

        //return listType;
    }

    /** Print "value" to XML. */
    public void printItem
    ( final Writer writer
    , final Class defaultType
    , final Object value
    , final UjoTextable ujo
    , final Key prop
    ) throws IOException {

        writeNewLine(writer);
        writer.write('<');
        writer.write(ATTR_ITEM);
        if (value!=null
        && (defaultType ==null
        || !defaultType.equals(value.getClass()))
        ){
            writer.write(SPACE);
            writer.write(ATTR_CLASS);
            writer.write("=\"");
            writer.write(value.getClass().getName());
            writer.write("\"");
        }
        writer.write('>');
        printValue2XML(writer, defaultType, value, ujo, prop, false);
        writer.write("</");
        writer.write(ATTR_ITEM);
        writer.write('>');
    }


    /** Print escaped text to XML */
    public void printText2Xml(final Appendable out, final String text) throws IOException {
        int length = text.length();
        for (int i=0; i<length; i++) {
            final char c = text.charAt(i);
            switch(c) {
                case '<' : out.append("&lt;"  ); break;
                case '>' : out.append("&gt;"  ); break;
                case '&' : out.append("&amp;" ); break;
                case '"' : out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default  : {
                    if (c<32) { // Condition including space: (c<=32)
                        out.append("&#");
                        out.append(Integer.toString(c));
                        out.append(';');
                    } else {
                        out.append(c);
                    }
                }
            }
        }
    }


    /** Conditionaly write new line. */
    public final void writeNewLine(final Appendable out) throws IOException {
        if (breakLineEnabled) {
            out.append('\n');
        }
    }

    // =========== VALUE CONVERSIONS ==========================

    /** Print "value" to XML. */
    @SuppressWarnings("unchecked")
    public void printValue2XML
    ( final Writer writer
    , final Class itemType
    , final Object value
    , final UjoTextable ujo
    , final Key prop
    , final boolean simpleProperty
    ) throws IOException {

        if (value == null) {
            // NOTHING;
        } else if (value instanceof UjoTextable) {
            printProperties(writer, (UjoTextable)value);
        } else if (!simpleProperty && value instanceof List) {
            for (Object item : (List<Object>) value) {
                printItem(writer, itemType, item, ujo, prop);
            }

//      // A Feature of the Future:
//      } else if (value instanceof Object[]) {
//          Object[] val = (Object[]) value;
//          for (int i=0; i<val.length; i++) {
//              printItem(writer, itemType, val[i]);
//          }
        } else {
            // A value from List or Array:
            final String str
            = true
            ? ujo.readValueString(prop, actionExport)
            : getUjoManager().encodeValue(value, false)
            ;
            printText2Xml(writer, str);
        }
    }

}
