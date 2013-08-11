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

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ujorm.hotels.gui.about.AboutPanel;
import org.ujorm.hotels.gui.booking.BookingTable;
import org.ujorm.hotels.gui.customer.CustomerTable;
import org.ujorm.hotels.gui.hotel.HotelTable;
import org.ujorm.orm.OrmHandler;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.tabs.UjoTab;
import org.ujorm.wicket.component.tabs.UjoTabbedPanel;
import static org.ujorm.wicket.CommonActions.*;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;
    /** Logout */
    public static final String LOGOUT_ID = "logout";

    public HomePage(PageParameters parameters) {
        super(parameters);

        // create a list of ITab objects used to feed the tabbed panel
        List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new UjoTab("Hotels", "hotel", HotelTable.class));
        tabs.add(new UjoTab("Booking", "booking", BookingTable.class));
        tabs.add(new UjoTab("Customer", "customer", CustomerTable.class));
        tabs.add(new UjoTab("About", "about", AboutPanel.class));
        add(new UjoTabbedPanel("tabs", tabs));

        // Add your page's components here:
        WicketApplication appl = (WicketApplication) getApplication();
        OrmHandler handler = appl.getOrmHandler();

        // Login name and logout action:
        add(new LoginName("login"));

        // Footer:
        add(new AjaxLink("aboutLink") {
            @Override public void onClick(AjaxRequestTarget target) {
                ((UjoTabbedPanel)HomePage.this.get("tabs")).selectedTab(AboutPanel.class, target);
            }
        });
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        UjoEvent<?> event = UjoEvent.get(argEvent);
        if (event != null && event.isAction(LOGIN_CHANGED)) {
            event.addTarget(HomePage.this.get("tabs"));
        }
    }
}
