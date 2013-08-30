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
package org.ujorm.hotels.gui.booking;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.gui.booking.action.BookActionPanel;
import org.ujorm.hotels.services.DbService;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.domestic.MessageDialogPane;
import org.ujorm.wicket.component.grid.KeyColumn;
import org.ujorm.wicket.component.grid.UjoDataProvider;
import org.ujorm.wicket.component.tools.LocalizedModel;
import static org.ujorm.wicket.CommonActions.*;
import static org.ujorm.wicket.component.grid.UjoDataProvider.*;

/**
 * BookingTable
 * @author Pavel Ponec
 */
public class BookingTable extends Panel {

    @SpringBean DbService dbService;
    private MessageDialogPane removeDialog;

    public BookingTable(String id) {
        super(id);

        UjoDataProvider<Booking> columns = UjoDataProvider.of(getCriterionModel());
        columns.add(Booking.DATE_FROM);
        columns.add(Booking.CUSTOMER.add(Customer.LOGIN));
        columns.add(Booking.HOTEL.add(Hotel.NAME));
        columns.add(Booking.HOTEL.add(Hotel.CITY).add(City.NAME));
        columns.add(Booking.PERSONS);
        columns.add(Booking.NIGHTS);
        columns.add(Booking.PRICE);
        columns.add(Booking.CURRENCY);
        columns.add(Booking.CREATION_DATE);
        columns.add(newActionColumn());
        columns.setSort(Booking.DATE_FROM);
        add(columns.createDataTable(10));

        add((removeDialog = MessageDialogPane.create("removeDialog", 290, 160)).getModalWindow());
    }

    /** Create a new criterion model from the {@code dbService} */
    private Model<Criterion<Booking>> getCriterionModel() {
        return new Model<Criterion<Booking>>(){
            @Override public Criterion<Booking> getObject() {
                return dbService.getBookingPreview();
            }
        };
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<Booking> event = UjoEvent.get(argEvent);
        if (event != null) {
            if (event.isAction(DELETE)) {
                if (event.showDialog()) {
                    removeDialog.setMessage(new Model("Do you want to remove selected Booking really?"));
                    removeDialog.show(event
                            , new LocalizedModel("dialog.delete.title")
                            , "delete");
                } else {
                    dbService.deleteBooking(event.getDomain());
                    reloadTable(event);
                }
            }
        }
    }

    /** Offer action: */
    private AbstractColumn<Booking, KeyRing<Booking>> newActionColumn() {
        return new KeyColumn<Booking, Integer>(KeyRing.of(Booking.ID), null) {
            @Override
            public void populateItem(Item item, String componentId, IModel model) {
                final Booking hotel = (Booking) model.getObject();
                final BookActionPanel panel = new BookActionPanel(componentId, hotel);
                item.add(panel);
            }
        };
    }

    /** Reload the data table */
    private void reloadTable(UjoEvent event) {
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

}
