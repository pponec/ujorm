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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.wicket.component.dialog.domestic.EntityDialogPane;
import org.ujorm.wicket.component.tools.LocalizedModel;

/**
 * Customer Editor
 * @author Pavel Ponec
 */
public class LoginDialog<U extends Customer> extends EntityDialogPane<U> {
    private static final long serialVersionUID = 0L;

    public LoginDialog(ModalWindow modalWindow, IModel<Customer> model) {
        super(modalWindow, model);

        // Editable fields:
        getFieldBuilder().add(Customer.LOGIN).addCssStyle("loginField");
        getFieldBuilder().add(Customer.PASSWORD).addCssStyle("passwordField");
    }

    /** Create the editor dialog */
    public static LoginDialog create(String componentId, int width, int height) {
        IModel<Customer> model = Model.of(new Customer());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final LoginDialog result = new LoginDialog(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new LocalizedModel("dialog.login.title"));
        //modalWindow.setCookieName(componentId + "-modalDialog");

        return result;
    }

}
