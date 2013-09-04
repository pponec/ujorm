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
package org.ujorm.hotels.gui;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.gui.customer.LoginDialog;
import org.ujorm.hotels.services.AuthService;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.link.MessageLink;
import org.ujorm.wicket.component.tools.LocalizedModel;
import static org.ujorm.wicket.CommonActions.*;

/**
 * Login component
 * @author Pavel Ponec
 */
public class LoginName extends MessageLink {

    @SpringBean
    private AuthService authService;
    /** Login dialog */
    private LoginDialog dialog;

    public LoginName(String id) {
        super(id, null);
        setDefaultModel(new LoginModel());
        setOutputMarkupPlaceholderTag(true);
        add(new AttributeModifier("title", "Change login"));
        add((dialog = LoginDialog.create("loginDialog", 600, 150)).getModalWindow());
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        if (authService.isCustomer()) {
            authService.logout();
            target.add(this);
            send(getWebPage(), Broadcast.EXACT, new UjoEvent(LOGIN_CHANGED, null, target));
        } else {
            dialog.show(new UjoEvent(LOGIN_CHANGED, new Customer(), target), new LocalizedModel("dialog.login.title"));
        }
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        if (argEvent.getPayload() instanceof UjoEvent) {
            final UjoEvent event = (UjoEvent) argEvent.getPayload();
            if (event.isAction(LOGIN_CHANGED)) {
                event.addTarget(this);
            }
        }
    }

    /** Login model */
    private class LoginModel extends Model<String> {
        @Override public String getObject() {
            final Customer cust = getCurrentCustomer();
            return cust != null ? cust.getLogin() : "Log-in";
        }
    }

    /** Returns logged user */
    private Customer getCurrentCustomer() {
        return authService.getCurrentCustomer();
    }

}
