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
package org.ujorm.hotels.gui.hotel;

import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.core.KeyRing;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.gui.booking.BookingEditor;
import org.ujorm.hotels.gui.hotel.action.ActionPanel;
import org.ujorm.hotels.gui.hotel.action.Toolbar;
import org.ujorm.hotels.services.AuthService;
import org.ujorm.hotels.services.DbService;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.domestic.MessageDialogPane;
import org.ujorm.wicket.component.grid.KeyColumn;
import org.ujorm.wicket.component.grid.UjoDataProvider;
import org.ujorm.wicket.component.toolbar.InsertToolbar;
import org.ujorm.wicket.component.tools.LocalizedModel;
import static org.ujorm.wicket.CommonActions.*;
import static org.ujorm.wicket.component.grid.KeyColumn.*;
import static org.ujorm.wicket.component.grid.UjoDataProvider.*;

/**
 * Hotel Table
 * @author Pavel Ponec
 */
public class HotelTable extends Panel {

    @SpringBean DbService dbService;
    @SpringBean AuthService authService;

    private Toolbar toolbar = new Toolbar("toolbar");
    private HotelEditor editDialog;
    private BookingEditor bookingDialog;
    private MessageDialogPane removeDialog;

    public HotelTable(String id) {
        super(id);

        UjoDataProvider<Hotel> columns = UjoDataProvider.of(toolbar.getCriterion());
        columns.add(Hotel.NAME);
        columns.add(Hotel.CITY.add(City.NAME)); // An example of relations
        columns.add(Hotel.STREET);
        columns.add(Hotel.PRICE);
        columns.add(KeyColumn.of(Hotel.CURRENCY, SORTING_OFF));
        columns.add(Hotel.STARS);
        columns.add(Hotel.PHONE);
        columns.add(newActionColumn());
        columns.setSort(Hotel.NAME);
        add(columns.createDataTable(DEFAULT_DATATABLE_ID, 10));

        add(toolbar);
        add((editDialog = HotelEditor.create("editDialog", 700, 410)).getModalWindow());
        add((bookingDialog = BookingEditor.create("bookingDialog", 700, 390)).getModalWindow());
        add((removeDialog = MessageDialogPane.create("removeDialog", 290, 160)).getModalWindow());

        DataTable table = ((DataTable) get(DEFAULT_DATATABLE_ID));
        table.addBottomToolbar(new InsertToolbar(table, Hotel.class) {
            @Override public boolean isVisible() {
                return authService.isAdmin();
            }
        } );
        columns.setCssClass(Hotel.NAME, "hotelName");
        columns.setCssClass(Hotel.STREET, "streetName");
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<Hotel> event = UjoEvent.get(argEvent);
        if (event != null) {
            if (event.isAction(UPDATE)) {
                if (event.showDialog()) {
                    String key = event.getDomain().getId() == null
                            ? "dialog.create.title"
                            : "dialog.edit.title";
                    editDialog.show(event, new LocalizedModel(key));
                } else {
                    dbService.saveOrUpdateHotel(event.getDomain());
                    reloadTable(event);
                }
            }
            else if (event.isAction(DELETE)) {
                if (event.showDialog()) {
                    removeDialog.setMessage(new Model("Do you want to remove selected Hotel really?"));
                    removeDialog.show(event
                            , new LocalizedModel("dialog.delete.title")
                            , "delete");
                } else {
                    dbService.deleteHotel(event.getDomain());
                    reloadTable(event);
                }
            }
            else if (event.isAction(BookingEditor.BOOKING_ACTION)) {
                if (event.showDialog()) {
                    //bookingDialog.setEnabled(Booking.CUSTOMER.add(Customer.LOGIN), true); // TODO
                    bookingDialog.setAction(event.getAction());
                    bookingDialog.show(event.getTarget(), dbService.prepareBooking(event));
                } else {
                    final UjoEvent<Booking> bookingEvent = UjoEvent.get(argEvent);
                    dbService.saveBooking(bookingEvent.getDomain());
                    send(getPage(), Broadcast.DEPTH, new UjoEvent(LOGIN_CHANGED, null, event.getTarget()));
                }
            }
            else if (event.isAction(Toolbar.FILTER_ACTION)) {
                reloadTable(event);
            }
        }
    }

    /** Action offer: */
    private AbstractColumn<Hotel, KeyRing<Hotel>> newActionColumn() {
        return new KeyColumn<Hotel, Integer>(KeyRing.of(Hotel.ID), null) {
            @Override
            public void populateItem(Item item, String componentId, IModel model) {
                final Hotel hotel = (Hotel) model.getObject();
                final ActionPanel panel = new ActionPanel(componentId, hotel);
                item.add(panel);
            }
        };
    }

    /** Reload the data table */
    private void reloadTable(UjoEvent event) {
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

}
