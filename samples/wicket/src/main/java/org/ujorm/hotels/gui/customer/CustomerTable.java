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

import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.core.KeyRing;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.gui.customer.action.CustActionPanel;
import org.ujorm.hotels.gui.hotel.action.Toolbar;
import org.ujorm.hotels.services.AuthService;
import org.ujorm.hotels.services.DbService;
import org.ujorm.validator.ValidationException;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.domestic.MessageDialogPane;
import org.ujorm.wicket.component.grid.KeyColumn;
import org.ujorm.wicket.component.grid.UjoDataProvider;
import org.ujorm.wicket.component.tools.LocalizedModel;
import static org.ujorm.wicket.CommonActions.*;
import static org.ujorm.wicket.component.grid.UjoDataProvider.*;

/**
 * Customer Panel
 * @author Pavel Ponec
 */
public class CustomerTable extends Panel {

    @SpringBean private DbService dbService;
    @SpringBean private AuthService authService;

    private CustomerEditor editDialog;
    private MessageDialogPane removeDialog;
    private LoginDialog loginDialog;

    public CustomerTable(String id) {
        super(id);

        UjoDataProvider<Customer> columns
                = UjoDataProvider.of(Customer.ACTIVE.whereEq(true));
        columns.add(Customer.LOGIN);
        columns.add(Customer.TITLE);
        columns.add(Customer.FIRSTNAME);
        columns.add(Customer.SURENAME);
        columns.add(Customer.EMAIL);
        columns.add(Customer.ADMIN);
        columns.add(Customer.ACTIVE);
        columns.add(createActionColumn());
        columns.setSort(Customer.LOGIN);
        add(columns.createDataTable(10));

        // Dialogs:
        add((editDialog = CustomerEditor.create("editDialog", 700, 390)).getModalWindow());
        add((removeDialog = MessageDialogPane.create("removeDialog", 290, 160)).getModalWindow());
        add((loginDialog = LoginDialog.create("loginDialog", 600, 150)).getModalWindow());
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<Customer> event = UjoEvent.get(argEvent);
        if (event != null) {
            if (event.isAction(UPDATE)) {
                if (event.showDialog()) {
                    editDialog.show(event, new LocalizedModel("dialog.edit.title"));
                } else {
                    dbService.updateCustomer(event.getDomain());
                    reloadTable(event);
                }
            }
            else if (event.isAction(DELETE)) {
                if (event.showDialog()) {
                    removeDialog.setMessage(new Model("Do you want to remove selected Customer really?"));
                    removeDialog.show(event
                            , new LocalizedModel("dialog.delete.title")
                            , "delete");
                } else {
                    dbService.deleteCustomer(event.getDomain());
                    reloadTable(event);
                }
            }
            else if (event.isAction(Toolbar.FILTER_ACTION)) {
                reloadTable(event);
            }
            else if (event.isAction(LOGIN)) {
                if (event.showDialog()) {
                    loginDialog.show(event, new LocalizedModel("dialog.login.title"));
                } else if (event.getDomain() != null) {
                    if (!authService.authenticate(event.getDomain())) {
                        throw new ValidationException("login.failed", "Login failed");
                    }
                    send(getPage(), Broadcast.DEPTH, new UjoEvent(LOGIN_CHANGED, null, event.getTarget()));
                }
                argEvent.stop();
            }
        }
    }

    /** Create action column */
    private AbstractColumn<Customer, KeyRing<Customer>> createActionColumn() {
        return new KeyColumn<Customer, Integer>(KeyRing.of(Customer.ID), null, null) {
            @Override
            public void populateItem(Item item, String componentId, IModel model) {
                final Customer customer = (Customer) model.getObject();
                final CustActionPanel panel = new CustActionPanel(componentId, customer);
                item.add(panel);
            }
        };
    }

    /** Reload the data table */
    private void reloadTable(UjoEvent event) {
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

}
