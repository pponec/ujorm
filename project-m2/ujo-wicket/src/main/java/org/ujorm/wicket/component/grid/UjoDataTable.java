/*
 *  Copyright 2014-2022 Pavel Ponec
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

import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.repeater.data.IDataProvider;

/**
 * Extended DataTable for a cleaner generated HTML code.
 * There was removed some excess HTML tags inside tag {@code TD}.
 * @author Pavel Ponec
 */
public class UjoDataTable<T, S> extends DataTable<T, S> {

    /**
     * Constructor
     *
     * @param id component id
     * @param columns list of IColumn objects
     * @param dataProvider imodel for data provider
     * @param rowsPerPage number of rows per page
     */
    public UjoDataTable
            ( final String id
            , final List columns
            , final IDataProvider dataProvider
            , final long rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
    }
}
