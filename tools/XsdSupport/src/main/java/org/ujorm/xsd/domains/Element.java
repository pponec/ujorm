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
package org.ujorm.xsd.domains;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.implementation.quick.QuickUjoMid;

/**
 *
 * @author Pavel Ponec
 */
public class Element extends QuickUjoMid<Element> {
    private static final KeyFactory<Element> f = newCamelFactory(Element.class);

    public static final Key<Element, ComplexType> COMPLEX_TYPE = f.newKey("xs:complexType");

    @XmlAttribute
    public static final Key<Element, String> TYPE = f.newKey("type", "xs:string");
    @XmlAttribute
    public static final Key<Element, String> NAME = f.newKey("name");


    // Lock the Key factory
    static { f.lock(); }

    /** Set name and type */
    public void set(String name, String type) {
        NAME.setValue(this, name);
        TYPE.setValue(this, type);
    }

}
