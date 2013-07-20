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

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.ujorm.core.KeyRing;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.gui.customer.action.CustActionPanel;
import org.ujorm.wicket.component.grid.KeyColumn;
import org.ujorm.wicket.component.grid.UjoDataProvider;

/**
 * Customer Panel
 * @author Pavel Ponec
 */
public class CustomerPanel extends Panel {

    public CustomerPanel(String id) {
        super(id);

        UjoDataProvider<Customer> columns
                = UjoDataProvider.of(Customer.ACTIVE.whereEq(true));
        columns.addColumn(Customer.LOGIN);
        columns.addColumn(Customer.TITLE);
        columns.addColumn(Customer.FIRSTNAME);
        columns.addColumn(Customer.SURENAME);
        columns.addColumn(Customer.EMAIL);
        columns.addColumn(Customer.ADMIN);
        columns.addColumn(Customer.ACTIVE);
        columns.setSort(Customer.LOGIN);
        columns.addColumn(newActionColumn());
        add(columns.createDataTable(10));
    }

    /** Nabídka akcí: */
    private AbstractColumn<Customer, KeyRing<Customer>> newActionColumn() {
        return new KeyColumn<Customer, Integer>(KeyRing.of(Customer.ID), null, null) {
            @Override
            public void populateItem(Item item, String componentId, IModel model) {
                final Customer customer = (Customer) model.getObject();
                final CustActionPanel panel = new CustActionPanel(componentId, customer);
                item.add(panel);
            }
        };
    }

}
