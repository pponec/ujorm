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
import org.ujorm.UjoAction;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.implementation.quick.SmartUjo;
import static org.ujorm.UjoAction.*;

/**
 *
 * @author Pavel Ponec
 */
public class Element extends SmartUjo<Element> {
    private static final KeyFactory<Element> f = newCamelFactory(Element.class);

    public static final Key<Element, ComplexType> COMPLEX_TYPE = f.newKey("xs:complexType");
    @XmlAttribute
    public static final Key<Element, String> NAME = f.newKey("name");
    @XmlAttribute
    public static final Key<Element, String> TYPE = f.newKey("type", "xs:string");
    @XmlAttribute
    public static final Key<Element, Integer> MIN_OCCURS = f.newKey("minOccurs", 0);
    @XmlAttribute
    public static final Key<Element, String> MAX_OCCURS = f.newKey("maxOccurs", "");

    // Lock the Key factory
    static { f.lock(); }

    /** Unbounded */
    private static final String OCCURS_UNBOUNDED = "unbounded";

    /** Set name and type */
    public void set(String name, String type) {
        NAME.setValue(this, name);
        TYPE.setValue(this, type);
    }

    /** Assign list type */
    public void setList(boolean list) {
        set(MAX_OCCURS, list ? OCCURS_UNBOUNDED : null);
    }

    /** Hide attribute {@link #MIN_OCCURS} */
    public void hideMinOccurs(boolean hide) {
        set(MIN_OCCURS, hide ? -1 : null);
    }

    /** Do not print a default value of the key {@link #MAX_OCCURS}. */
    @Override
    @SuppressWarnings("unchecked")
    public boolean readAuthorization( final UjoAction action, final Key key, final Object value) {
        if (action.getType() == ACTION_XML_EXPORT) {
            return !(key == MIN_OCCURS && ((Integer) value).intValue() < 0
                    || key == MAX_OCCURS && key.isDefault(this));
        }
        return true;
    }
}
