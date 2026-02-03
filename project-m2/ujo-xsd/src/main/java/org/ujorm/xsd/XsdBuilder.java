/*
 *  Copyright 2014-2026 Pavel Ponec
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
package org.ujorm.xsd;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.core.XmlHeader;
import org.ujorm.extensions.StringWrapper;
import org.ujorm.xsd.domains.ComplexType;
import org.ujorm.xsd.domains.Element;
import org.ujorm.xsd.domains.RootSchema;
import org.ujorm.xsd.domains.SimpleType;

/**
 * The class build XSD file according to Ujo object.<br>
 * Example of the use<br>
 * <pre class=pre>[@code
 *    String xsd = new XsdBuilder(Company.class).print();
 * }</pre>
 * @author Pavel Ponec
 */
public class XsdBuilder {

    /** XSD prefix */
    private static final String XSD = "xs:";
    private static final UjoManager manager = UjoManager.getInstance();

    private final Class<? extends Ujo> rootClass;
    private final RootSchema rootSchema = new RootSchema();
    private final Map<Class, String> typeMap = new HashMap<Class, String>();
    private final List<Class> typeList = new ArrayList<Class>();
    private final List<Class> enumList = new ArrayList<Class>();

    public XsdBuilder(Class<? extends Ujo>  ujoClass) {
        this.rootClass = ujoClass;
        loadBaseTypes();
        loadUjoType(rootClass);
        buildMetaModel();
    }

    /** Init base types */
    private void loadBaseTypes() {
        typeMap.put(String.class, Element.TYPE.getDefault());
        typeMap.put(Integer.class, XSD + "int");
        typeMap.put(BigInteger.class, XSD + "integer");
        typeMap.put(Long.class, XSD + "long");
        typeMap.put(Short.class, XSD + "short");
        typeMap.put(BigDecimal.class, XSD + "decimal");
        typeMap.put(Float.class, XSD + "float");
        typeMap.put(Double.class, XSD + "double");
        typeMap.put(Boolean.class, XSD + "boolean");
        typeMap.put(Byte.class, XSD + "byte");
        typeMap.put(java.util.Date.class, XSD + "dateTime");
        typeMap.put(java.sql.Date.class, XSD + "date");
    }

    /** Load type of persistent keys */
    private void loadUjoType(Class<?> ujoClass) {
        typeMap.put(ujoClass, ujoClass.getSimpleName());
        typeList.add(ujoClass);

        for (Key<?, ?> key : createUjo(ujoClass).readKeys()) {
             assignKeyType(key);
        }
    }

    /** Assign data type to the {@code typeMap} */
    private void assignKeyType(Key<?, ?> key) {
        if (manager.isTransient(key)) {
            return;
        }
        final Class keyType = key instanceof ListKey
                ? ((ListKey)key).getItemType()
                :  key.getType();

        if (!typeMap.containsKey(keyType)) {
            if (Ujo.class.isAssignableFrom(keyType)) {
                loadUjoType(keyType);
            } else if (keyType.isEnum()) {
                typeMap.put(keyType, keyType.getSimpleName());
                enumList.add(keyType);
            } else {
                typeMap.put(keyType, Element.TYPE.getDefault());
            }
        }
    }

    /** Save data types */
    private void buildMetaModel() {
        // Reverse the order:
        Collections.reverse(enumList);
        Collections.reverse(typeList);

        // Enumerators:
        for (Class type : enumList) {
            final SimpleType simpleType = new SimpleType();
            RootSchema.SIMPLE_STYPE.addItem(rootSchema, simpleType);
            simpleType.setName(typeMap.get(type));

            for (Object enumItem : type.getEnumConstants()) {
                final String enumValue = enumItem instanceof StringWrapper
                        ? ((StringWrapper)enumItem).exportToString()
                        : ((Enum)enumItem).name() ;
                simpleType.addEnumerationValue(enumValue);
            }
        }

        // Ujo types:
        for (Class type : typeList) {
            ComplexType complexType = new ComplexType();
            RootSchema.COMPLEX_TYPE.addItem(rootSchema, complexType);
            complexType.setName(typeMap.get(type));

            for (Key<?,?> key : createUjo(type).readKeys()) {
                if (manager.isTransient(key)) {
                    continue;
                }
                final boolean list = key instanceof ListKey;
                final Class keyType = list
                        ? ((ListKey)key).getItemType()
                        : key.getType();
                if (manager.isXmlAttribute(key)) {
                   complexType.addAttribute(key.getName(), typeMap.get(keyType));
                } else {
                   complexType.addElement(key.getName(), typeMap.get(keyType), list);
                }
            }
        }

        // The main element:
        Element mainElement = new Element();
        mainElement.set("body", typeMap.get(rootClass));
        mainElement.hideMinOccurs(true);
        RootSchema.ELEMENT.setValue(rootSchema, mainElement);
    }

    /** Print */
    public String print() {
        StringBuilder writer = new StringBuilder(256);
        try {
            final String xmlHeader = null;
            print(xmlHeader, writer);
        } catch (IOException ex) {
            String msg = "Can't export model into XML";
            throw new IllegalUjormException(msg, ex);
        }
        return writer.toString();
    }

    /** Print to writer */
    public void print(String xmlHeader, Appendable writer) throws IOException {
        final XmlHeader header = new XmlHeader(XSD + "schema");
        header.setHeader(xmlHeader);
        UjoManagerXML.getInstance().saveXML(writer, header, rootSchema, rootSchema);
    }

    /** Create new instance of Ujo */
    private Ujo createUjo(Class<?> ujoClass) {
        try {
            return (Ujo) ujoClass.newInstance();
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("Can't create instance for " + ujoClass, e);
        }
    }
}
