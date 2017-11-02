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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.extensions.UjoTextable;
import org.ujorm.tools.Assert;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/** Use an subclass on your own risk.
 * <br>Use an API of UjoManagerXML instead of. */
final class UjoHandlerXML extends DefaultHandler {

    /** An Object hierarchy (Ujo or Element) */
    private Element[] elementList = new Element[16];
    int lastElement = -1;

    /** Class of the root. */
    final protected Class rootType;

    /** Import action */
    final protected UjoAction actionImport;
    /** UjoManager */
    final protected UjoManager ujoManager;

    /** Ignore missing key related to an ELEMENT or ATTRIBUTE during XML import. */
    private boolean ignoreMissingProp = false;

    // -- Temporary fields --
    protected String  $elementName  = null;
    protected Class   $elementType  = null;
    protected Key     $key     = null;
    protected ListKey $propertyList = null;
    protected Class   $listType     = null;
    protected Class   $itemType     = null;
    protected Element $parentObj    = null;
    protected boolean $elementCont  = false;
    protected StringBuilder $value  = new StringBuilder(64);

    /** A list of XML attributes, one item is always a pair: attribute - value */
    protected ArrayList<String[]> $attributes = new ArrayList<>();

    /** Constructor. */
    @SuppressWarnings("deprecation")
    /*protected*/ UjoHandlerXML(Class resultType, Object context, UjoManager ujoManager) {
        this.rootType = resultType!=null ? resultType : Object.class ;
        this.actionImport  = new UjoActionImpl(UjoAction.ACTION_XML_IMPORT , context);
        this.ujoManager    = ujoManager;
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
        $parentObj   = lastElement<0 ? new Element() : getLastElement() ;
        $key    = $parentObj.isUjo()
                     ? $parentObj.ujo.readKeys().findDirectKey($parentObj.ujo, $elementName, actionImport, true, !ignoreMissingProp)
                     : null ;
        $propertyList = $key instanceof ListKey ? (ListKey) $key : null;
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
            if (null != attribName) switch (attribName) {
                case UjoManagerXML.ATTR_CLASS:
                    $elementType = Class.forName(attribs.getValue(i));
                    break;
                case UjoManagerXML.ATTR_LIST:
                    $listType = Class.forName(attribs.getValue(i));
                    break;
                case UjoManagerXML.ATTR_ITEM:
                    $itemType = Class.forName(attribs.getValue(i));
                    break;
                default:
                    $attributes.add(new String[]{attribName, attribs.getValue(i)});
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalUjormException(ex.getMessage(), ex);
        }

        // Find an ELEMENT class:
        if ($elementType==null) {
            Assert.isFalse($parentObj.isRoot()
                    , "Tag <{}> has missing attribute '{}'"
                    , $elementName
                    , UjoManagerXML.ATTR_CLASS);
            $elementType = $parentObj.isUjo()
            ? ($propertyList != null ? $propertyList.getItemType() : $key.getType())
            : ($parentObj.itemType)
            ;
        }

        Assert.notNull($elementType, "Tag <{}> can't find class."
                , $elementName);
        try {
            boolean isUJO = UjoTextable.class.isAssignableFrom($elementType);
            boolean isList = List.class.isAssignableFrom($elementType);
            if (isUJO || isList) {
                $elementCont = true;
                Object container = $elementType.newInstance(); // UjoContainer

                if (isUJO) newElement().init((Ujo)container);
                else       newElement().init((List)container, $itemType);

                if (isUJO && !$attributes.isEmpty()) {
                    addAttributes((UjoTextable) container, ignoreMissingProp || $parentObj.isRoot());
                }

                // Save container into parent:
                if ($parentObj.isUjo()) {
                    Ujo ujoParent = $parentObj.ujo;

                    if ($propertyList != null) {
                        List list = (List) $propertyList.of(ujoParent);
                        if (list==null) {
                            if ($listType==null
                            ||  $listType.isInterface()) {
                                list = $propertyList.getList(ujoParent);
                            } else {
                                list = (List) $listType.newInstance();
                                ujoParent.writeValue($key, list);
                            }
                        }
                        list.add(container);
                    } else if ($key!=null) {
                        ujoParent.writeValue($key, container);
                    }

                } else if ($parentObj.isList()) {
                    $parentObj.add(container);
                }
            }

        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("Can't create instance of " + $elementType, e);
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
            getLastElement().saveBody();

            if (lastElement>0) {
                lastElement--;
            }
        } else if ($parentObj.ujo instanceof UjoTextable) {
            // Vrite Value:
            if ($propertyList != null) {
                List oldValue = (List) $parentObj.ujo.readValue($key); // The original solution for a back compatibility
                if (oldValue != null) {
                   final Object value = ujoManager.decodeValue($elementType, $value.toString());
                   oldValue.add(value);
                } else {
                   ((UjoTextable) $parentObj.ujo).writeValueString($key, $value.toString(), $elementType, actionImport);
                }
            } else {
               ((UjoTextable) $parentObj.ujo).writeValueString($key, $value.toString(), $elementType, actionImport);
            }
            $value.setLength(0);
            $elementCont = true;
        } else if ($parentObj.isList()) {
            // Vrite Value/Container:
            $parentObj.list.add( ujoManager.decodeValue($key, $value.toString(), $elementType) );
            $value.setLength(0);
            $elementCont = true;
        }
    }

    /** Append an part of tag value. */
    @Override
    public void characters(char[] buf, int offset, int len) throws SAXException {
        $value.append(buf, offset, len);
    }

    // === UTILITIES ==============================

    /** Returns true, if text in not empty. */
    protected final boolean isEmpty(final CharSequence text) {
        return text==null || text.length()==0;
    }

    /** Returns root */
    public Ujo getRoot() {
        return lastElement<0 ? null : elementList[0].ujo ;
    }

    @SuppressWarnings("unchecked")
    public static <T extends UjoTextable> T parseXML(InputStream inputStream, Class<T> classType, boolean validate, Object context, UjoManager ujoManager)
    throws IllegalStateException {

        // Parser Factory
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(validate);

        // Parse the input
        UjoHandlerXML handler = new UjoHandlerXML(classType, context, ujoManager);
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputStream, handler);
        } catch (RuntimeException | ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalUjormException("Parser exception with context: " + context, e);
        }

        return (T) handler.getRoot();
    }

    /** Add all XML attributes from internal buffer to UJO. */
    protected void addAttributes(final UjoTextable ujo, boolean ignoreMissingProp) {
        for (String[] attrib : $attributes) {
            final Key key = ujo.readKeys().findDirectKey
                    (ujo, attrib[0], actionImport, false, !ignoreMissingProp);
            if (key != null){
                ujo.writeValueString(key, attrib[1], null, actionImport);
            }
        }
    }


    /** Ignore missing key related to an ELEMENT or ATTRIBUTE during XML import. */
    public boolean isIgnoreMissingProp() {
        return ignoreMissingProp;
    }

    /** Ignore missing key related to an ELEMENT or ATTRIBUTE during XML import. */
    public void setIgnoreMissingProp(boolean ignoreMissingProp) {
        this.ignoreMissingProp = ignoreMissingProp;
    }

    /** Set a BodyText. */
    protected void addBodyText(CharSequence bodyText) {
        if (lastElement<0) { return; }
        getLastElement().addBody(bodyText);
    }

    /** Returns the getLastKey element from the object list  */
    protected final Element getLastElement() {
        return elementList[lastElement];
    }

    /** Returns the new element from the object list  */
    protected final Element newElement() {
        if (++lastElement==elementList.length) {
            Element[] newElem = new Element[lastElement + 32];
            System.arraycopy(elementList, 0, newElem, 0, elementList.length);
            elementList = newElem;
        }

        Element result = elementList[lastElement];
        if (result==null) {
            result = new Element();
            elementList[lastElement] = result;
        }
        return elementList[lastElement];
    }


    /**
     * List includes metaInfo.
     * @author Pavel Ponec
     */
    final class Element {

        List<Object> list;
        Class itemType;
        Ujo ujo;
        Key bodyProperty;
        StringBuilder body;

        public void init(List<Object> list, Class itemType, Ujo ujo) {
            this.list     = list;
            this.itemType = itemType;
            this.ujo      = ujo;
            //
            this.bodyProperty = ujo!=null ? ujoManager.getXmlElementBody(ujo.getClass()) : null ;
            this.body = bodyProperty!=null ? new StringBuilder() : null ;
        }

        public void init(List<Object> list, Class itemType) {
            init(list, itemType, null);
        }

        public void init(Ujo ujo) {
            init(null, null, ujo);
        }

        /** Add new item to List */
        public void add(Object item) {
            list.add(item);
        }

        public boolean isRoot() {
            return ujo==list; // ujo==null && list==null;
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

        /** Save body by the body key. */
        public void saveBody() {
            if (body!=null) {
                String bodyText = body.toString().trim();
                if (bodyText.isEmpty()) { return; }

                if (ujo instanceof UjoTextable) {
                    ((UjoTextable)ujo).writeValueString(bodyProperty, bodyText, null, actionImport);
                } else {
                    final Object bodyObj = ujoManager.decodeValue(bodyProperty, bodyText, null);
                    ((UjoTextable)ujo).writeValue(bodyProperty, bodyObj);
                }
            }
        }

    }
}
