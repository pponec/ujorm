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

package org.ujoframework.orm;

import java.util.Collections;
import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.ListUjoProperty;
import org.ujoframework.UjoAction;
import org.ujoframework.implementation.quick.QuickUjo;


/**
 * Abstract Metamodel
 * @author Pavel Ponec
 */
abstract public class AbstractMetaModel extends QuickUjo {

    /** Read-only state */
    private boolean readOnly = false;

    /** Property values can be readed only */
    public boolean readOnly() {
        return readOnly;
    }

    /** Set a read-only state. */
    @SuppressWarnings("unchecked")
    public void setReadOnly(boolean recurse) {
        if (readOnly) return;

        for (UjoProperty p : readProperties()) {

            if (p instanceof ListUjoProperty) {
               final List list = (List) p.of(this);
               p.setValue(this, list!=null
                   ? Collections.unmodifiableList(list)
                   : Collections.EMPTY_LIST
               );
            }
        }

        this.readOnly = true; // <<<<<< LOCK THE OBJECT !!!
        
        if (recurse) for (UjoProperty p : readProperties()) {

            Object value = p.getValue(this);
            if (value instanceof AbstractMetaModel) {
                ((AbstractMetaModel) value).setReadOnly(recurse);
            }

            else if (p instanceof ListUjoProperty) {
               if ( AbstractMetaModel.class.isAssignableFrom( ((ListUjoProperty)p).getItemType())) {
                    for (AbstractMetaModel m : ((ListUjoProperty<AbstractMetaModel,AbstractMetaModel>)p).getList(this) ) {
                        m.setReadOnly(recurse);
                    }
               }
            }
        }
    }

    /** Test a read-only state */
    public boolean checkReadOnly(final boolean exception) throws UnsupportedOperationException {
        if (readOnly && exception) {
            throw new UnsupportedOperationException("Object have got a read-only state");
        }
        return readOnly;
    }

    @Override
    public void writeValue(final UjoProperty property, final Object value) {
        this.checkReadOnly(true);
        super.writeValue(property, value);
    }

    /** Assign a 'valid value' over a default UJO property value only */
    protected <UJO extends Ujo, VALUE> void changeDefault
    ( final UJO ujo
    , final UjoProperty<UJO, VALUE> property
    , final VALUE value
    ) {
        if (property.isDefault(ujo) && isUsable(value)) {
            property.setValue(ujo, value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
        if (action.getType()==UjoAction.ACTION_XML_EXPORT) {
            return !property.isDefault(this);
        }
        return super.readAuthorization(action, property, value);
    }

    /** Returns true, if the argument text is not null and not empty. */
    protected boolean isUsable(final CharSequence text) {
        final boolean result = text!=null && text.length()>0;
        return result;
    }

    /** Returns true, if the argument text is not null and not empty. */
    protected boolean isUsable(final Object value) {
        final boolean result = value instanceof CharSequence
            ? isUsable((CharSequence)value)
            : value!=null
            ;
        return result;
    }

    /** Getter based on one UjoProperty */
    @SuppressWarnings("unchecked")
    public <UJO extends AbstractMetaModel, VALUE> VALUE get ( UjoProperty<UJO, VALUE> property) {
        return property.of((UJO) this);
    }

}
