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
package org.ujorm.xsd.domains;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.implementation.quick.SmartUjo;

/**
 *
 * @author Pavel Ponec
 */
public class SimpleType extends SmartUjo<SimpleType> {
    private static final KeyFactory<SimpleType> f = newCamelFactory(SimpleType.class);

    public static final Key<SimpleType, Restriction> RESTRICTION = f.newKey("xs:restriction");

    @XmlAttribute
    public static final Key<SimpleType, String> NAME = f.newKey("name", "");

    // Lock the Key factory
    static { f.lock(); }


    public void setName(String name) {
        NAME.setValue(this, name);
    }

    public void addEnumerationValue(String enumValue) {
        Restriction restriction = get(RESTRICTION);
        if (restriction == null) {
            restriction = new Restriction();
            set(RESTRICTION, restriction);
        }

        Enumeration enumeration = new Enumeration();
        enumeration.set(Enumeration.VALUE, enumValue);
        Restriction.ENUMERATION.addItem(restriction, enumeration);
    }
}
