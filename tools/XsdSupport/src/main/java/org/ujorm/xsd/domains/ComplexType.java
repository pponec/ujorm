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
public class ComplexType extends QuickUjoMid<ComplexType> {
    private static final KeyFactory<ComplexType> f = newCamelFactory(ComplexType.class);

    public static final Key<ComplexType, Sequence> SEQUENCE = f.newKey("xs:sequence");

    @XmlAttribute
    public static final Key<ComplexType, String> NAME = f.newKey("name");


    // Lock the Key factory
    static { f.lock(); }

    /** Set a complex type name */
    public void setName(String name) {
        NAME.setValue(this, name);
    }

    /** Add new Element */
    public void addElement(String name, String type, boolean list) {
        Sequence sequence = get(SEQUENCE);
        if (sequence == null) {
            sequence = new Sequence();
            set(SEQUENCE, sequence);
        }

        Element element = new Element();
        element.set(Element.NAME, name);
        element.set(Element.TYPE, type);
        element.setList(list);

        Sequence.ELEMENT.addItem(sequence, element);
    }
}
