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
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.util.lang.Args;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.UjoManager;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.validator.ValidatorUtils;
import org.ujorm.wicket.OrmSessionProvider;
import org.ujorm.wicket.component.form.fields.BooleanField;
import org.ujorm.wicket.component.form.fields.ComboField;
import org.ujorm.wicket.component.form.fields.DateField;
import org.ujorm.wicket.component.form.fields.EnumField;
import org.ujorm.wicket.component.form.fields.Field;
import org.ujorm.wicket.component.form.fields.PasswordField;
import org.ujorm.wicket.component.form.fields.TextAreaField;

/**
 * Field Factory
 * @author Pavel Ponec
 */
public class FieldProvider<U extends Ujo> implements Serializable {

    /** Password key name to create a component PasswordField */
    public static final String PASSWORD_KEY_NAME = "PASSWORD";

    private RepeatingView repeatingView;
    private Map<String, Field> fields = new LinkedHashMap<String, Field>(16);
    private U domain;

    transient private OrmHandler ormHandler;

    public FieldProvider(String repeatingViewId) {
        this(new RepeatingView(repeatingViewId));
    }

    public FieldProvider(RepeatingView repeatingView) {
        this.repeatingView = repeatingView;
    }

    /** Add new field to a repeating view*/
    public void add(final Key key, final Field field) {
        Field oldField = fields.put(key.getName(), field);
        if (oldField != null) {
            throw new IllegalStateException("Field is assigned for the key: " + field);
        }
        repeatingView.add(field);
        setValidator(key, field);
    }

    /** Add new field to a repeating view*/
    public void add(Key key) {
        Field field;

        if (key.isTypeOf(Boolean.class)) {
            field = new BooleanField(key);
        } else if (key.isTypeOf(String.class)) {
            if (isPasswordKey(key)) {
                field = new PasswordField(key);
            } else {
                final int length = ValidatorUtils.getMaxLength(key.getValidator());
                field = length >= getTextAreaLimit()
                        ? new TextAreaField(key)
                        : new Field(key);
            }
        } else if (key.isTypeOf(Enum.class)) {
            field = new EnumField(key, "combo");
        } else if (key.isTypeOf(Enum.class)) {
            field = new EnumField(key, "combo");
        } else if (key.isTypeOf(java.sql.Date.class)) {
            field = new DateField(key);
        } else if (key.isTypeOf(java.util.Date.class)) {
            field = new DateField(key); // TODO DateTime field
        } else {
            field = new Field(key); // The common field
        }
        add(key, field);
    }

    /** Add a Combo-box for a persistent entity */
    public <T extends OrmUjo> void add(Key<?,T> key, Criterion<T> crn, Key<T,?> display) {
        add(key, ComboField.of(key, crn, display));
    }

    /** Get Value */
    public <T> T getValue(Key<U,T> key) {
        return (T) fields.get(key.getName()).getModelValue();
    }

    /** Set Value */
    public <T> void setValue(Key<U,T> key, T value) {
        fields.get(key.getName()).setModelValue(value);
    }

    /** Set Value and repaing component */
    public <T> void setValue(Key<U,T> key, T value, AjaxRequestTarget target) {
        setValue(key, value);
        target.add(fields.get(key.getName()));
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

    /** Set a validator of the Key to the Field from argument */
    protected void setValidator(final Key key, final Field field) {
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

    /** Copy domain attributes to fields and clone the arguments to internal parameters.
     * <br/>Persistent object will be reloaded from database.
     */
    public void setDomain(U domain) {
        OrmSessionProvider session = new OrmSessionProvider();
        try {
            this.domain = copyToFields(cloneDomain(domain, session));
        } finally {
            session.closeSession();
        }
    }

    /** Clone the domain object of reload the persistent object from database. */
    protected U cloneDomain(U domain, OrmSessionProvider session) throws NoSuchElementException, IllegalStateException {
        final U result = domain instanceof OrmUjo
             ? (U) session.getSession().loadBy((OrmUjo) domain)
             : (U) UjoManager.clone(domain, 2, "clone");
        return result != null ? result : domain;
    }

    /** Assign values to required component fields in a transaction for a lazy loading case */
    protected U copyToFields(U domain) {
        Args.notNull(domain, "domain");
        for (Field field : getFields()) {
            final Key k = field.getKey();
            field.setModelValue(k.of(domain));
        }
        return domain;
    }

    /** Copy new value to the result and return the result */
    public U getDomain() {
        for (Field field : fields.values()) {
            final Key k = field.getKey();
            final Object newValue = getValue(k);
            if (!k.equals(domain, newValue)) {
                k.setValue(domain, newValue);
            }
        }
        return domain;
    }

    /** Get original domain */
    public U getInputDomain() {
        return domain;
    }

    /** Returns a miminal text length to create a TextArea component.
     * @return The default value is 180 characters
     */
    public int getTextAreaLimit() {
        return 180;
    }

    /** Is the key type of PasswordField ?
     * The default condition is: the last key name must be 'PASSWORD'.
     */
    protected boolean isPasswordKey(Key key) {
        return key.getName().endsWith(PASSWORD_KEY_NAME)
           && ((key.length()==PASSWORD_KEY_NAME.length()
           || key.charAt(key.length() - PASSWORD_KEY_NAME.length() - 1) == '.'));
    }

    /** Refresh component */
    public void onChange(Key<U, ?> source) {
        onChange(source, "");
    }

    /** Refresh component */
    public void onChange(Key<U, ?> source, String action) {
        getField(source).onChange(action);
    }
}
