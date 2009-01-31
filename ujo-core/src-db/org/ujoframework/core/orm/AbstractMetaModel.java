/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.core.orm;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.map.MapUjo;

/**
 * Abstract Metamodel
 * @author pavel
 */
abstract public class AbstractMetaModel extends MapUjo {

    /** Read-only state */
    private boolean readOnly = false;

    /** Property values can be readed only */
    public boolean readOnly() {
        return readOnly;
    }

    /** Set a read-only state. */
    @SuppressWarnings("unchecked")
    public void setReadOnly(boolean recurse) {
        this.readOnly = true;
        if (recurse) for (UjoProperty p : readProperties()) {

            Object value = p.getValue(this);
            if (value instanceof AbstractMetaModel) {
                AbstractMetaModel m = (AbstractMetaModel) value;
                if (!m.readOnly()) {
                    m.setReadOnly(recurse);
                }
            }

            else if (p instanceof ListProperty
            && AbstractMetaModel.class.isAssignableFrom( ((ListProperty)p).getItemType())) {
                for (AbstractMetaModel m : ((ListProperty<AbstractMetaModel,AbstractMetaModel>)p).getList(this) ) {
                    m.setReadOnly(recurse);
                }
            }
        }
    }

    @Override
    public void writeValue(final UjoProperty property, final Object value) {
        if (readOnly) {
            throw new UnsupportedOperationException("Objec have got read-only state");
        }
        super.writeValue(property, value);
    }

    /** Change a default value only */
    protected <UJO extends Ujo, VALUE> void changeDefault
    ( final UJO ujo
    , final UjoProperty<UJO, VALUE> property
    , final VALUE value
    ) {
        if (property.isDefault(ujo)) {
            property.setValue(ujo, value);
        }
    }

}
