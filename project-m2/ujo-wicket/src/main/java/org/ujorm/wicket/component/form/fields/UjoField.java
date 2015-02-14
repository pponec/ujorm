/*
 *  Copyright 2015 Pavel Ponec
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
package org.ujorm.wicket.component.form.fields;

import java.io.Serializable;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.core.UjoManager;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.wicket.component.dialog.OfferDialogPanel;
import org.ujorm.wicket.component.dialog.OfferModel;

/**
 * UjoField field with a Label including a feedback message.
 * @author Pavel Ponec
 * @param <T> Ujo type
 */
public class UjoField<T extends Ujo & Serializable> extends Field<T> {
    private static final long serialVersionUID = 20150206L;

    /** Attribute to display in the input field */
    private KeyRing<T> displayKey;
    /** Offer dialog */
    private OfferDialogPanel<T> offerDialog;
    /** Offer data model */
    private OfferModel<T> model;

    /** Constructor */
    public <U extends Ujo> UjoField(String id, Key<U,T> key) {
        this(id, key, (OfferModel<T>) null);
    }

    /** Constructor */
    public <U extends Ujo> UjoField(@Nonnull String id, @Nonnull Key<U,T> key, @Nullable Key<T,?> display) {
        this(id, key, (OfferModel<T>) null);
        this.displayKey = display != null ? KeyRing.of(display) : null;
    }

    /** Constructor */
    public <U extends Ujo> UjoField(String id, Key<U,T> key, @Nullable OfferModel<T> model) {
        super(id, key, null);
        this.model = model != null ? model : new OfferModel(key.getType());

        add((offerDialog = OfferDialogPanel.create("offerDialog", this.model)).getModalWindow());
        addBehaviour(new AjaxEventBehavior("onclick") {
             protected void onEvent(AjaxRequestTarget target) {
                 showOfferDialog(target);
             }
        });
    }

    /** Show offer dialog */
    protected void showOfferDialog(AjaxRequestTarget target) {
        model.setHighliting(createHighlitingCriterion());
        offerDialog.show(target, getInput().getLabel(), new Model(""));
    }

    /** Create new Highliting Criterion */
    protected Criterion<T> createHighlitingCriterion() {
        final Key idKey = model.getId();
        final Object idValue = idKey.of(getModelValue());
        final Criterion<T> result = idKey.whereEq(idValue);
        return result;
    }

    /** Find a default name key */
    public Key<T,?> getDisplayKey() {
        final Key<T,?> result;
        if (displayKey != null) {
            result = displayKey.getFirstKey();
        } else {
            final Key<T,?> modelKey = model.getDisplay();
            result = modelKey!= null
                    ? modelKey
                    : findDefaultDisplayKey();
            displayKey = KeyRing.of(result);
        }
        return result;
    }

    /** Find a default display key by the next steps:
     * <ul>
     * <li>Find the first key with a String type where the key name contains a text {@code "NAME"}</li>
     * <li>Find the first key with a String type</li>
     * <li>Find the first key</li>
     * </ul>
     */
    protected Key<T,?> findDefaultDisplayKey() {
        final Class<T> type = (Class<T>) getKey().getType();
        final KeyList<T> keyList = UjoManager.getInstance().readKeys(type);
        for (Key k : keyList) {
            if (k.isTypeOf(String.class)
            &&  k.getName().toUpperCase(Locale.ENGLISH).indexOf("NAME") >= 0) {
                return k;
            }
        }
        for (Key k : keyList) {
            if (k.isTypeOf(String.class)) {
                return k;
            }
        }
        return keyList.getFirstKey();
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(final String componentId, final IModel<T> model) {
        final Model<String> displayModel = new Model<String>(){
            @Override public String getObject() {
                final T ujo = UjoField.this.getModelObject();
                final Object result = ujo != null ? getDisplayKey().of(ujo) : null;
                return result != null ? result.toString() : "";
            }
        };

        @SuppressWarnings("unchecked")
        final FormComponent result = new org.apache.wicket.markup.html.form.TextField(componentId, displayModel, String.class);

        result.add(new AttributeModifier("readonly", "readonly"));
        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        result.setRequired(isRequired());

        return result;
    }

    /** Returns an {@code input} value from model */
    @Override
    public T getModelValue() {
        return super.getModel().getObject();
    }

     /** Set new value for the {@code input} and reset feedback messages */
    @Override
    public void setModelValue(final T value) {
        super.getModel().setObject(value);
    }

    // ----------- FACTORIES -------------

    /** Create new ComboField using database request */
    public static <T extends OrmUjo & Serializable> Field<T> of(Key<?, T> key, Query<T> query, @Nullable Key<T, ?> display) {
        return new UjoField<T>(key.getName(), key, display);
    }

    /** Create new ComboField using database request */
    public static <T extends OrmUjo & Serializable> Field<T> of(Key<?, T> key, @Nullable Key<T, ?> display) {
        return new UjoField<T>(key.getName(), key, display);
    }

    /** Create new ComboField using database request */
    public static <T extends OrmUjo & Serializable> Field<T> of(Key<?, T> key) {
        return new UjoField<T>(key.getName(), key, (OfferModel)null);
    }

}