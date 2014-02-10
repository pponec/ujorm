/*
 *  Copyright 2014-2014 Pavel Ponec
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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.extensions.StringWrapper;
import org.ujorm.orm.metaModel.MetaRoot;
import org.ujorm.xsd.domains.ComplexType;
import org.ujorm.xsd.domains.Element;
import org.ujorm.xsd.domains.RootSchema;
import org.ujorm.xsd.domains.SimpleType;

/**
 *
 * @author Pavel Ponec
 */
public class XsdBuilder {

    /** XSD prefix */
    private static final String XSD = "xsd:";
    private static final UjoManager manager = UjoManager.getInstance();
    private final Ujo ujoRoot;
    private final RootSchema schemaRoot = new RootSchema();
    private Map<Class, String> map = new TreeMap<Class, String>();

    public XsdBuilder(MetaRoot ujo) {
        this.ujoRoot = ujo;
        initTypes();
        loadDataTypes(ujo.getClass());
        saveDataTypes();
    }

    /** Init base types */
    private void initTypes() {
        map.put(String.class, XSD + "string");
        map.put(Integer.class, XSD + "integer");
        map.put(BigInteger.class, XSD + "integer");
        map.put(Long.class, XSD + "long");
        map.put(Short.class, XSD + "short");
        map.put(BigDecimal.class, XSD + "decimal");
        map.put(Float.class, XSD + "float");
        map.put(Double.class, XSD + "double");
        map.put(Boolean.class, XSD + "boolean");
        map.put(Byte.class, XSD + "byte");
        map.put(java.util.Date.class, XSD + "dateTime");
        map.put(java.sql.Date.class, XSD + "date");
    }

    /** Load data */
    private void loadDataTypes(Class<?> ujoClass) {
        if (map.containsKey(ujoClass)) {
            return;
        }
        map.put(ujoClass, ujoClass.getSimpleName());
        for (Key<?, ?> key : createUjo(ujoClass).readKeys()) {
            if (!manager.isTransientProperty(key)) {
                assignDataType(key);
            }
        }
    }

    /** Save data types */
    private void saveDataTypes() {
        // Enumerators:
        for (Class type : map.keySet()) {
            if (type.isEnum()) {
                SimpleType simpleType = new SimpleType();
                simpleType.setName(map.get(type));
                
                for (Object enumItem : type.getEnumConstants()) {
                    if (enumItem instanceof StringWrapper) {
                        simpleType.addEnumerationValue(((StringWrapper)enumItem).exportToString());
                    } else {
                        simpleType.addEnumerationValue(((Enum)enumItem).name());
                    }
                }
            }
        }
        // Ujo:
        for (Class type : map.keySet()) {
            if (Ujo.class.isAssignableFrom(type)) {
                ComplexType complexType = new ComplexType();
                complexType.setName(map.get(complexType));

                for (Key<?,?> key : createUjo(type).readKeys()) {
                    if (manager.isTransientProperty(key)) {
                        continue;
                    }
                    complexType.addElement(key.getName(), map.get(key.getType()));
                }
            }
        }

        // The main element:
        Element mainElement = new Element();
        mainElement.set("body", map.get(ujoRoot.getClass()));
        RootSchema.ELEMENT.setValue(schemaRoot, mainElement);
    }

    /** Print */
    public String print() {
        CharArrayWriter writer = new CharArrayWriter(256);
        try {
            String xmlHeader = "";
            UjoManagerXML.getInstance().saveXML(writer, schemaRoot, xmlHeader, schemaRoot);
        } catch (IOException ex) {
            String msg = "Can't export model into XML";
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, msg, ex);
        }
        return writer.toString();
    }

    /** Create new instance of Ujo */
    private Ujo createUjo(Class<?> ujoClass) {
        try {
            return (Ujo) ujoClass.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Can't create instance for " + ujoClass, ex);
        }
    }

    /** Assign data type */
    private void assignDataType(Key<?, ?> key) {
        final Class keyType = key.getType();
        if (!map.containsKey(keyType)) {
            if (Ujo.class.isInstance(keyType)) {
                loadDataTypes(keyType);
            }
        } else
        if (keyType.isEnum()) {
           map.put(keyType, keyType.getSimpleName());
        }
        else {
           map.put(keyType, map.get(String.class));
        }
    }

}
