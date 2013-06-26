/*
 *  Copyright 2013 Pavel Ponec
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
package org.ujorm.wicket.component.form;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.ujorm.Key;
import org.ujorm.Ujo;

/**
 * Field Factory
 * @author Pavel Ponec
 */
public class FieldFactory<U extends Ujo> implements Serializable {

    private RepeatingView repeatingView;
    private HashMap<String, Field> fields = new HashMap<String, Field>(16);

    public FieldFactory(RepeatingView repeatingView) {
        this.repeatingView = repeatingView;
    }

    /** Add new field to a repeating view*/
    public void add(Key key, Field field) {
        Field oldField = fields.put(key.getName(), field);
        if (oldField != null) {
            throw new IllegalStateException("Field is assigned for the key: " + field);
        }
        repeatingView.add(field);
    }

    /** Add new field to a repeating view*/
    public void add(Key key) {
        final Field field = new Field(key);
        add(key, field);
    }


    /** Get Value */
    public <T> T getValue(Key<U,T> key) {
        return (T) fields.get(key.getName()).getModelValue();
    }

    /** Set Value */
    public <T> void setValue(Key<U,T> key, T value) {
        fields.get(key.getName()).setModelValue(value);
    }

    /** Return all fields */
    public Collection<Field> getFields() {
        return fields.values();
    }

    /** Return all keys in a String format */
    public Set<String> getKeyNames() {
        return fields.keySet();
    }

}
