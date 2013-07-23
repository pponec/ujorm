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
import org.ujorm.hotels.services.AuthService;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.link.MessageLink;
import static org.ujorm.wicket.CommonActions.*;

/**
 * Login component
 * @author Pavel Ponec
 */
public class Login extends MessageLink {

    @SpringBean
    private AuthService authService;

    public Login(String id) {
        super(id, null);
        setDefaultModel(new LoginModel());
        setOutputMarkupPlaceholderTag(true);
        add(new AttributeModifier("title", "Logout"));
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        authService.logout(getSession());
        setResponsePage(HomePage.class);
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

    /** Component is visible if the Current customer is not null */
    @Override
    public boolean isVisible() {
        return super.isVisible()
            && getCurrentCustomer() != null;
    }

    /** Returns logged user */
    public Customer getCurrentCustomer() {
        return authService.getCurrentCustomer(getSession());
    }

    /** Login model */
    private class LoginModel extends Model<String> {
        @Override public String getObject() {
            final Customer cust = Login.this.getCurrentCustomer();
            return cust != null ? cust.get(Customer.LOGIN) : "";
        }
    }

}
