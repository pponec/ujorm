/*
 * Copyright 2013-2014, Pavel Ponec
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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.gui.about.AboutPanel;
import org.ujorm.hotels.gui.about.DeployInfo;
import org.ujorm.hotels.gui.about.MeasuringCode;
import org.ujorm.hotels.gui.booking.BookingTable;
import org.ujorm.hotels.gui.customer.CustomerTable;
import org.ujorm.hotels.gui.hotel.HotelTable;
import org.ujorm.hotels.gui.params.ParamsTable;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.param.ApplicationParams;
import org.ujorm.validator.ValidationException;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.tabs.UjoTab;
import org.ujorm.wicket.component.tabs.UjoTabbedPanel;
import org.ujorm.wicket.component.waiting.WaitingIcon;
import static org.ujorm.wicket.CommonActions.*;

public class HomePage extends WebPage implements IAjaxIndicatorAware {
    private static final long serialVersionUID = 1L;
    /** Logout */
    public static final String LOGOUT_ID = "logout";
    @SpringBean
    private AuthService authService;
    @SpringBean
    private ApplicationParams applParams;
    /** Waiting animated icon */
    private final Component waitingIcon;

    public HomePage(PageParameters parameters) {
        super(parameters);

        // create a list of ITab objects used to feed the tabbed panel
        List<UjoTab> tabs = new ArrayList<UjoTab>();
        tabs.add(new UjoTab("Hotels", "hotel", HotelTable.class));
        tabs.add(new UjoTab("Booking", "booking", BookingTable.class));
        tabs.add(new UjoTab("Parameters", "params", ParamsTable.class));
        tabs.add(new UjoTab("Customer", "customer", CustomerTable.class));
        tabs.add(new UjoTab("About", "about", AboutPanel.class));
        add(new UjoTabbedPanel("tabs", tabs));

        // Login name and logout action:
        add(new LoginName("login"));

        // Footer:
        add(new AjaxLink("aboutLink") {
            @Override public void onClick(AjaxRequestTarget target) {
                showAboutTab(target);
            }
        });
        Label label;
        add(waitingIcon = new WaitingIcon("waitingIcon"));
        add(new MeasuringCode("measuringCode"));
        add(new DeployInfo("deployInfo"));
        add(new Label("applicationTitle", MainApplication.APPLICATION_NAME));
        add(label = new Label("applicationHeader", MainApplication.APPLICATION_NAME));
        if (!applParams.isProduction()) {
            label.add(new CssAppender("test"));
        }

        if (parameters.get("about").toString() != null) {
            showAboutTab(null);
        }
    }

    /** Show the about tab */
    private void showAboutTab(@Nonnull AjaxRequestTarget target) {
        ((UjoTabbedPanel)HomePage.this.get("tabs")).selectTab(AboutPanel.class, target);
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        UjoEvent<Customer> event = UjoEvent.get(argEvent);
        if (event != null && event.isAction(LOGIN_CHANGED)) {
            if (event.getDomain()!=null) {
                if (!authService.authenticate(event.getDomain())) {
                    throw new ValidationException("login.failed", "Login failed");
                }
            }
            event.addTarget(HomePage.this.get("tabs"));
            event.addTarget(HomePage.this.get("login"));
        }
    }

    /** Set a MSIE compatibility mode to a response header */
    @Override
    protected void setHeaders(WebResponse response) {
        super.setHeaders(response);
        response.setHeader("X-UA-Compatible", "IE=edge");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    }

    /** {@inheritDoc} */
    @Override
    public String getAjaxIndicatorMarkupId() {
        return waitingIcon.getMarkupId();
    }
}
