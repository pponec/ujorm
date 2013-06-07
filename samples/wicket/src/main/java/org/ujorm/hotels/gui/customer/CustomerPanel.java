/*
 * Copyright 2013, Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.hotels.gui.customer;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.ujorm.hotels.domains.Customer;
import org.ujorm.wicket.component.gridView.KeyColumn;

/**
 *
 * @author Pavel Ponec
 */
public class CustomerPanel extends Panel {

    public CustomerPanel(String id) {
        super(id);

        final List<IColumn> columns = new ArrayList<IColumn>();
        columns.add(KeyColumn.of(Customer.LOGIN));
        columns.add(KeyColumn.of(Customer.TITLE));
        columns.add(KeyColumn.of(Customer.FIRSTNAME));
        columns.add(KeyColumn.of(Customer.SURENAME));
        columns.add(KeyColumn.of(Customer.EMAIL));
        columns.add(KeyColumn.of(Customer.ADMIN));

        add(new DefaultDataTable("datatable", columns, new CustomerProvider(), 20));
    }

}
