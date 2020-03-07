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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.extensions.UjoTextable;
import org.ujorm.tools.xml.AbstractElement;
import org.ujorm.tools.xml.CommonXmlWriter;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.builder.XmlPrinter;
import static java.nio.charset.StandardCharsets.UTF_8;

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
    public static final String XML_HEADER = CommonXmlWriter.XML_HEADER;

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
    @Nullable
    public <T extends UjoTextable> T parseXML(File inputFile, Class<T> classType, boolean validating, Object context) throws IllegalStateException {
        InputStream bis = null;
        try {
            bis = getInputStream(inputFile);
            return parseXML(bis, classType, validating, context);
        } catch (RuntimeException | FileNotFoundException e) {
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
        try (OutputStream os = getOutputStream(xmlFile)) {
            saveXML(os, ujo, xmlHeader, context);
        }
    }

    /** Write keys to XML including a XML header. A root tag is "body" by default. */
    public void saveXML(OutputStream outStream, UjoTextable ujo, XmlHeader xmlHeader, Object context) throws IOException {
        try (Writer writer = new OutputStreamWriter(outStream, UTF_8)) {
            saveXML(writer, ujo, xmlHeader, context);
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
            writer.write("\n<!-- ");
            writer.write(xmlHeader.getComment());
            writer.write(" -->");
        }

        final XmlPrinter printer = new XmlPrinter(writer);
        try (XmlBuilder rootElement = printer.createElement(xmlHeader.getRootElement())) {
            printProperty(null, null, null, ujo, rootElement, false);
        }
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
        throw new IllegalUjormException("XML failed for a context: " + context, e);
    }

    /** Print attributes of the tag */
    @SuppressWarnings("unchecked")
    protected void printAttributes
    ( final UjoTextable ujo
    , final AbstractElement writer
    ) throws IOException {

        // Write attributes:
        for (Key key : ujo.readKeys()) {
            final Object value = key.of(ujo);

            if (value!=null
            && !key.isTypeOf(Ujo.class)
            &&  getUjoManager().isXmlAttribute(key)
            &&  ujo.readAuthorization(actionExport, key, value)
            && !getUjoManager().isTransient(key)
            ){
                final String valueStr = ujo.readValueString(key, actionExport);
                writer.setAttrib(key.getName(), valueStr);
            }
        }
    }

    /** Write required keys to XML writer. */
    public void printProperties(AbstractElement writer, UjoTextable ujo) throws IOException {
        printProperties(writer, ujo, ujo.readKeys());
    }

    /** Write required keys to a XML writer. */
    @SuppressWarnings("unchecked")
    public void printProperties(final AbstractElement writer, UjoTextable ujo, final KeyList<?> keys) throws IOException {
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
                        printProperty(ujo, key, itemClass, item, writer, false);
                    }
                } else {
                    final Class baseType2 = null; //value.getClass()!=key.getType() ? value.getClass() : null ;
                    printProperty(ujo, key, baseType2, value, writer, true);
                }


            } else if (bodyProperty==key) {
                printValue2XML(writer, Object.class, value, ujo, key, true);
            } else {
                final Class baseType = value.getClass()!=key.getType() ? value.getClass() : null ;
                printProperty(ujo, key, baseType, value, writer, false);
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
    ( @Nullable final UjoTextable ujo
    , @Nullable final Key key
    , @Nullable final Class valueType
    , @Nullable final Object value
    , @Nullable final AbstractElement parent
    , final boolean simpleProperty
    ) throws IOException {

        if (value == null
        ||  ujo != null // NOT Root
        && !ujo.readAuthorization(actionExport, key, value)) {
            return; // listType;
        }

        final AbstractElement writer = ujo == null
                ? parent // The root
                : parent.addElement(key.getName());

        if (value instanceof UjoTextable) {
            printAttributes((UjoTextable) value, writer);
        }

        // Attributes: Class of a Value
        if (valueType != null) {
            writer.setAttrib(ATTR_CLASS, valueType.getName());
        }

        if (simpleProperty && key instanceof ListKey) {
            final List valueList = (List) value;
            for (int i = 0, max = valueList.size(); i < max; i++) {
                final AbstractElement nextChild = i == 0
                        ? writer
                        : parent.addElement(key.getName());
                nextChild.addText(getUjoManager().encodeValue(valueList.get(i), false));
            }
        } else {
           printValue2XML(writer, Object.class, value, ujo, key, simpleProperty);
        }
    }

    /** Print "value" to XML. */
    public void printItem
    ( final AbstractElement parent
    , final Class defaultType
    , final Object value
    , final UjoTextable ujo
    , final Key prop
    ) throws IOException {

        final AbstractElement writer = parent.addElement(ATTR_ITEM);

        if (value!=null
        && (defaultType ==null
        || !defaultType.equals(value.getClass()))
        ){
            writer.setAttrib(ATTR_CLASS, value.getClass().getName());
        }
        printValue2XML(writer, defaultType, value, ujo, prop, false);
    }

    // =========== VALUE CONVERSIONS ==========================

    /** Print "value" to XML. */
    @SuppressWarnings("unchecked")
    public void printValue2XML
    ( final AbstractElement writer
    , final Class itemType
    , final Object value
    , final UjoTextable ujo
    , final Key prop
    , final boolean simpleProperty
    ) throws IOException {

        if (value == null) {
            // NOTHING;
        } else if (value instanceof UjoTextable) {
            printProperties(writer, (UjoTextable) value);
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
            writer.addText(str);
        }
    }

}
