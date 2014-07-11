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
package org.ujorm.wicketForms.gui.customer;

import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.criterion.Criterion;
import org.ujorm.validator.ValidationException;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.domestic.MessageDialogPane;
import org.ujorm.wicket.component.form.FieldProvider;
import org.ujorm.wicket.component.tools.LocalizedModel;
import org.ujorm.wicketForms.entity.Customer;
import org.ujorm.wicketForms.gui.hotel.action.Toolbar;
import org.ujorm.wicketForms.services.AuthService;
import org.ujorm.wicketForms.services.DbService;
import static org.ujorm.wicket.CommonActions.*;
import static org.ujorm.wicket.component.grid.UjoDataProvider.*;

/**
 * Customer Panel
 * @author Pavel Ponec
 */
public class CustomerTable<U extends Customer> extends GenericPanel<U> {

    @SpringBean private DbService dbService;
    @SpringBean private AuthService authService;

    private CustomerEditor editDialog;
    private MessageDialogPane removeDialog;
    private LoginDialog loginDialog;

    public CustomerTable(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        final Form form = new Form("form");
        add(form);

        createCustomForm("fields", form);

        // Dialogs:
        add((editDialog = CustomerEditor.create("editDialog", 700, 390)).getModalWindow());
        add((removeDialog = MessageDialogPane.create("removeDialog", 290, 160)).getModalWindow());
        add((loginDialog = LoginDialog.create("loginDialog", 600, 150)).getModalWindow());

        DataTable table = ((DataTable) get(DEFAULT_DATATABLE_ID));
     //   table.addBottomToolbar(new InsertCustomer(table));
    }

    /** Create a criterion for the table */
    private IModel<Criterion<? super U>> getCriterion() {
        return new Model(authService.isAdmin()
             ? Customer.ACTIVE.forAll()
             : Customer.ACTIVE.whereEq(true));
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<Customer> event = UjoEvent.get(argEvent);
        if (event != null) {
            if (event.isAction(UPDATE)) {
                if (event.showDialog()) {
                    String key = event.getDomain().getId() == null
                            ? "dialog.create.title"
                            : "dialog.edit.title";
                    editDialog.show(event, new LocalizedModel("dialog.edit.title"));
                } else {
                    dbService.saveOrUpdateCustomer(event.getDomain());
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

    /** Reload the data table */
    private void reloadTable(UjoEvent event) {
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

    /** Create a customer edit form */
    private void createCustomForm(final String id, final Form form) {
        FieldProvider<Customer> fields = new FieldProvider(id);

        fields.add(Customer.LOGIN);
        fields.add(Customer.PASSWORD);
        fields.add(Customer.TITLE);
        fields.add(Customer.FIRSTNAME);
        fields.add(Customer.SURNAME);
        fields.add(Customer.EMAIL);
        fields.add(Customer.ADMIN);
        fields.add(Customer.ACTIVE);
        form.add(fields.getRepeatingView());

        fields.setDomain(new Customer());
    }


}
