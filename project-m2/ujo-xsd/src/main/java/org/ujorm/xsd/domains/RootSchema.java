/*
 *  Copyright 2014-2022 Pavel Ponec
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
package org.ujorm.xsd.domains;

import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.implementation.quick.SmartUjo;

/**
 *
 * @author Pavel Ponec
 */
public class RootSchema extends SmartUjo<RootSchema> {
    private static final KeyFactory<RootSchema> f = newCamelFactory(RootSchema.class);

    public static final ListKey<RootSchema, SimpleType> SIMPLE_STYPE = f.newListKey("xs:simpleType");
    public static final ListKey<RootSchema, ComplexType> COMPLEX_TYPE = f.newListKey("xs:complexType");
    public static final Key<RootSchema, Element> ELEMENT = f.newKey("xs:element");

    // --- Attributes ---

    @XmlAttribute
    public static final Key<RootSchema, String> ATTRIBUTE_FORM_DEFAULT = f.newKey("attributeFormDefault", "unqualified");
    @XmlAttribute
    public static final Key<RootSchema, String> ELEMENT_FORM_DEFAULT = f.newKey("elementFormDefault", "qualified");
    @XmlAttribute
    public static final Key<RootSchema, String> XMLNS_XS = f.newKey("xmlns:xs", "http://www.w3.org/2001/XMLSchema");

    // Lock the Key factory
    static {
        f.lock();
    }

}
