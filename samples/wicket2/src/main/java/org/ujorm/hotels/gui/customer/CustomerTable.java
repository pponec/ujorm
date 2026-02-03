/*
 * Copyright 2013-2019, Pavel Ponec
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
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.gui.customer.action.Toolbar;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.CommonService;
import org.ujorm.hotels.service.param.ApplicationParams;
import org.ujorm.hotels.sources.SrcLinkPanel;
import org.ujorm.validator.ValidationException;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.domestic.MessageDialogPane;
import org.ujorm.wicket.component.grid.CommonAction;
import org.ujorm.wicket.component.grid.OrmDataProvider;
import org.ujorm.wicket.component.grid.OrmDataProviderCached;
import org.ujorm.wicket.component.tools.LocalizedModel;

import jakarta.inject.Named;

import static org.ujorm.wicket.CommonActions.*;
import static org.ujorm.wicket.component.grid.AbstractDataProvider.DEFAULT_DATATABLE_ID;

/** Customer Table
 * @author Pavel Ponec
 */
public class CustomerTable<U extends Customer> extends GenericPanel<U> {

    @SpringBean private CommonService dbService;
    @SpringBean private AuthService authService;
    @Named("applParams")
    @SpringBean ApplicationParams<ApplicationParams> params;

    private final CustomerEditor editDialog;
    private final MessageDialogPane removeDialog;
    private final LoginDialog loginDialog;
    private final Toolbar<U> toolbar = new Toolbar("toolbar");

    public CustomerTable(String id) {
        super(id);

        final OrmDataProvider<U> columnBuider = params.isTableCacheEnabled()
                ? OrmDataProviderCached.of(toolbar.getCriterion())
                : OrmDataProvider.of(toolbar.getCriterion());

        columnBuider.add(Customer.LOGIN);
        columnBuider.add(Customer.TITLE);
        columnBuider.add(Customer.FIRSTNAME);
        columnBuider.add(Customer.SURNAME);
        columnBuider.add(Customer.EMAIL);
        columnBuider.add(Customer.ADMIN);
        columnBuider.add(Customer.ACTIVE);
        columnBuider.add(Customer.ID, createActions());
        columnBuider.setSort(Customer.LOGIN);
        add(columnBuider.createDataTable(10, true));

        // Dialogs:
        add(toolbar);
        add((editDialog = CustomerEditor.create("editDialog", 700, 390)).getModalWindow());
        add((removeDialog = MessageDialogPane.create("removeDialog", 290, 160)).getModalWindow());
        add((loginDialog = LoginDialog.create("loginDialog", 600, 150)).getModalWindow());
        add(new SrcLinkPanel("sourceLink", this));
    }

    /** Create actions */
    private CommonAction[] createActions() {
        final CommonAction[] result =
        { getActionDelete(DELETE)
        , getActionsUpdate(UPDATE)
        , getActionsUpdate(LOGIN)
        };
        return result;
    }

    /** Action DELETE */
    private CommonAction getActionDelete(String action) {
        return new CommonAction<U>(action) {
            @Override public boolean isVisible(U row) {
                return !authService.isLogged(row) && authService.isAdmin();
            }
        };
    }

    /** Action DELETE */
    private CommonAction getActionsUpdate(final String action) {
        return new CommonAction<U>(action) {
            @Override public boolean isVisible(U row) {
                boolean updatable = authService.isAdmin() || authService.isLogged(row);
                return UPDATE.equals(action) == updatable;
            }
        };
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<Customer> event = UjoEvent.get(argEvent);
        switch (event.getAction()) {
            case UPDATE:
                if (event.showDialog()) {
                    String key = event.getDomain().getId() == null
                            ? "dialog.create.title"
                            : "dialog.edit.title";
                    editDialog.show(event, new LocalizedModel(key));
                } else {
                    dbService.saveOrUpdateCustomer(event.getDomain());
                    reloadTable(event);
                }
                break;
            case DELETE:
                if (event.showDialog()) {
                    removeDialog.setMessage(new Model("Do you want to remove selected Customer really?"));
                    removeDialog.show(event, new LocalizedModel("dialog.delete.title"), "delete");
                } else {
                    dbService.deleteCustomer(event.getDomain());
                    reloadTable(event);
                }
                break;
            case LOGIN:
                if (event.showDialog()) {
                    loginDialog.show(event, new LocalizedModel("dialog.login.title"));
                } else if (event.getDomain() != null) {
                    if (!authService.authenticate(event.getDomain())) {
                        throw new ValidationException("login.failed", "Login failed");
                    }
                    send(getPage(), Broadcast.DEPTH, new UjoEvent(LOGIN_CHANGED, null, event.getTarget()));
                }
                argEvent.stop();
                break;
            case org.ujorm.hotels.gui.hotel.action.Toolbar.FILTER_ACTION:
                reloadTable(event);
                break;
        }
    }

    /** Reload the data table */
    private void reloadTable(UjoEvent event) {
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

}
