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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.services.AuthService;
import org.ujorm.wicket.component.dialog.domestic.EntityDialogPane;
import org.ujorm.wicket.component.tools.LocalizedModel;

/**
 * Booking Editor
 * @author Pavel Ponec
 */
public class BookingEditor extends EntityDialogPane<Booking> {
    private static final long serialVersionUID = 0L;

    /** Default value is the same like the field */
    public static final String BOOKING_ACTION = "BOOKING";

    @SpringBean AuthService authService;

    public BookingEditor(ModalWindow modalWindow, IModel<Booking> model) {
        super(modalWindow, model);

        // Editable fields:
        fields.add(Booking.CUSTOMER.add(Customer.LOGIN));
        fields.add(Booking.CUSTOMER.add(Customer.PASSWORD));
        fields.add(Booking.HOTEL.add(Hotel.NAME));
        fields.add(Booking.HOTEL.add(Hotel.CITY).add(City.NAME));
        fields.add(Booking.DATE_FROM);
        fields.add(Booking.NIGHTS);
        fields.add(Booking.PERSONS);
        fields.add(Booking.PRICE);
        fields.add(Booking.CURRENCY);

        // Modify attribute(s):
        fields.getField(Booking.HOTEL.add(Hotel.NAME)).setEnabled(false);
        fields.getField(Booking.HOTEL.add(Hotel.CITY).add(City.NAME)).setEnabled(false);
        fields.getField(Booking.PRICE).setEnabled(false);
        fields.getField(Booking.CURRENCY).setEnabled(false);
    }

    /** Enable/Disable login fields */
    @Override
    protected void onBeforeRender() {
        final boolean enabled = !authService.isCustomer();
        fields.getField(Booking.CUSTOMER.add(Customer.LOGIN)).setEnabled(enabled);
        fields.getField(Booking.CUSTOMER.add(Customer.PASSWORD)).setEnabled(enabled);
        super.onBeforeRender();
    }

    /** Create the editor dialog */
    public static BookingEditor create(String componentId, int width, int height) {
        IModel<Booking> model = Model.of(new Booking());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final BookingEditor result = new BookingEditor(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new LocalizedModel("dialog.booking.title"));
        //modalWindow.setCookieName("modal-dialog");

        return result;
    }
}
