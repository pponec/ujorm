/*
 *  Copyright 2009-2010 Pavel Ponec
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
package org.ujorm.implementation.orm;

import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.extensions.Property;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;

/** The special property pro LazyLoadiing */
public class OrmProperty<U extends OrmUjo, VALUE> extends Property<U, VALUE> {

    // /** Is the property type of Relation  */
    // private boolean relation;

    public OrmProperty(int index) {
        super(index);
    }

    public OrmProperty(int index, String name, VALUE defaultValue, Validator<VALUE> validator) {
        super(index, validator);
        init(name, null, null, defaultValue, index, false);
    }

    /** Lock the Property */
    @Override
    protected void lock() {
        super.lock();
        // relation = isTypeOf(OrmUjo.class); // TODO ...
    }

    /**
     * An alias for the method of(Ujo).
     * @see #of(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public VALUE of(final U ujo) {
        final Session mySession = ujo.readSession();  // maybe readSession() is better?
        Object result = ujo.readValue(this);

        if (isTypeOf(OrmUjo.class)) {
            if (result instanceof ForeignKey) {
                if (mySession == null) {
                    throw new IllegalStateException("The Session was not assigned.");
                }
                result = mySession.loadInternal(this, ((ForeignKey) result).getValue(), true);
                ujo.writeValue(this, result);
            } else if (result != null
                && mySession != null
                && mySession != ((OrmUjo) result).readSession()
            ) {
                // Write the current session to a related object:
                ((OrmUjo) result).writeSession(mySession);
            }
        }
        return result != null
                ? (VALUE) result
                : getDefault();
    }
}