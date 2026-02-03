/*
 * Copyright 2013-2015, Pavel Ponec
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.gui.customer.LoginDialog;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.link.MessageLink;
import org.ujorm.wicket.component.tools.LocalizedModel;
import static org.ujorm.wicket.CommonActions.*;

/**
 * Login component
 * @author Pavel Ponec
 */
public class LoginName extends Panel {

    @SpringBean
    private AuthService authService;
    /** Login link */
    private final MessageLink link;
    /** Login dialog */
    private final LoginDialog dialog;

    public LoginName(String id) {
        super(id);
        setOutputMarkupPlaceholderTag(true);
        add(link = new MessageLink("link", new LoginModel()) {
            @Override protected void onClick(AjaxRequestTarget target) {
                LoginName.this.onClick(target);
            }
        });
        link.addBehaior(new AttributeModifier("title", "Change login"));
        add((dialog = LoginDialog.create("loginDialog", 600, 150)).getModalWindow());
    }

    /** On click event */
    private void onClick(AjaxRequestTarget target) {
        if (authService.isLogged()) {
            authService.logout();
            target.add(this);
            send(getWebPage(), Broadcast.BREADTH, new UjoEvent(LOGIN_CHANGED, null, target));
        } else {
            dialog.show(new UjoEvent(LOGIN_CHANGED, new Customer(), target), new LocalizedModel("dialog.login.title"));
        }
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        if (argEvent.getPayload() instanceof UjoEvent event) {
            if (event.isAction(LOGIN_CHANGED)) {
                event.addTarget(this);
            }
        }
    }

    /** Login model */
    private class LoginModel extends Model<String> {
        @Override public String getObject() {
            final Customer result = authService.getLoggedCustomer();
            return result != null ? result.getFullName() : "Login";
        }
    }
}
