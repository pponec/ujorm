/*
 *  Copyright 2015-2016 Pavel Ponec
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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.ujorm.wicket.component.dialog.domestic.OfferDialogPane;
import org.ujorm.wicket.component.dialog.domestic.OfferModel;
import org.ujorm.wicket.component.form.Closeable;

/**
 * UjoField field with a Label including a feedback message.
 * @author Pavel Ponec
 * @param <U> The Ujo type
 */
public class OfferField<U extends Ujo & Serializable> extends Field<U> implements Closeable<U> {
    private static final long serialVersionUID = 20150206L;

    /** Attribute to display in the input field */
    private KeyRing<U> displayKey;
    /** Offer dialog */
    private OfferDialogPane<U> offerDialog;
    /** Offer data model */
    private OfferModel<U> model;

    /** Constructor */
    public <W extends Ujo> OfferField(String id, Key<W,U> key) {
        this(id, key, (OfferModel<U>) null);
    }

    /** Constructor */
    public <W extends Ujo> OfferField(@Nonnull String id, @Nonnull Key<W,U> key, @Nullable Key<U,?> display) {
        this(id, key, (OfferModel<U>) null);
        this.displayKey = display != null ? KeyRing.<U>of(display) : null;
    }

    /** Constructor */
    public <W extends Ujo> OfferField(String id, Key<W,U> key, @Nullable OfferModel<U> model) {
        super(id, key, null);
        this.model = model != null ? model : new OfferModel(key.getType());
        this.model.setClosable(this);

        add((offerDialog = createDialog("offerDialog", this.model)).getModalWindow());
        addBehaviour(new AjaxEventBehavior("click") {
             protected void onEvent(AjaxRequestTarget target) {
                 showOfferDialog(target);
             }
        });
    }

    /** Show offer dialog */
    protected void showOfferDialog(AjaxRequestTarget target) {
        if (getInput().isEnableAllowed()) {
            model.setHighliting(createHighlitingCriterion());
            offerDialog.show(target, getInput().getLabel(), new Model(""));
        }
    }

    /** Create new Highliting Criterion */
    @Nullable
    protected <V> Criterion<U> createHighlitingCriterion() {
        final Key<U,V> idKey = model.getId();
        final U ujo = getModelValue();
        final Criterion<U> result = ujo != null ? idKey.whereEq(idKey.of(ujo)) : null;
        return result;
    }

    /** Check the ORM type of the main domain object */
    protected boolean isOrm() {
        return super.getKey().isTypeOf(OrmUjo.class);
    }

    /** Find a default name key */
    public Key<U,?> getDisplayKey() {
        final Key<U,?> result;
        if (displayKey != null) {
            result = displayKey.getFirstKey();
        } else {
            final Key<U,?> modelKey = model.getDisplay();
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
    protected Key<U,?> findDefaultDisplayKey() {
        final Class<U> type = (Class<U>) getKey().getType();
        final KeyList<U> keyList = UjoManager.getInstance().readKeys(type);
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
    protected FormComponent createInput(final String componentId, final IModel<U> model) {
        final Model<String> displayModel = new Model<String>(){
            @Override public String getObject() {
                final U ujo = OfferField.this.getModelObject();
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
    public U getModelValue() {
        return getModel().getObject();
    }

    /** Create the editor dialog */
    public OfferDialogPane createDialog(final String componentId, final OfferModel model) {
        final ModalWindow modalWindow = new ModalWindow(componentId, Model.of(""));
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final OfferDialogPane result = new OfferDialogPane(modalWindow, model);
        modalWindow.setInitialWidth(model.getDimension().width);
        modalWindow.setInitialHeight(model.getDimension().height);
        modalWindow.setTitle(model.getTitle());
        // modalWindow.setCookieName(modalWindow.getPath() + "-modalDialog");

        return result;
    }

    /** Close dialog set new model and set a focus to the current input */
    @Override
    public void closeDialog(AjaxRequestTarget target, U row) {
        this.offerDialog.close(target);
        if (row != null) {
            setModelValue(row);
            target.add(this);
            target.focusComponent(getInput());
        }
    }

    // ----------- FACTORIES -------------

    /** Create new ComboField using database request */
    public static <T extends OrmUjo & Serializable> Field<T> of(Key<?, T> key, Query<T> query, @Nullable Key<T, ?> display) {
        return new OfferField<T>(key.getName(), key, display);
    }

    /** Create new ComboField using database request */
    public static <U extends OrmUjo & Serializable> Field<U> of(Key<?, U> key, @Nullable Key<U, ?> display) {
        return new OfferField<U>(key.getName(), key, display);
    }

    /** Create new ComboField using database request */
    public static <U extends OrmUjo & Serializable> Field<U> of(Key<?, U> key) {
        return new OfferField<U>(key.getName(), key, (OfferModel)null);
    }

}