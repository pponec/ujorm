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


import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoPropertyList;
import org.ujoframework.extensions.UjoTextable;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.ujoframework.Ujo;
import org.ujoframework.extensions.UjoAction;

/** Use an subclass on your own risk.
 * <br>Use an API of UjoManagerXML insted of. */
class UjoBuilderXML extends DefaultHandler {

    /** An Object hierarchy (Ujo or Element) */
    final protected LinkedList<Element> objectList = new LinkedList<Element>();

    /** Class of the root. */
    final protected Class rootType;

    /** Import action */
    final protected UjoAction actionImport;
    /** XML Element action */
    final protected UjoAction actionElement;

    /** Ignore missing property related to an ELEMENT or ATTRIBUTE during XML import. */
    private boolean ignoreMissingProp = false;

    // -- Temporarry fields --
    protected String  $elementName  = null;
    protected Class   $elementType  = null;
    protected UjoProperty $property = null;
    protected Class   $listType     = null;
    protected Class   $itemType     = null;
    protected Element $parentObj    = null;
    protected boolean $elementCont  = false;
    protected StringBuilder $value  = new StringBuilder(64);

    /** A list of XML attributes, one item is always a pair: attribute - value */
    protected ArrayList<String[]> $attributes = new ArrayList<String[]>();

    /** Constructor. */
    @SuppressWarnings("deprecation")
    /*protected*/ UjoBuilderXML(Class resultType, Object context) {
        this.rootType = resultType!=null ? resultType : Object.class ;
        this.actionImport  = new UjoActionImpl(UjoAction.ACTION_XML_IMPORT , context);
        this.actionElement = new UjoActionImpl(UjoAction.ACTION_XML_ELEMENT, context);
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    /**
     * <p>This event allows up to three name components for each element:</p>
     * <ol>
     * <li>the Namespace URI;</li>
     * <li>the local name; and</li>
     * <li>the qualified (prefixed) name.</li>
     * </ol>
     * @param namespaceURI The Namespace URI, or the empty string if the element has no Namespace URI
     *            or if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qualifiedName The qualified name (with prefix), or the empty string if qualified names are not available.
     * @param attribs The attributes attached to the element.  If there are no attributes, it shall be an empty Attributes object.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     **/
    @SuppressWarnings("unchecked")
    @Override
    public void startElement
    ( String namespaceURI
    , String localName
    , String qualifiedName
    , Attributes attribs
    ) throws SAXException {

        addBodyText($value);
        $elementName = localName.length()!=0 ? localName : qualifiedName ;
        $parentObj   = objectList.isEmpty() ? new Element(null) : objectList.getLast() ;
        $property    = $parentObj.isUjo()
	                 ? getUjoManager().findProperty($parentObj.ujo, $elementName, actionImport, true, !ignoreMissingProp)
		             : null ;
        $elementType = $parentObj.isRoot() ? rootType : null ;
        $listType    = null;
        $itemType    = null;
        $elementCont = false;
        $value.setLength(0);
        $attributes.clear();

        if (attribs!=null) for (int i=0; i<attribs.getLength(); i++) try {
            String attribName = attribs.getLocalName(i);
            if (isEmpty(attribName)) {
                attribName = attribs.getQName(i);
            }
            if (UjoManagerXML.ATTR_CLASS.equals(attribName)) {
                $elementType = Class.forName(attribs.getValue(i));
            } else if (UjoManagerXML.ATTR_LIST.equals(attribName)) {
                $listType = Class.forName(attribs.getValue(i));
            } else if (UjoManagerXML.ATTR_ITEM.equals(attribName)) {
                $itemType = Class.forName(attribs.getValue(i));
            } else {
                $attributes.add(new String[]{attribName, attribs.getValue(i)});
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }

        // Find an ELEMENT class:
        if ($elementType==null) {
            if ($parentObj.isRoot()) {
                throw new IllegalStateException("Tag <" + $elementName  + "> is missing attribute '" + UjoManagerXML.ATTR_CLASS + "'");
            }
            $elementType = $parentObj.isUjo()
            ? ($property instanceof UjoPropertyList ? ((UjoPropertyList)$property).getItemType() : $property.getType())
            : ($parentObj.itemType)
            ;
        }

        if ($elementType==null) {
            throw new IllegalStateException("Tag <" + $elementName + "> can't find class.");
        } else try {

            boolean isUJO = UjoTextable.class.isAssignableFrom($elementType);
            boolean isList = List.class.isAssignableFrom($elementType);
            if (isUJO || isList){
                $elementCont = true;
                Object container = $elementType.newInstance(); // UjoContainer
                objectList.add(isUJO
                    ? new Element((Ujo)container)
                    : new Element((List)container, $itemType)
		    );

                if (isUJO && !$attributes.isEmpty()) {
                    addAttributes((UjoTextable) container);
                }

                // Save container into parent:
                if ($parentObj.isUjo()) {
                    Ujo ujoParent = $parentObj.ujo;

                    if ($property instanceof UjoPropertyList) {
                        List list = (List) ujoParent.readValue($property);
                        if (list==null) {
                            if ($listType==null) {
                                $listType = ((UjoPropertyList)$property).getType();
                            }
                            list = (List) $listType.newInstance();
                            ujoParent.writeValue($property, list);
                        }
                        list.add(container);
                    } else if ($property!=null) {
                        ujoParent.writeValue($property, container);
                    }

                } else if ($parentObj.isList()) {
                    $parentObj.add(container);
                }
            }

        } catch (Exception ex) {
            throw new IllegalArgumentException( "Can't create instance of " + $elementType, ex );
        }
    }

    /** End the scope of a prefix-URI mapping. */
    @Override
    public void endElement
    ( String namespaceURI
    , String simpleName
    , String qualifiedName
    ) throws SAXException {
        // String elementName = simpleName.length()!=0 ? simpleName : qualifiedName ;

        if ($elementCont) {

            addBodyText($value);
            objectList.getLast().saveBody();

            if (objectList.size()>1) {
                objectList.removeLast();
            }
        } else if ($parentObj.ujo instanceof UjoTextable) {
            // Vrite Value:
            ((UjoTextable) $parentObj.ujo).writeValueString($property, $value.toString(), $elementType, actionImport);
            $value.setLength(0);
            $elementCont = true;
        } else if ($parentObj.isList()) {
            // Vrite Value/Container:
            $parentObj.list.add( getUjoManager().decodeValue($elementType, $value.toString()) );
            $value.setLength(0);
            $elementCont = true;
        }

    }

    /** Appned an part of tag value. */
    @Override
    public void characters(char buf[], int offset, int len) throws SAXException {
        $value.append(buf, offset, len);
    }

    // === UTILITIES ==============================

    /** Returns true, if text in not empty. */
    protected final boolean isEmpty(CharSequence text) {
        return text==null || text.length()==0;
    }

    /** Returns root */
    public Ujo getRoot() {
        return objectList.isEmpty() ? null : objectList.get(0).ujo ;
    }

    @SuppressWarnings("unchecked")
    public static <T extends UjoTextable> T parseXML(InputStream inputStream, Class<T> classType, boolean validate, Object context)
    throws ParserConfigurationException, SAXException, IOException {

        // Parser Factory
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(validate);

        // Parse the input
        UjoBuilderXML handler = new UjoBuilderXML(classType, context);
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse( inputStream, handler);

        return (T) handler.getRoot();
    }

    /** Add all XML attributes from internal buffer to UJO. */
    protected void addAttributes(final UjoTextable ujo) {
        for (String[] attrib : $attributes) {

            UjoProperty prop = getUjoManager().findProperty(ujo, attrib[0], actionElement, false, !ignoreMissingProp);
            if (prop!=null){
                ujo.writeValueString(prop, attrib[1], null, actionImport);
            }
        }
    }


    /** Ignore missing property related to an ELEMENT or ATTRIBUTE during XML import. */
    public boolean isIgnoreMissingProp() {
        return ignoreMissingProp;
    }

    /** Ignore missing property related to an ELEMENT or ATTRIBUTE during XML import. */
    public void setIgnoreMissingProp(boolean ignoreMissingProp) {
        this.ignoreMissingProp = ignoreMissingProp;
    }

    /** Returns DEFAULT UjoManager. */
    public UjoManager getUjoManager() {
        return UjoManager.getInstance();
    }

    /** Set a BodyText. */
    protected void addBodyText(CharSequence bodyText) {
        if (objectList.isEmpty()) { return; }
        objectList.getLast().addBody(bodyText);
    }


    /**
     * List include metaInfo.
     * @author Pavel Ponec
     */
    class Element {

        final List<Object> list;
        final Class itemType;
        final Ujo ujo;
        final UjoProperty bodyProperty;
        final StringBuilder body;

        public Element(List<Object> list, Class itemType, Ujo ujo) {
            this.list     = list;
            this.itemType = itemType;
            this.ujo      = ujo;
            //
            this.bodyProperty = ujo!=null ? getUjoManager().getXmlElementBody(ujo.getClass()) : null ;
            this.body = bodyProperty!=null ? new StringBuilder() : null ;
        }

        public Element(List<Object> list, Class itemType) {
            this(list, itemType, null);
        }

        public Element(Ujo ujo) {
            this(null, null, ujo);
        }

        /** Add new item to Listl */
        public void add(Object item) {
            list.add(item);
        }

        public boolean isRoot() {
            return ujo==null && list==null;
        }

        public boolean isUjo() {
            return ujo!=null;
        }
        public boolean isList() {
            return list!=null;
        }

        // ---------------------

        public void addBody(CharSequence text) {
            if (body!=null) {
                body.append(text);
            }
        }

        /** Save body by the body property. */
        public void saveBody() {
            if (body!=null) {
                String bodyText = body.toString().trim();
                if (bodyText.length()==0) { return; }

                if (ujo instanceof UjoTextable) {
                    ((UjoTextable)ujo).writeValueString(bodyProperty, bodyText, null, actionImport);
                } else {
                    final Object bodyObj = getUjoManager().decodeValue(bodyProperty, bodyText);
                    ((UjoTextable)ujo).writeValue(bodyProperty, bodyObj);
                }
            }
        }

    }
}
