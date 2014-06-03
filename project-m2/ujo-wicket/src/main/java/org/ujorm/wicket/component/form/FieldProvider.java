/*
 *  Copyright 2013 - 2014 Pavel Ponec
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
import org.apache.wicket.validation.IValidator;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.UjoManager;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.validator.ValidatorUtils;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.OrmSessionProvider;
import org.ujorm.wicket.component.form.fields.BooleanField;
import org.ujorm.wicket.component.form.fields.ComboField;
import org.ujorm.wicket.component.form.fields.DateField;
import org.ujorm.wicket.component.form.fields.EnumField;
import org.ujorm.wicket.component.form.fields.Field;
import org.ujorm.wicket.component.form.fields.GridField;
import org.ujorm.wicket.component.form.fields.PasswordField;
import org.ujorm.wicket.component.form.fields.TextAreaField;
import org.ujorm.wicket.component.form.fields.TextField;

/**
 * Field Factory
 * @author Pavel Ponec
 */
public class FieldProvider<U extends Ujo> implements Serializable {

    /** Password key name to create a component PasswordField */
    public static final String PASSWORD_KEY_NAME = "PASSWORD";

    private RepeatingView repeatingView;
    private Map<String, Field> fields;
    private U domain;
    private transient OrmHandler ormHandler;

    /** Default constructor */
    public FieldProvider(String repeatingViewId) {
        this(new RepeatingView(repeatingViewId));
    }

    /** Default constructor with a repeatingView */
    public FieldProvider(RepeatingView repeatingView) {
        this(repeatingView, new LinkedHashMap<String, Field>(16) ) ;
    }

    /**
     * Final constructor
     * @param repeatingView a repeating views
     * @param fields Serializable field map
     */
    public FieldProvider(RepeatingView repeatingView, Map<String, Field> fields) {
        this.repeatingView = repeatingView;
        this.fields = fields;
    }

    /** Add any field to a repeating view
     * and set the OutputMarkupPlaceholderTag to value {@code true}. */
    public void add(final Field field) {
        final Key key = field.getKey();
        Field oldField = fields.put(key.getName(), field);
        if (oldField != null) {
            throw new IllegalStateException("Field is assigned for the key: " + field);
        }
        repeatingView.add(field);
        addValidator(key, field);
        field.setOutputMarkupPlaceholderTag(true);
    }

    /** Add new field to a repeating view*/
    public <T extends Object> void add(Key<U,T> key) {
        final Field field;

        if (key.isTypeOf(Boolean.class)) {
            field = new BooleanField(key);
        } else if (key.isTypeOf(String.class)) {
            if (isPasswordKey(key)) {
                field = new PasswordField(key);
            } else {
                final int length = ValidatorUtils.getMaxLength(key.getValidator());
                field = length >= getTextAreaLimit()
                        ? new TextAreaField(key)
                        : new TextField(key);
            }
        } else if (key.isTypeOf(Enum.class)) {
            field = new EnumField(key, "combo");
        } else if (key.isTypeOf(Enum.class)) {
            field = new EnumField(key, "combo");
        } else if (key.isTypeOf(java.sql.Date.class)) {
            field = new DateField(key);
        } else if (key.isTypeOf(java.util.Date.class)) {
            field = new DateField(key); // TODO DateTime field
        } else if (key instanceof ListKey && Ujo.class.isAssignableFrom( ((ListKey)key).getItemType())) {
            field = new GridField(key);
        } else {
            field = new Field(key); // The common field
        }
        add(field);
    }

    /** Add all fields of  a domain class to the form */
    public void add(Class<U> domainClass) {
        try {
            add(domainClass.newInstance().readKeys());
        } catch (Exception e) {
            throw new IllegalStateException("Can't get keys of the domain " + domainClass, e);
        }
    }

    /** Add all fields */
    public void add(final KeyList<? super U> fields) {
        for (Key key : fields) {
            add(key);
        }
    }

    /** Create a field of the required instance and set the result into container.
     * @param <T> Ujo type
     * @param key Related Key
     * @param fieldClass Class must have got a one argument constructor type of {@link Key}.
     */
    public <T extends Ujo> void add(Key<U, T> key, Class<? extends Field> fieldClass) {
        try {
            add(fieldClass.getConstructor(Key.class).newInstance(key));
        } catch (Exception ex) {
            throw new IllegalStateException("Can't create instance of the " + fieldClass, ex);
        }
    }

    /** Add a Combo-box for a <string>persistent</strong> entity */
    public <T extends OrmUjo> void add(Key<U, T> key, Key<T,?> display, Criterion<T> crn) {
        add(ComboField.of(key, crn, display));
    }

    /** Get Value, or returns a default value */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Key<U, T> key) {
        final Field filed = fields.get(key.getName());
        return (T) filed != null ? (T) filed.getModelValue() : key.getDefault();
    }

    /** Set Value */
    public <T> void setValue(Key<U, T> key, T value) {
        fields.get(key.getName()).setModelValue(value);
    }

    /** Set Value and repaint the component */
    public <T> void setValue(Key<U, T> key, T value, AjaxRequestTarget target) {
        setValue(key, value);
        target.add(fields.get(key.getName()));
    }

    /** Return all fields */
    public Collection<Field> getFields() {
        return fields.values();
    }

    /** Returns the related Field */
    public <F extends Field> F getField(Key<? super U,?> key) {
        return (F) fields.get(key.getName());
    }

    /** Returns the last field or throw an exception
     * @return Notnull always
     * @throws ArrayIndexOutOfBoundsException No last filed was found.
     */
    public <T extends Field> T getLast() throws ArrayIndexOutOfBoundsException {
        return (T) repeatingView.get(repeatingView.size() - 1);
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
    protected boolean isMandatory(Key<U, ?> key) {
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
    protected void addValidator(final Key<U, ?> key, final Field field) {
        final Validator validator = key.getValidator();
        if (validator != null) {
            field.addValidator(new UiValidator(validator, key));
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
            //  Some fields can be inactive or hidden in the GUI:
            //  } else if (k.getValidator()!=null) {
            //  k.getValidator().checkValue(newValue, k, domain);
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
    protected boolean isPasswordKey(Key<U, ?> key) {
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

    /** Set an enabled attribute for a required filed.
     * If the field is not found, the statement is ignored */
    public <T extends Object>void setEnabled(final Key<U,T> key, boolean enabled) {
        final Field<T> field = getField(key);
        if (field != null) {
            field.setEnabled(enabled);
        }
    }

    /** Set a visible attribute for a required filed.
     * If the field is not found, the statement is ignored */
    public void setVisible(final Key<U, ?> key, boolean visible) {
        final Field field = getField(key);
        if (field != null) {
            field.setVisibilityAllowed(visible);
        }
    }

    /** Add a validator for a required filed.
     * If the field is not found, the statement is ignored */
    public <T> void addValidator(final Key<U, T> key, Validator<T> validator) {
        final Field field = getField(key);
        if (field != null) {
            field.addValidator(validator);
        }
    }

    /** Add a validator for a required filed, where the validator have <strong>no generic</strong> type.
     * If the field is not found, the statement is ignored
     * @see #setValidator(org.ujorm.Key, org.ujorm.Validator)
     */
    public <T> void addValidatorUnchecked(final Key<U, T> key, Validator validator) {
        addValidator(key, validator);
    }

    /** Set a visible attribute for a required filed.
     * If the field is not found, the statement is ignored */
    public <T> void addValidator(final Key<U, T> key, IValidator<T> validator) {
        final Field field = getField(key);
        if (field != null) {
            field.addValidator(validator);
        }
    }

    /** Add a CSS style to the field of required key.
     * If the field is not found, the statement is ignored */
    public <T> void addCssStyle(final Key<U, T> key, String cssStyle) {
        final Field field = getField(key);
        if (field != null) {
            field.add(new CssAppender(cssStyle));
        }
    }
}
