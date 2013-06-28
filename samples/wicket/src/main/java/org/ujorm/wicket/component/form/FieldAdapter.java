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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.validator.ValidatorUtils;

/**
 * Field Factory
 * @author Pavel Ponec
 */
public class FieldAdapter<U extends Ujo> implements Serializable {

    private RepeatingView repeatingView;
    private Map<String, Field> fields = new LinkedHashMap<String, Field>(16);
    private Ujo domain;

    transient private OrmHandler ormHandler;

    public FieldAdapter(String repeatingViewId) {
        this(new RepeatingView(repeatingViewId));
    }

    public FieldAdapter(RepeatingView repeatingView) {
        this.repeatingView = repeatingView;
    }

    /** Add new field to a repeating view*/
    public void add(final Key key, final Field field) {
        Field oldField = fields.put(key.getName(), field);
        if (oldField != null) {
            throw new IllegalStateException("Field is assigned for the key: " + field);
        }
        repeatingView.add(field);
        assignValidator(key, field);
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

    /** Return field */
    public Field getField(Key key) {
        return fields.get(key.getName());
    }

    /** Return all keys in a String format */
    protected Set<String> getKeyNames() {
        return fields.keySet();
    }

    /** Returns the repeating view */
    public RepeatingView getRepeatingView() {
        return repeatingView;
    }

    /** Returns OrmHandler */
    protected OrmHandler getOrmHandler() {
        if (ormHandler != null) {
            Application appl = repeatingView.getPage().getApplication();
            if (appl instanceof OrmHandlerProvider) {
                ormHandler = ((OrmHandlerProvider) appl).getOrmHandler();
            }
        }

        return ormHandler;
    }

    /** Is the key mandatory ? */
    protected boolean isMandatory(Key key) {
        final OrmHandler handler = getOrmHandler();
        if (handler != null) {
            MetaColumn column = handler.findColumnModel(key, false);
            if (column.isMandatory()) {
                return true;
            }
        }
        return ValidatorUtils.isMandatoryValidator(key.getValidator());
    }

    /** Assign the validator */
    protected void assignValidator(final Key key, final Field field) {
        final Validator validator = key.getValidator();
        if (validator != null) {
            field.setValidator(new UjoValidator(validator, key));
        } else if (isMandatory(key)) {
            Component input = field.getInput();
            if (input instanceof FormComponent) {
                ((FormComponent)input).setRequired(true);
            }
        }
    }

    /** Save domain value and assign value into components */
    public void setDomain(Ujo domain) {
        for (String keyName : getKeyNames()) {
            Key k = domain.readKeys().find(keyName);
            setValue(k, k.of(domain));
        }
        this.domain = domain;
    }

    /** Copy new value to the result and return the result */
    public Ujo getDomain() {
        for (String keyName : getKeyNames()) {
            final Key k = domain.readKeys().find(keyName);
            final Object newValue = getValue(k);
            if (!k.equals(domain, newValue)) {
                k.setValue(domain, newValue);
            }
        }
        return domain;
    }

}
