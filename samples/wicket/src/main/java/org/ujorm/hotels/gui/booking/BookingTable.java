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
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.gui.booking.action.BookActionPanel;
import org.ujorm.hotels.service.param.ApplicationParams;
import org.ujorm.hotels.sources.SrcLinkPanel;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.domestic.MessageDialogPane;
import org.ujorm.wicket.component.grid.OrmDataProvider;
import org.ujorm.wicket.component.tools.LocalizedModel;
import static org.ujorm.wicket.CommonActions.*;
import static org.ujorm.wicket.component.grid.AbstractDataProvider.DEFAULT_DATATABLE_ID;
import org.ujorm.hotels.service.CommonService;

/**
 * BookingTable
 * @author Pavel Ponec
 */
public class BookingTable<U extends Booking> extends GenericPanel<U> {

    @SpringBean
    private ApplicationParams params;

    @SpringBean
    private CommonService dbService;

    private MessageDialogPane removeDialog;

    public BookingTable(String id) {
        super(id);

        final OrmDataProvider<U> columns = OrmDataProvider.of(getCriterionModel());

        columns.add(Booking.DATE_FROM);
        columns.add(Booking.HOTEL.add(Hotel.NAME));
        columns.add(Booking.HOTEL.add(Hotel.CITY).add(City.NAME));
        columns.add(Booking.PERSONS);
        columns.add(Booking.NIGHTS);
        columns.add(Booking.PRICE);
        columns.add(Booking.CURRENCY);
        columns.add(Booking.CUSTOMER.add(Customer.LOGIN));
        columns.add(Booking.CREATION_DATE);
        columns.add(Booking.ID, BookActionPanel.class);
        columns.setSort(Booking.DATE_FROM);
        add(columns.createDataTable(params.getRowsPerPage()));

        add((removeDialog = MessageDialogPane.create("removeDialog", 290, 160)).getModalWindow());
        add(new SrcLinkPanel("sourceLink", this));
    }

    /** Create a new criterion model from the {@code dbService} */
    private Model<Criterion<U>> getCriterionModel() {
        return new Model<Criterion<U>>(){
            @Override public Criterion<U> getObject() {
                return dbService.getBookingPreview().cast();
            }
        };
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<Booking> event = UjoEvent.get(argEvent);
        switch (event.getAction()) {
            case DELETE:
                if (event.showDialog()) {
                    removeDialog.setMessage(new Model("Do you want to remove selected Booking really?"));
                    removeDialog.show(event
                            , new LocalizedModel("dialog.delete.title")
                            , "delete");
                } else {
                    dbService.deleteBooking(event.getDomain());
                    reloadTable(event);
                }
                break;
        }
     }

    /** Reload the data table */
    private void reloadTable(UjoEvent event) {
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

}
