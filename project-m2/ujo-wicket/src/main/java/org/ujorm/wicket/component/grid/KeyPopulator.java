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
package org.ujorm.wicket.component.grid;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.wicket.KeyModel;

/**
 * A convenience implementation of {@link ICellPopulator} that adds a label that will display the
 * value of the specified key. Non-string properties will be converted to a string before
 * display.
 * <p>
 * Example:
 * <pre class="pre">
 * List&lt;ICellPopulator&gt; columns = KeyPopulator.list
 *         ( Employee.ID
 *         , Employee.FIRSTNAME
 *         , Employee.LASTNAME
 *         , Employee.EMAIL
 *         );
 *
 * <span class="keyword-directive">final</span> WebMarkupContainer table = <span class="keyword-directive">new</span> WebMarkupContainer(<span class="character">&quot;</span><span class="character">table</span><span class="character">&quot;</span>);
 * <span class="keyword-directive">final</span> DataGridView grid = <span class="keyword-directive">new</span> DataGridView(<span class="character">&quot;</span><span class="character">gridPanel</span><span class="character">&quot;</span>, columns, <span class="keyword-directive">new</span> InnerPeopleProvicer());
 * table.setOutputMarkupId(<span class="keyword-directive">true</span>);
 * grid.setItemsPerPage(20);
 * add(table);
 * table.add(grid);
 * add(<span class="keyword-directive">new</span> AjaxPagingNavigator(<span class="character">&quot;</span><span class="character">tableNavigator</span><span class="character">&quot;</span>, grid));
 * </pre>
 *
 * @param <UJO>
 * @author Igor Vaynberg (ivaynberg)
 * @author Pavel Ponec
 */
public class KeyPopulator<UJO extends Ujo,T> implements ICellPopulator<UJO>, IColumn<UJO,Key<UJO,T>> {

    private static final long serialVersionUID = 1L;
    private final KeyRing<UJO> key;

    public KeyPopulator(KeyRing<UJO> key) {
        this.key = key;
    }

    public KeyPopulator(Key<UJO, ?> key) {
        this(KeyRing.of(key));
    }

    /**
     * @see org.apache.wicket.model.IDetachable#detach()
     */
    @Override
    public void detach() {
    }

    /**
     * @see org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator#populateItem(org.apache.wicket.markup.repeater.Item,
     *      java.lang.String, org.apache.wicket.model.IModel)
     */
    @Override
    public void populateItem(final Item<ICellPopulator<UJO>> cellItem, final String componentId, final IModel<UJO> rowModel) {
        cellItem.add(new Label(componentId, KeyModel.of(rowModel.getObject(), (Key<UJO, ?>) key.getFirstKey())));
    }

    @Override
    public Component getHeader(String componentId) {
        String id = key.getFirstKey().getName();
        return new Label(id, id);
    }

    @Override
    public Key<UJO,T> getSortProperty() {
        return key.getFirstKey();
    }

    @Override
    public boolean isSortable() {
        return true;
    }

    /**
     * Create new ICellPopulator List
     * @see #list(java.lang.Class, org.ujorm.Key<? super UJO,?>[])
     */
    @Deprecated
    public static <UJO extends Ujo> List<ICellPopulator<UJO>> safeList(Key<UJO, ?>... properties) {
        final List<ICellPopulator<UJO>> result = new ArrayList<ICellPopulator<UJO>>(properties.length);
        for (Key p : properties) {
            result.add(new KeyPopulator(p));
        }
        return result;
    }

   /**
     * Create new ICellPopulator List with a pupulator with no generic item type for easy use
     */
    public static <UJO extends Ujo> List<ICellPopulator> list(Key<? super UJO, ?>... properties) {
        return (List<ICellPopulator>) safeList((Key[]) properties);
    }

}
