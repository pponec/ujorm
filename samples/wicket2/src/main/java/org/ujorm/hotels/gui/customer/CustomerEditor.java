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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.wicket.component.dialog.domestic.EntityDialogPane;
import org.ujorm.wicket.component.form.FieldProvider;
import org.ujorm.wicket.component.tools.LocalizedModel;

/** Customer Editor
 * @author Pavel Ponec
 */
public class CustomerEditor<U extends Customer> extends EntityDialogPane<U> {
    private static final long serialVersionUID = 0L;

    @SpringBean private AuthService authService;
    final private FieldProvider<U> fieldBuilder;

    public CustomerEditor(ModalWindow modalWindow, IModel<U> model) {
        super(modalWindow, model);

        // Editable fields:
        fieldBuilder = getFieldBuilder();
        fieldBuilder.add(Customer.LOGIN);
        fieldBuilder.add(Customer.PASSWORD);
        fieldBuilder.add(Customer.TITLE);
        fieldBuilder.add(Customer.FIRSTNAME);
        fieldBuilder.add(Customer.SURNAME);
        fieldBuilder.add(Customer.EMAIL);
        fieldBuilder.add(Customer.ADMIN);
        fieldBuilder.add(Customer.ACTIVE);
    }

    /** Modify attribute(s): */
    @Override
    protected void onBeforeRender() {
        final boolean newMode = isNew();
        fieldBuilder.setEnabled(Customer.LOGIN, newMode);
        fieldBuilder.setVisible(Customer.ACTIVE, !newMode);
        fieldBuilder.setVisible(Customer.ADMIN, authService.isAdmin());
        super.onBeforeRender();
    }

    /** Dialog for a new Customer */
    private boolean isNew() {
        final Customer customer = (Customer) getDefaultModelObject();
        return customer.get(Customer.ID) == null;
    }

    /** Create the editor dialog */
    public static CustomerEditor create(String componentId, int width, int height) {
        IModel<Customer> model = Model.of(new Customer());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final CustomerEditor<Customer> result = new CustomerEditor<Customer>(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new LocalizedModel("dialog.edit.title"));
        //modalWindow.setCookieName(componentId + "-modalDialog");

        return result;
    }
}
