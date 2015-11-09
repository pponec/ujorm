/*
 *  Copyright 2013-2015 Pavel Ponec
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.validation.IValidator;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.UjoManager;
import org.ujorm.criterion.Criterion;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.validator.ValidatorUtils;
import org.ujorm.wicket.OrmSessionProvider;
import org.ujorm.wicket.component.dialog.domestic.OfferModel;
import org.ujorm.wicket.component.form.fields.BooleanField;
import org.ujorm.wicket.component.form.fields.ComboField;
import org.ujorm.wicket.component.form.fields.DateField;
import org.ujorm.wicket.component.form.fields.EnumField;
import org.ujorm.wicket.component.form.fields.Field;
import org.ujorm.wicket.component.form.fields.GridField;
import org.ujorm.wicket.component.form.fields.PasswordField;
import org.ujorm.wicket.component.form.fields.TextAreaField;
import org.ujorm.wicket.component.form.fields.TextField;
import org.ujorm.wicket.component.form.fields.OfferField;

/**
 * Field Factory
 * @author Pavel Ponec
 */
public class FieldProvider<U extends Ujo> implements Serializable {
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(FieldProvider.class);

    /** Password key name to create a component PasswordField */
    public static final String PASSWORD_KEY_NAME = "PASSWORD";

    private RepeatingView repeatingView;
    private Map<String, Field> fields;
    private U domain;
    /** Enable method {@link #requestFocus(AjaxRequestTarget)}. The default value is {@code true} */
    private boolean focusRequestEnabled = true;
    private transient OrmHandler ormHandler;
    /** Disable new fields if the argument is {@code true} */
    private boolean disableRequest = false;

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
    public <T extends Object> Field<T> add(final Field<T> field) {
        final Key key = field.getKey();
        Field oldField = fields.put(key.getName(), field);
        if (oldField != null) {
            throw new IllegalStateException("Field is assigned for the key: " + field);
        }
        repeatingView.add(field);
        addValidator(key, field);
        field.setOutputMarkupPlaceholderTag(true);
        if (disableRequest) {
            field.setEnabled(false);
        }
        return field;
    }

    /** Generates a child component id */
    protected String newChildId() {
        return repeatingView.newChildId();
    }

    /** Add new field to a repeating view*/
    public <T extends Object> Field<T> add(Key<? super U,T> key) {
        final Field field;

        if (key.isTypeOf(Boolean.class)) {
            field = new BooleanField(newChildId(), key, null);
        } else if (key.isTypeOf(String.class)) {
            if (isPasswordKey(key)) {
                field = new PasswordField(newChildId(), key, null);
            } else {
                final int length = ValidatorUtils.getMaxLength(key.getValidator());
                field = length >= getTextAreaLimit()
                        ? new TextAreaField(newChildId(), key, null)
                        : new TextField(newChildId(), key, null);
            }
        } else if (key.isTypeOf(Enum.class)) {
            field = new EnumField(newChildId(), key, "combo");
        } else if (key.isTypeOf(java.sql.Date.class)) {
            field = new DateField(newChildId(), key, null);
        } else if (key.isTypeOf(java.util.Date.class)) {
            field = new DateField(newChildId(), key, null); // TODO DateTime field
        } else if (key.isTypeOf(Ujo.class)) {
            field = new OfferField(newChildId(), key);
        } else if (key instanceof ListKey && ((ListKey)key).isItemTypeOf(Ujo.class)) {
            field = new GridField(newChildId(), key, null);
        } else {
            field = new Field(newChildId(), key, null); // The common field
        }
        return add(field);
    }

    /** Add new field to a repeating view*/
    public <T extends Ujo & Serializable> Field<T> add(Key<? super U,T> key, Criterion<T> filter) {
        return add(key, new OfferModel<T>(filter));
    }

    /** Add new field to a repeating view*/
    public <T extends Ujo & Serializable> Field<T> add(Key<? super U,T> key, OfferModel<T> model) {
        return add(new OfferField<T>(newChildId(), key, model));
    }


    /** Add all fields of  a domain class to the form */
    public void add(Class<? super U> domainClass) {
        try {
            add(((U)domainClass.newInstance()).readKeys());
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
    public <T extends Ujo> Field<T> add(Key<? super U, T> key, Class<? extends Field> fieldClass) {
        try {
            return add(fieldClass.getConstructor(Key.class).newInstance(key));
        } catch (Exception e) {
            throw new IllegalStateException("Can't create instance of the " + fieldClass, e);
        }
    }

    /** Add a Combo-box for a <string>persistent</strong> entity */
    public <T extends OrmUjo> Field<T> add(Key<? super U,T> key, Key<T,?> display, Criterion<T> crn) {
        final Field<T> result = ComboField.of(key, crn, display);
        return add(result);
    }

    /** Get Value, or returns a default value */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Key<? super U, T> key) {
        final Field filed = fields.get(key.getName());
        return (T) filed != null
                ? (T) filed.getModelValue()
                : key.getDefault();
    }

    /** Set Value */
    @SuppressWarnings("unchecked")
    public <T> void setValue(final Key<? super U, T> key, final T value) {
        getField(key).setModelValue(value);
    }

    /** Set Value and repaint the component */
    public <T> void setValue(final Key<? super U, T> key, final T value, final AjaxRequestTarget target) {
        final Field field = getField(key);
        field.setModelValue(value);
        target.add(field);
    }

    /** Return all fields */
    public Collection<Field> getFields() {
        return fields.values();
    }

    /** Returns the related Field component */
    public final <F extends Field> F getField(final Key<? super U,?> key) {
        return (F) fields.get(key.getName());
    }

    /** Returns the last field or throw an exception
     * @return Not {@code null} always
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
    protected boolean isMandatory(Key<? super U, ?> key) {
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
    protected void addValidator(final Key<? super U, ?> key, final Field field) {
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

    /** Clone the domain object or reload the ORM object from database. */
    protected U cloneDomain(U domain, OrmSessionProvider session) throws NoSuchElementException, IllegalStateException {
        final U result = domain instanceof OrmUjo
             ? (U) session.getSession().loadBy((OrmUjo) domain)
             : (U) UjoManager.clone(domain, getClonedDepth(), "clone");
        return result != null ? result : domain;
    }

    /** Get a default cloned depth for the method {@link #cloneDomain(org.ujorm.Ujo, org.ujorm.wicket.OrmSessionProvider) }
     * @return Minimal value is 1.
     */
    protected int getClonedDepth() {
        int result = 1;
        for (Field field : getFields()) {
            final Key key = field.getKey();
            if (key instanceof CompositeKey) { // Test for the Instance
                result = Math.max(result, ((CompositeKey) key).getCompositeCount());
            }
        }
        return result;
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

    /** Copy component values to the domain result and return the result */
    public U getDomain() {
        copyToDomain(domain);
        return domain;
    }

    /** Copy component values of the enabled fields to the domain */
    public void copyToDomain(final U domain) {
        for (Field field : fields.values()) {
            if (field.isEnabled()) {
                final Key k = field.getKey();
                final Object newValue = getValue(k);
                // Note: if the key is Ujo type, then there are an endless loop
                if (k.isTypeOf(Ujo.class) || !k.equals(domain, newValue)) {
                    k.setValue(domain, newValue);
                }
            }
        }
    }

    /** Copy values according to argument from or to the domain.
     * @param domain Domain object
     * @param toFields The TRUE value means 'copy to fields' and FALSE means 'copy to domain'.
     */
    public void copyValues(final U domain, final boolean toFields) {
        if (toFields) {
            copyToFields(domain);
        } else {
            copyToDomain(domain);
        }
    }

    /** Get original domain */
    public U getInputDomain() {
        return domain;
    }

    /** Returns a minimal text length to create a TextArea component.
     * @return The default value is 180 characters
     */
    public int getTextAreaLimit() {
        return 180;
    }

    /** Is the key type of PasswordField ?
     * The default condition is: the last key name must be 'PASSWORD'.
     */
    protected boolean isPasswordKey(Key<? super U, ?> key) {
        return key.getName().endsWith(PASSWORD_KEY_NAME)
           && ((key.length()==PASSWORD_KEY_NAME.length()
           || key.charAt(key.length() - PASSWORD_KEY_NAME.length() - 1) == '.'));
    }

    /** Refresh component */
    public void onChange(Key<? super U, ?> source) {
        onChange(source, "");
    }

    /** Refresh component */
    public void onChange(Key<? super U, ?> source, String action) {
        getField(source).onChange(action);
    }

    /** Set an enabled attribute for a required filed.
     * If the field is not found, the statement is ignored */
    public <T extends Object>void setEnabled(final Key<? super U,T> key, boolean enabled) {
        final Field<T> field = getField(key);
        if (field != null) {
            field.setEnabled(enabled);
        }
    }

    /** Set a visible attribute for a required filed.
     * If the field is not found, the statement is ignored */
    public void setVisible(final Key<? super U, ?> key, boolean visible) {
        final Field field = getField(key);
        if (field != null) {
            field.setVisibilityAllowed(visible);
        }
    }

    /** Disable new fields if the argument is {@code true} */
    public boolean isDisableRequest() {
        return disableRequest;
    }

    /** Disable new fields if the argument is {@code true} */
    public void setDisablRequest(boolean disableRequest) {
        this.disableRequest = disableRequest;
    }

    /** Disable new fields (by default) */
    public final FieldProvider<U> disableFields() {
        setDisablRequest(true);
        return this;
    }

    /** Add a validator for a required filed.
     * If the field is not found, the statement is ignored */
    public <T> void addValidator(final Key<? super U, T> key, Validator<? super T> validator) {
        final Field field = getField(key);
        if (field != null) {
            field.addValidator(validator);
        }
    }

    /** Add a validator for a required filed, where the validator have <strong>no generic</strong> type.
     * If the field is not found, the statement is ignored
     * @see #setValidator(org.ujorm.Key, org.ujorm.Validator)
     */
    public <T> void addValidatorUnchecked(final Key<? super U, ? super T> key, Validator validator) {
        addValidator(key, validator);
    }

    /** Set a visible attribute for a required filed.
     * If the field is not found, the statement is ignored */
    public <T> void addValidator(final Key<? super U, T> key, IValidator<? super T> validator) {
        final Field field = getField(key);
        if (field != null) {
            field.addValidator(validator);
        }
    }

    /** Add a CSS style to the field of required key.
     * If the field is not found, the statement is ignored */
    public <T> void addCssStyle(final Key<? super U, ? super T> key, String cssStyle) {
        final Field field = getField(key);
        if (field != null) {
            field.addCssStyle(cssStyle);
        }
    }
    /** Set a focus to the first component by default */
    public void requestFocus(@Nonnull final AjaxRequestTarget target) {
        if (focusRequestEnabled) {
            final Field field = findFirstField();
            if (field != null) {
                try {
                    field.requestFocus(target);
                } catch (Throwable e) {
                    LOGGER.log(UjoLogger.WARN, "Focus", e);
                }
            }
        }
    }

    /** Find first enabled field with a non-null Key or return the {@code null}.*/
    @Nullable
    protected Field<?> findFirstField() {
        for (int i = 0, max = repeatingView.size(); i < max; i++) {
            final Component component = repeatingView.get(i);
            if (component instanceof Field ) {
                final Field<?> result = (Field<?>) component;
                if (result.isVisibilityAllowed()
                &&  result.getInput().isEnabled()
                &&  result.getKey() != null) {
                    return result;
                }
            }
        }
        return null;
    }
}