package org.ujorm.hotels.gui;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ujorm.hotels.gui.about.AboutPanel;
import org.ujorm.hotels.gui.booking.BookingPanel;
import org.ujorm.wicket.component.tabs.UjoAjaxTabbedPanel;
import org.ujorm.wicket.component.tabs.UjoTab;
import org.ujorm.hotels.gui.customer.CustomerPanel;
import org.ujorm.hotels.gui.hotel.HotelPanel;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
        super(parameters);

        // create a list of ITab objects used to feed the tabbed panel
        List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new UjoTab("Hotels", "hotel", HotelPanel.class));
        tabs.add(new UjoTab("Booking", "booking", BookingPanel.class));
        tabs.add(new UjoTab("Customer", "customer", CustomerPanel.class));
        tabs.add(new UjoTab("About", "about", AboutPanel.class));
        add(new UjoAjaxTabbedPanel("tabs", tabs));

        // TODO Add your page's components here:

        // Footer:
       add(new Label("version", getApplication().getFrameworkSettings().getVersion()));
    }


}
