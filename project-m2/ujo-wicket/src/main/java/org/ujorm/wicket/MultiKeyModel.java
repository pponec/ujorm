/*
 *  Copyright 2013-2017 Pavel Ponec
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
package org.ujorm.wicket;

import javax.annotation.Nonnull;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;

public class MultiKeyModel<U extends Ujo> implements IModel<CharSequence> {

    private static final long serialVersionUID = 1L;
    /** Object model */
    private final IModel<U> domainModel;
    /** Object model */
    private final String separator;
    /** Serializable key. */
    private final KeyRing<U> keyRing;

    /**
     * Protected Construct with a wrapped (IModel) or unwrapped (non-IModel) object and a key expression
     * that works on the given model. To create instance use the method {@link #of(org.ujorm.Ujo, org.ujorm.Key of(...)) }
     *
     * @param domainModel The model object, which may or may not implement IModel
     * @param separator Separator
     * @param keys Instance of the Key
     * @see #of(org.ujorm.Ujo, org.ujorm.Key)
     */
    @SuppressWarnings("unchecked")
    protected MultiKeyModel(final IModel<U> domainModel, final String separator, final Key<U, ?> ... keys) {
        this.domainModel = domainModel;
        this.separator = separator;
        this.keyRing = KeyRing.of(keys);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getObject().toString();
    }
    
    /** Return Key */
    @SuppressWarnings("unchecked")
    public final Key<U, ?> getKeyRing() {
        return (Key<U, ?>) keyRing;
    }

    @Override
    public CharSequence getObject() {
        final StringBuilder result = new StringBuilder();
        final U domain = domainModel.getObject();
        
        for (int i = 0, max = keyRing.size(); i < max; ++i) {
            if (i > 0) {
                result.append(separator);
            }
            result.append(format(keyRing.getValue(domain, i)));            
        }
        
        return result;
    }
    
    /** Format the result value */
    @Nonnull
    protected String format(@Nonnull final Object value) {
        return value != null
             ? value.toString()
             : "";
    }

    @Override
    public void setObject(CharSequence object) {
        throw new UnsupportedOperationException(String.valueOf(object));
    }

    /** Return a class of the base domainObject */
    public Class<? super U> getBaseClass() {
        return keyRing.getType();
    }

    @Override
    public void detach() {
    }

    // ------ STATIC ------

    /**
     * Type-infering factory method
     * @param keys key array
     * @return {@link PropertyModel} instance
     */
    public static <UJO extends Ujo, T> MultiKeyModel<UJO> of(@Nonnull final IModel<UJO> domainModel, Key<UJO, ?> ... keys) {
        return new MultiKeyModel<UJO>(domainModel, " ", keys);
    }

    /**
     * Type-infering factory method
     * @param keys key array
     * @return {@link PropertyModel} instance
     */
    public static <UJO extends Ujo, T> MultiKeyModel<UJO> of(@Nonnull final IModel<UJO> domainModel, @Nonnull final String separator, Key<UJO, ?> ... keys) {
        return new MultiKeyModel<UJO>(domainModel, separator, keys);
    }

}
