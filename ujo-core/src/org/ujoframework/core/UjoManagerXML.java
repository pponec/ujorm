/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.extensions.UjoPropertyList;
import org.ujoframework.extensions.UjoTextable;
import org.ujoframework.implementation.map.MapProperty;
import org.xml.sax.SAXException;

/**
 * Ujo Manager for instances type of UjoTextAccess.
 * <br>Method supports for example persistentions type of XML
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
    
    /** XML HEADER */
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    
    /** A name of Java Class of XML attribute. */
    public static final String ATTR_CLASS      = "javaClass";
    /** A name of Java List Class of a XML attribute. */
    public static final String ATTR_LIST       = "javaList";
    /** A name of Java Class of XML List attribute. */
    public static final String ATTR_ITEM       = "javaItem";
    
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
    public static final UjoManagerXML getInstance() {
        return new UjoManagerXML();
    }
    
    /** Create Ujo from XMl file */
    public <T extends UjoTextable> T parseXML(File inputFile, Class<T> classType, Object context) throws ParserConfigurationException, SAXException, IOException {
        return parseXML(inputFile, classType, true, context);
    }
    
    /** Create Ujo from XMl file */
    public <T extends UjoTextable> T parseXML(File inputFile, Class<T> classType, boolean validating, Object context) throws ParserConfigurationException, SAXException, IOException {
        final InputStream bis = getInputStream(inputFile);
        try {
            return parseXML(bis, classType, validating, context);
        } finally {
            bis.close();
        }
    }
    
    /**
     * An Deserialization of Ujo object.
     */
    public <T extends UjoTextable> T parseXML(InputStream inputStream, Class<T> classType, Object context) throws ParserConfigurationException, SAXException, IOException {
        return parseXML(inputStream, classType, true, context);
    }
    
    /**
     * An Deserialization of Ujo object.
     */
    public <T extends UjoTextable> T parseXML(InputStream inputStream, Class<T> classType, boolean validate, Object context) throws ParserConfigurationException, SAXException, IOException {
        return UjoBuilderXML.parseXML(inputStream, classType, validate, context);
    }
    
    
    /** Write properties to XML include XML header. A root tag is "body". */
    public void saveXML(File xmlFile, UjoTextable ujo, String xmlHeader, Object context) throws IOException {
        final OutputStream os = getOutputStream(xmlFile);
        try {
            saveXML(os, ujo, xmlHeader, context);
        } finally {
            os.close();
        }
    }
    
    /** Write properties to XML include XML header. A root tag is "body". */
    public void saveXML(OutputStream outStream, UjoTextable ujo, String xmlHeader, Object context) throws IOException {
        final Writer writer = new OutputStreamWriter(outStream, UTF_8);
        try {
            saveXML(writer, ujo, xmlHeader, context);
        } finally {
            writer.close();
        }
    }
    
    /** Write properties to XML include XML header. A root tag is "body". */
    public void saveXML(Writer writer, UjoTextable ujo, String xmlHeader, Object context) throws IOException {
        saveXML(writer, "body", ujo, xmlHeader, context);
        writer.flush();
    }
    
    /** Write properties to XML include a XML header. */
    public void saveXML(Writer writer, String tag, UjoTextable ujo, String xmlHeader, Object context) throws IOException {
        this.actionExport  = new UjoActionImpl(UjoAction.ACTION_XML_EXPORT , context);
        this.actionElement = new UjoActionImpl(UjoAction.ACTION_XML_ELEMENT, context);
        writer.write(xmlHeader!=null ? xmlHeader : XML_HEADER );
        
        @SuppressWarnings("unchecked")
        MapProperty property = new MapProperty(tag, ujo.getClass());
        printProperty(null, property, null, null, ujo, writer);
        
    }
    
    /** Print attributes of the tag */
    protected void printAttributes
    ( final UjoTextable ujo
    , final Writer writer
    ) throws IOException {
        
        // Write attributes:
        for (UjoProperty property : ujo.readProperties()) {
            Object value = ujo.readValue(property);  // it is always a direct property from readProperties()
            
            if (value!=null
            && !Ujo.class.isAssignableFrom(property.getType())
            && !ujo.readAuthorization(actionElement, property, value)
            &&  ujo.readAuthorization(actionExport , property, value)
            && !getUjoManager().isTransientProperty(property)
            ){
                final String valueStr = ujo.readValueString(property, actionExport);
                writer.write(' ');
                writer.write(property.getName());
                writer.write("=\"");
                printText2Xml(writer, valueStr);
                writer.write('"');
            }
        }
    }
    
    /** Write required properties to XML writer. */
    public void printProperties(Writer writer, UjoTextable ujo) throws IOException {
        printProperties(writer, ujo, ujo.readProperties());
    }
    
    /** Write required properties to a XML writer. */
    public void printProperties(Writer writer, UjoTextable ujo, UjoProperty[] properties) throws IOException {
        for (UjoProperty property : properties) {
            Object value = ujo.readValue(property); // Only direct property from readProperties()
            
            if (value==null
            || !ujo.readAuthorization(actionExport , property, value)
            || !ujo.readAuthorization(actionElement, property, value)
            || (value instanceof List && ((List)value).isEmpty())
            || getUjoManager().isTransientProperty(property)
            ){
                continue;
            }
            final Class baseType = value.getClass()!=property.getType() ? value.getClass() : null ;
            final Class itemType = property instanceof UjoPropertyList ? ((UjoPropertyList)property).getItemType() : null ;
            
            if (value instanceof List) {
                for (Object item : (List) value) {
                    Class itemClass = itemType!=item.getClass() ? item.getClass() : null ;
                    printProperty( ujo, property, itemClass, baseType, item, writer );
                }
            } else {
                printProperty(ujo, property, baseType, null, value, writer);
//          } else if (value instanceof Object[]) {
//                // PoP:TODO
            }
        }
    }
    
    /**
     * Print one Property
     * @param ujo       Ujo object
     * @param property  Property
     * @param valueType Is NOT mandatory attribute (can be null).
     * @param listType  Is NOT mandatory attribute (can be null).
     * @param value
     * @param writer
     * @param actionExport
     * @throws java.io.IOException
     */
    private void printProperty
    ( final UjoTextable ujo
    , final UjoProperty property
    , final Class valueType
    , Class listType
    , final Object value
    , final Writer writer
    ) throws IOException {
        
        // --------------
        
        if (value==null
        ||  ujo!=null // NOT Root
        && !ujo.readAuthorization(actionExport, property, value)) {
            return; // listType;
        }
        
        writeNewLine(writer);
        writer.write('<');
        writer.write(property.getName());
        if (value instanceof UjoTextable) {
            printAttributes((UjoTextable) value, writer);
        }
        
        // Attributes: Class of a List
        if (listType!=null) {
            writer.write(' ');
            writer.write(ATTR_LIST);
            writer.write("=\"");
            writer.write(listType.getName());
            writer.write('"');
            listType = null;
        }
        
        // Attributes: Class of a Value
        if (valueType!=null) {
            writer.write(' ');
            writer.write(ATTR_CLASS);
            writer.write("=\"");
            writer.write(valueType.getName());
            writer.write('"');
        }
        
        writer.write('>');
        printValue2XML(writer, Object.class, value, ujo, property);
        writer.write("</");
        writer.write(property.getName());
        writer.write('>');
        
        //return listType;
    }
    
    /** Print "value" to XML. */
    public void printItem
    ( final Writer writer
    , final Class defaultType
    , final Object value
    , final UjoTextable ujo
    , final UjoProperty prop
    ) throws IOException {
        
        writeNewLine(writer);
        writer.write('<');
        writer.write(ATTR_ITEM);
        if (value!=null
        && (defaultType ==null
        || !defaultType.equals(value.getClass()))
        ){
            writer.write(' ');
            writer.write(ATTR_CLASS);
            writer.write("=\"");
            writer.write(value.getClass().getName());
            writer.write("\"");
        }
        writer.write('>');
        printValue2XML(writer, defaultType, value, ujo, prop);
        writer.write("</");
        writer.write(ATTR_ITEM);
        writer.write('>');
    }
    
    
    /** Print escaped text to XML */
    public void printText2Xml(final Writer writer, final String text) throws IOException {
        int length = text.length();
        for (int i=0; i<length; i++) {
            final char c = text.charAt(i);
            switch(c) {
                case '<' : writer.write("&lt;"  ); break;
                case '>' : writer.write("&gt;"  ); break;
                case '&' : writer.write("&amp;" ); break;
                case '"' : writer.write("&quot;"); break;
                case '\'': writer.write("&apos;"); break;
                default  : {
                    if (c<=32) {
                        writer.write("&#");
                        writer.write(Integer.toString(c));
                        writer.write(';');
                    } else {
                        writer.write(c);
                    }
                }
            }
        }
    }
    
    
    /** Conditionaly write new line. */
    public final void writeNewLine(final Writer writer) throws IOException {
        if (breakLineEnabled) {
            writer.write('\n');
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
    , final UjoProperty prop
    ) throws IOException {
        
        if (value == null) {
            // NOTHING;
        } else if (value instanceof UjoTextable) {
            printProperties(writer, (UjoTextable)value);
        } else if (value instanceof List) {
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
