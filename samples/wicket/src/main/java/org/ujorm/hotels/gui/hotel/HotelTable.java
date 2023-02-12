/*
 * Copyright 2013-2019, Pavel Ponec
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

import java.util.List;
import javax.inject.Named;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.gui.booking.BookingEditor;
import org.ujorm.hotels.gui.hotel.action.ActionPanel;
import org.ujorm.hotels.gui.hotel.action.Toolbar;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.CommonService;
import org.ujorm.hotels.service.param.ApplicationParams;
import org.ujorm.hotels.sources.SrcLinkPanel;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicket.component.dialog.domestic.MessageDialogPane;
import org.ujorm.wicket.component.grid.KeyColumn;
import org.ujorm.wicket.component.grid.ListDataProvider;
import org.ujorm.wicket.component.toolbar.InsertToolbar;
import org.ujorm.wicket.component.tools.LocalizedModel;
import static org.ujorm.wicket.CommonActions.*;
import static org.ujorm.wicket.component.grid.AbstractDataProvider.DEFAULT_DATATABLE_ID;
import static org.ujorm.wicket.component.grid.KeyColumn.*;

/** Hotel Table
 * @author Pavel Ponec
 */
public class HotelTable<U extends Hotel> extends GenericPanel<U> {

    @SpringBean CommonService dbService;
    @SpringBean AuthService authService;
    @Named("applParams")
    @SpringBean ApplicationParams<ApplicationParams> params;
    @SpringBean CommonService commonService;

    private final Toolbar<U> toolbar = new Toolbar("toolbar");
    private final HotelEditor editDialog;
    private final BookingEditor bookingDialog;
    private final MessageDialogPane removeDialog;
    private final ListDataProvider<U> columnBuilder;

    public HotelTable(String id) {
        super(id);

        columnBuilder = ListDataProvider.of(toolbar.getCriterion(), Hotel.NAME);
        columnBuilder.add(Hotel.NAME);
        columnBuilder.add(Hotel.CITY.add(City.NAME)); // An example of relations
        columnBuilder.add(Hotel.STREET);
        columnBuilder.add(Hotel.PRICE);
        columnBuilder.add(KeyColumn.of(Hotel.CURRENCY, SORTING_OFF));
        columnBuilder.add(Hotel.STARS);
        columnBuilder.add(Hotel.PHONE);
        columnBuilder.add(Hotel.ID, ActionPanel.class);
        columnBuilder.setSort(Hotel.NAME);
        columnBuilder.setRows(getDbRows()); // Assign required rows
        add(columnBuilder.createDataTable(DEFAULT_DATATABLE_ID, params.getRowsPerPage()));

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
        columnBuilder.setCssClass(Hotel.NAME, "hotelName");
        columnBuilder.setCssClass(Hotel.STREET, "streetName");
        add(new SrcLinkPanel("sourceLink", this));
    }

    /** Manage events */
    @Override
    public void onEvent(IEvent<?> argEvent) {
        final UjoEvent<Hotel> event = UjoEvent.get(argEvent);
        switch (event.getAction()) {
            case UPDATE:
                if (event.showDialog()) {
                    String key = event.getDomain().getId() == null
                            ? "dialog.create.title"
                            : "dialog.edit.title";
                    editDialog.show(event, new LocalizedModel(key));
                } else {
                    dbService.saveOrUpdateHotel(event.getDomain());
                    reloadTable(event, true);
                }
                break;
            case DELETE:
                if (event.showDialog()) {
                    removeDialog.setMessage(new Model("Do you want to remove selected Hotel really?"));
                    removeDialog.show(event
                            , new LocalizedModel("dialog.delete.title")
                            , "delete");
                } else {
                    dbService.deleteHotel(event.getDomain());
                    reloadTable(event, true);
                }
                break;
            case BookingEditor.BOOKING_ACTION:
                if (event.showDialog()) {
                    //bookingDialog.setEnabled(Booking.CUSTOMER.add(Customer.LOGIN), true); // TODO
                    bookingDialog.setAction(event.getAction());
                    bookingDialog.show(event.getTarget(), dbService.prepareBooking(event));
                } else {
                    final UjoEvent<Booking> bookingEvent = UjoEvent.get(argEvent);
                    dbService.saveBooking(bookingEvent.getDomain());
                    send(getPage(), Broadcast.DEPTH, new UjoEvent(LOGIN_CHANGED, null, event.getTarget()));
                }
                break;
            case Toolbar.FILTER_ACTION:
                reloadTable(event, true);
                break;
        }
    }

    /** Get user parameters include system params */
    private List<? super U> getDbRows() {
        return commonService.findHotels(toolbar.getCriterion().getObject());
    }

    /** Reload the data table */
    private void reloadTable(UjoEvent event, boolean dbRequest) {
        if (dbRequest) {
            columnBuilder.setRows(getDbRows());
        }
        event.addTarget(get(DEFAULT_DATATABLE_ID));
    }

}
