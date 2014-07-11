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
package org.ujorm.wicketForms.gui.booking;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.criterion.Criterion;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.domestic.MessageDialogPane;
import org.ujorm.wicket.component.tools.LocalizedModel;
import org.ujorm.wicketForms.entity.Booking;
import org.ujorm.wicketForms.services.DbService;
import static org.ujorm.wicket.CommonActions.*;
import static org.ujorm.wicket.component.grid.AbstractDataProvider.DEFAULT_DATATABLE_ID;

/**
 * BookingTable
 * @author Pavel Ponec
 */
public class BookingTable<U extends Booking> extends GenericPanel<U> {

    @SpringBean DbService dbService;
    private MessageDialogPane removeDialog;

    public BookingTable(String id) {
        super(id);

//        UjoDataProvider<Booking> columns = UjoDataProvider.of(getCriterionModel());
//        columns.add(Booking.DATE_FROM);
//        columns.add(Booking.CUSTOMER.add(Customer.LOGIN));
//        columns.add(Booking.HOTEL.add(Hotel.NAME));
//        columns.add(Booking.HOTEL.add(Hotel.CITY).add(City.NAME));
//        columns.add(Booking.PERSONS);
//        columns.add(Booking.NIGHTS);
//        columns.add(Booking.PRICE);
//        columns.add(Booking.CURRENCY);
//        columns.add(Booking.CREATION_DATE);
//        columns.add(newActionColumn());
//        columns.setSort(Booking.DATE_FROM);
//        add(columns.createDataTable(10));

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

    /** Reload the data table */
    private void reloadTable(UjoEvent event) {
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

}
