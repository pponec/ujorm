/*
 *  Copyright 2013-2022 Pavel Ponec
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

import java.util.List;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.wicket.OrmSessionProvider;
import org.ujorm.wicket.component.form.FieldEvent;

/**
 * CheckBox field with a Label including a feedback message.
 * @author Pavel Ponec
 */
public class ComboField<T extends Ujo> extends Field<T> {

    private static final long serialVersionUID = 20130621L;
    private final KeyRing<T> keys;
    private final List<T> items;

    public <U extends Ujo> ComboField(Key<U,T> key, List<T> items, Key<T,?> selectId, Key<T,?> display) {
        super(key.getName(), key, null);
        this.keys = KeyRing.of(selectId, display);
        this.items = items;
    }

    public <U extends Ujo> ComboField(String componentId, Key<U,T> key, List<T> items, Key<T, ?> selectId, Key<T,?> display, String cssClass) {
        super(componentId, key, cssClass);
        this.keys = KeyRing.of(selectId, display);
        this.items = items;
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(final String componentId, final IModel<T> model) {
        DropDownChoice<T> result = new DropDownChoice<T>(componentId, model, getItems(), new ChoiceRenderer<T>() {
            @Override
            public Object getDisplayValue(T object) {
                return getComboDisplayValue(object);
            }

            @Override
            public String getIdValue(T object, int index) {
                return getComboIdValue(object, index);
            }
        });
        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        result.setRequired(isRequired());
        result.setNullValid(!isRequired());
        return result;
    }

    /**
     * Get the value for displaying to an end user.
     * @param object the actual object
     * @return the value meant for displaying to an end user
     */
    protected Object getComboDisplayValue(T object) {
        return getKeyDisplay().of(object);
    }

    /**
     * This method is called to get the id value of an object (used as the value attribute of a
     * choice element) The id can be extracted from the object like a primary key,
     * or if the list is stable you could just return a toString of the index.
     * <p>
     * Note that the given index can be {@code -1} if the object in question is not contained in the
     * available choices.
     *
     * @param object The object for which the id should be generated
     * @param index The index of the object in the choices list.
     * @return String
     */
    protected String getComboIdValue(T object, int index) {
        return String.valueOf(getKeyId().of(object));
    }

    /** Get component items */
    public List<T> getItems() {
        return items;
    }

    /** Get the identifier key */
    public final Key<T, ?> getKeyId() {
        return keys.get(0);
    }

    /** Get a key to display */
    public final Key<T, ?> getKeyDisplay() {
        return keys.get(1);
    }

    /** Create an Updating Behavior with "keyup" event */
    @Override
    public void onChange(final String action) {
        addBehaviour(createChangeBehaviour(action, "change"));
    }

    /** Create new AjaxFormComponentUpdatingBehavior with delay 300 ms. */
    @Override
    protected AjaxEventBehavior createChangeBehaviour(final String action, final String jsEvent) {
        return new AjaxFormComponentUpdatingBehavior(jsEvent) {
            @Override protected void onUpdate(AjaxRequestTarget target) {
                send(ComboField.this, Broadcast.BUBBLE, new FieldEvent(action, key, target));
            }
        };
    }

    // ----------- FACTORIES -------------

    /** Create new ComboField using database request */
    public static <T extends OrmUjo> ComboField<T> of(Key<?, T> key, Criterion<T> items, Key<T, ?> display) {
        final OrmSessionProvider session = new OrmSessionProvider();
        try {
            final Query<T> query = session.getSession().createQuery(items);
            if (display == null) {
                display = query.getColumns().get(0).getKey();
            }
            query.setColumns(true, display);
            query.orderBy(display);
            return of(key, query, display);
        } finally {
            session.closeSession();
        }
    }

    /** Create new ComboField using database request */
    public static <T extends OrmUjo> ComboField<T> of(Key<?, T> key, Query<T> query, Key<T, ?> display) {
        final Key<T,?> idKey = query.getTableModel().getFirstPK().getKey();
        return new ComboField<T>(key, query.list(), idKey, display);
    }

}