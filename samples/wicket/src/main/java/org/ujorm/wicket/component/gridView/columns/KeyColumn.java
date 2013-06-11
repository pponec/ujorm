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
package org.ujorm.wicket.component.gridView.columns;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.wicket.KeyModel;


/**
 * A convenience implementation of column that adds a Ujo Property to the cell whose model is determined by
 * the provided wicket property expression (same as used by {@link PropertyModel}) that is evaluated
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
 * The above will attach a label to the cell with a property model for the expression
 * &quot;name.first&quot;
 *
 * @see KeyModel
 *
 * @author Igor Vaynberg ( ivaynberg )
 * @author Pavel Ponec
 * @param <UJO extends Ujo>
 *            The Model object type
 */
public class KeyColumn<UJO extends Ujo, T> extends AbstractColumn<UJO, Key<UJO,T>> {

    private static final long serialVersionUID = 1L;
    protected final KeyRing<UJO> keySerializable;
    protected final String cssClass;

    /**
     * Creates a property column that is also sortable - the simplyfied methdod
     *
     * @param displayModel display model
     * @param sortProperty sort property
     * @param key wicket property expression used by PropertyModel
     * @param cssClass optional argument for a CSS class
     */
    public KeyColumn(final Key<UJO,T> key, String cssClass) {
        super(new Model(key.getName()));
        this.keySerializable = KeyRing.of(key);
        this.cssClass = cssClass;
    }

    /**
     * Implementation of populateItem which adds a label to the cell whose model is the provided
     * property expression evaluated against rowModelObject
     *
     * @see ICellPopulator#populateItem(Item, String, IModel)
     */
    @Override
    public void populateItem(final Item<ICellPopulator<UJO>> item, final String componentId, final IModel<UJO> rowModel) {
        final UJO ujo = rowModel.getObject();
        final IModel<?> valueModel = createValueModel(ujo);
        final Component value = createValueCoponent(componentId, valueModel);
        appendCssClass(value, ujo);
        item.add(value);
    }

    /** {@inheritDoc } */
    @Override
    public String getCssClass() {
        return cssClass;
    }

    /** Create the Label for a Value component */
    private Component createValueCoponent(final String componentId, final IModel<?> valueModel) {
        return new Label(componentId, valueModel);
    }

    /**
     * Factory method for generating a model that will generated the displayed value. Typically the
     * model is a property model using the {@link #keySerializable} specified in the constructor.
     *
     * @param rowModel
     * @return model
     */
    protected IModel<?> createValueModel(final UJO ujo) {
        final IModel result = KeyModel.of(ujo, keySerializable.getFirstKey());
        return result;
    }

    /**
     * @return wicket property expression
     */
    public String getPropertyExpression() {
        return keySerializable.toString();
    }

    /** Get the Key */
    public Key<UJO,?> getKey() {
        return keySerializable.getFirstKey();
    }

    /** A factory method */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key) {
        return new KeyColumn(key, null);
    }

    /** A factory method */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, String cssClass) {
        return new KeyColumn(key, cssClass);
    }

    /** Append CSS class */
    protected void appendCssClass(final Component value, final UJO ujo) {
        final String cssClass$ = getCssClass();
        if (cssClass$ != null) {
            value.add(new AttributeAppender("class", new Model(cssClass$), " "));
        }
    }

}
