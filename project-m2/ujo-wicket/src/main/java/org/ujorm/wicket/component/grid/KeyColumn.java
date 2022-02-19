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
package org.ujorm.wicket.component.grid;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.wicket.KeyModel;
import org.ujorm.wicket.component.tools.LocalizedModel;

/**
 * A convenience implementation of column that adds a Ujo Property to the cell whose model is determined by
 * the provided wicket key expression (same as used by {@link PropertyModel}) that is evaluated
 * against the current row's model object
 * <p>
 * Example:
 * <pre class="pre">
 * <span class="keyword-directive">public</span> <span class="keyword-directive">class</span> DataTablePage2 <span class="keyword-directive">extends</span> WebPage {
 *
 *     <span class="keyword-directive">public</span> DataTablePage2() {
 *         <span class="keyword-directive">final</span> EmployeeProvider userProvider = <span class="keyword-directive">new</span> EmployeeProvider();
 *
 *         <span class="keyword-directive">final</span> List&lt;IColumn&gt; columns = <span class="keyword-directive">new</span> ArrayList&lt;IColumn&gt;();
 *         columns.add(<span class="keyword-directive">new</span> KeyColumn(Employee.ID));
 *         columns.add(<span class="keyword-directive">new</span> KeyColumn(Employee.FIRSTNAME));
 *         columns.add(<span class="keyword-directive">new</span> KeyColumn(Employee.LASTNAME));
 *         columns.add(<span class="keyword-directive">new</span> KeyColumn(Employee.EMAIL));
 *         columns.add(<span class="keyword-directive">new</span> KeyColumn(Employee.STUDENT));
 *
 *         add(<span class="keyword-directive">new</span> DefaultDataTable(<span class="character">&quot;</span><span class="character">datatable</span><span class="character">&quot;</span>, columns, userProvider, 20));
 *     }
 * }
 * </pre>
 *
 * The above will attach a label to the cell with a key model for the expression
 * &quot;name.first&quot;
 *
 * @see KeyModel
 * @author Pavel Ponec
 * @param <U extends Ujo> The Model object type
 */
public class KeyColumn<U extends Ujo, T> extends AbstractColumn<U, KeyRing<U>> {
    private static final long serialVersionUID = 1L;

    /** Enable a grid column sorting */
    public static final boolean SORTING_ON = true;
    /** Disable a grid column sorting */
    public static final boolean SORTING_OFF = false;
    /** Localization key prefix */
    public static final String PROPERTY_PREFIX = "column.";

    /** Data key */
    protected final KeyRing<U> keySerializable;
    /** The CSS class of the column */
    protected String cssClass;

    /**
     * Creates a sortable key column
     * @param key Ujorm key to display in the column
     * @param sortKey Optional persistent ujorm key to sorting
     */
    public KeyColumn(final KeyRing<U> key, final KeyRing<U> sortKey) {
        this(new ResourceModel(PROPERTY_PREFIX
                + LocalizedModel.getSimpleKeyName(key.getFirstKey())
                , key.getFirstKey().getName())
                , key, sortKey);
    }

    /**
     * Creates a sortable key column
     * @param label Column label
     * @param key Ujorm key to display in the column
     * @param sortKey Optional persistent ujorm key to sorting
     */
    public KeyColumn
            ( final IModel<String> label
            , final KeyRing<U> key
            , final KeyRing<U> sortKey) {
        super(label, sortKey);
        this.keySerializable = key;
    }

    /**
     * Implementation of populateItem which adds a label to the cell whose model is the provided
     * key expression evaluated against rowModelObject
     *
     * @see ICellPopulator#populateItem(Item, String, IModel)
     */
    @Override
    public void populateItem(final Item<ICellPopulator<U>> item, final String componentId, final IModel<U> rowModel) {
        final U ujo = rowModel.getObject();
        final IModel<?> valueModel = createValueModel(ujo);
        final Component value = createValueCoponent(componentId, valueModel, ujo);
        appendCssClass(item, ujo);
        item.add(value);
    }

    /** {@inheritDoc } */
    @Override
    public String getCssClass() {
        return cssClass;
    }

    /** Assign a CSS class */
    public void setCssClass(final String cssClass) {
        this.cssClass = cssClass;
    }

    /** Create the Label for a Value component, but no CSS class.
     * @see #appendCssClass(org.apache.wicket.Component, org.ujorm.Ujo)
     */
    protected Component createValueCoponent(final String componentId, final IModel<?> valueModel, final U ujo) {
        return new Label(componentId, valueModel);
    }

    /**
     * Factory method for generating a model that will generated the displayed value. Typically the
     * model is a key model using the {@link #keySerializable} specified in the constructor.
     *
     * @param rowModel
     * @return model
     */
    protected IModel<?> createValueModel(final U ujo) {
        final IModel<?> result = KeyModel.of(ujo, keySerializable.getFirstKey());
        return result;
    }

    /**
     * @return wicket key expression
     */
    public String getPropertyExpression() {
        return keySerializable.toString();
    }

    /** Get the Key */
    public Key<U,T> getKey() {
        return (Key<U,T>) keySerializable.getFirstKey();
    }

    /** Append a CSS class - to overwriting only */
    protected void appendCssClass(final Component value, final U ujo) {
    }

    /** Domain class + key */
    @Override
    public String toString() {
        return keySerializable.getFirstKey().getFullName();
    }

    // =============== STATIC METHODS ===============

    /** A factory method */
    public static <U extends Ujo, T> KeyColumn<U, T> of(Key<U, T> key, boolean sorted) {
        return of(key, sorted, null);
    }

    /** A factory method */
    public static <U extends Ujo, T> KeyColumn<U, T> of(Key<U, T> key, boolean sorted, String cssClass) {
        final KeyRing serializableKey = KeyRing.of(key);
        final KeyColumn<U, T> result = new KeyColumn
                ( serializableKey
                , sorted ? serializableKey : null);
        result.setCssClass(cssClass);
        return result;
    }

    /** A factory method */
    public static <U extends Ujo, T> KeyColumn<U, T> of(Key<U, T> key, Key<U, T> sort, String cssClass) {
        final KeyColumn<U, T> result = new KeyColumn
            ( KeyRing.of(key)
            , KeyRing.of(sort));
        result.setCssClass(cssClass);
        return result;
    }

}