package org.ujorm.hotels.gui;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.ujorm.hotels.gui.booking.BookingPanel;
import org.ujorm.hotels.gui.customer.CustomerPanel;
import org.ujorm.hotels.gui.hotel.HotelPanel;

public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
        super(parameters);

        // create a list of ITab objects used to feed the tabbed panel
        List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(createHotelTab("Hotels"));
        tabs.add(createBookingTab("Booking"));
        tabs.add(createCustomerTab("Customer"));
        add(new AjaxTabbedPanel("tabs", tabs));

        // TODO Add your page's components here:

        // Footer:
       add(new Label("version", getApplication().getFrameworkSettings().getVersion()));
    }

    /** Booking page */
    private AbstractTab createBookingTab(String name) {
        return new AbstractTab(new Model<String>(name)) {
          @Override public Panel getPanel(String panelId) {
              return new BookingPanel(panelId);
          }
      };
    }

    /** Customer page */
    private AbstractTab createCustomerTab(String name) {
        return new AbstractTab(new Model<String>(name)) {
          @Override public Panel getPanel(String panelId) {
              return new CustomerPanel(panelId);
          }
      };
    }

    /** Hotel tab */
    private AbstractTab createHotelTab(String name) {
        return new AbstractTab(new Model<String>(name)) {
          @Override public Panel getPanel(String panelId) {
              return new HotelPanel(panelId);
          }
      };
    }

}
