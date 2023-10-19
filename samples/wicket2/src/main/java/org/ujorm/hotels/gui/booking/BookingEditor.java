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

import java.math.BigDecimal;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.ujorm.Validator;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.wicket.component.dialog.domestic.EntityDialogPane;
import org.ujorm.wicket.component.form.FieldEvent;
import org.ujorm.wicket.component.form.FieldProvider;
import org.ujorm.wicket.component.tools.LocalizedModel;
import static org.ujorm.validator.impl.ValidatorFactory.futureLocalDate;
/**
 * Booking Editor
 * @author Pavel Ponec
 */
public class BookingEditor<U extends Booking> extends EntityDialogPane<U> {
    private static final long serialVersionUID = 0L;

    /** Default value is the same like the field */
    public static final String BOOKING_ACTION = "BOOKING";

    @SpringBean AuthService authService;
    final FieldProvider<U> fieldBuilder;

    public BookingEditor(ModalWindow modalWindow, IModel<U> model) {
        super(modalWindow, model);

        // Editable fields:
        fieldBuilder = getFieldBuilder();
        fieldBuilder.add(Booking.CUSTOMER.add(Customer.LOGIN));
        fieldBuilder.add(Booking.CUSTOMER.add(Customer.PASSWORD));
        fieldBuilder.add(Booking.HOTEL.add(Hotel.NAME));
        fieldBuilder.add(Booking.HOTEL.add(Hotel.CITY).add(City.NAME));
        fieldBuilder.add(Booking.PERSONS);
        fieldBuilder.add(Booking.DATE_FROM).addCssStyle("date");
        fieldBuilder.getField(Booking.DATE_FROM).addValidator(Validator.Build.futureLocalDate());
        fieldBuilder.add(Booking.NIGHTS);
        fieldBuilder.add(Booking.PRICE);
        fieldBuilder.add(Booking.CURRENCY);

        // Modify attribute(s):
        fieldBuilder.setEnabled(Booking.HOTEL.add(Hotel.NAME), false);
        fieldBuilder.setEnabled(Booking.HOTEL.add(Hotel.CITY).add(City.NAME), false);
        fieldBuilder.setEnabled(Booking.PRICE, false);
        fieldBuilder.setEnabled(Booking.CURRENCY, false);
        fieldBuilder.addValidatorUnchecked(Booking.DATE_FROM, futureLocalDate());

        // Ajax Events.
        fieldBuilder.onChange(Booking.NIGHTS);
        fieldBuilder.onChange(Booking.PERSONS);
    }

    /** Enable/Disable login fields */
    @Override
    protected void onBeforeRender() {
        final boolean enabled = !authService.isLogged();
        fieldBuilder.setEnabled(Booking.CUSTOMER.add(Customer.LOGIN), enabled);
        fieldBuilder.setVisible(Booking.CUSTOMER.add(Customer.PASSWORD), enabled);
        super.onBeforeRender();
    }

    /** Calculate price */
    @Override
    public void onEvent(IEvent<?> iEvent) {
        final FieldEvent event = FieldEvent.get(iEvent);
        if (event.hasAction()) {
            try {
                short nights = fieldBuilder.getValue(Booking.NIGHTS);
                short persons = fieldBuilder.getValue(Booking.PERSONS);
                BigDecimal price = fieldBuilder.getInputDomain().getHotel().getPrice()
                        .multiply(new BigDecimal((int) nights * persons));
                fieldBuilder.setValue(Booking.PRICE, price, event.getRequestTarget());
                iEvent.stop();
            } catch (Exception e) {
                e.printStackTrace();
                fieldBuilder.setValue(Booking.PRICE, BigDecimal.ZERO, event.getRequestTarget());
            }
        }
    }

    /** Create the editor dialog */
    public static BookingEditor create(String componentId, int width, int height) {
        IModel<Booking> model = Model.of(new Booking());
        final ModalWindow modalWindow = new ModalWindow(componentId, model);
        modalWindow.setCssClassName(ModalWindow.CSS_CLASS_BLUE);

        final BookingEditor<Booking> result = new BookingEditor<Booking>(modalWindow, model);
        modalWindow.setInitialWidth(width);
        modalWindow.setInitialHeight(height);
        modalWindow.setTitle(new LocalizedModel("dialog.booking.title"));
        //modalWindow.setCookieName(componentId + "-modalDialog");

        return result;
    }
}
