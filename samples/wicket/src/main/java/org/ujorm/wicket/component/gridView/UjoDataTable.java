/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.wicket.component.gridView;

import java.util.List;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.ujorm.orm.OrmUjo;

/**
 * It is the DefaultDataTable class modified for the AjaxNavigationToolbar.
 * @author Igor Vaynberg ( ivaynberg )
 * @author Pavel Ponec
 * @see DefaultDataTable
 */
public class UjoDataTable<T extends OrmUjo, S> extends DataTable<T, S> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     * @param id component id
     * @param columns list of columns
     * @param dataProvider data provider
     * @param rowsPerPage number of rows per page
     */
    public UjoDataTable
            ( final String id
            , final List<? extends IColumn<T, S>> columns
            , final ISortableDataProvider<T, S> dataProvider
            , final int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);

        addTopToolbar(new AjaxNavigationToolbar(this));
        addTopToolbar(new HeadersToolbar<S>(this, dataProvider));
        addBottomToolbar(new NoRecordsToolbar(this));
        setOutputMarkupId(true);
    }

    @Override
    protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
        return new OddEvenItem<T>(id, index, model);
    }
}
