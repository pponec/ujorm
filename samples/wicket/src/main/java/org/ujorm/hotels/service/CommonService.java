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
package org.ujorm.hotels.service;

import java.time.Period;
import java.util.List;
import org.apache.wicket.model.IModel;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.wicket.UjoEvent;

/**
 * Common service interfaces
 * @author ponec
 */
public interface CommonService {
    /** System account is {@code null} always */
    public static final Customer SYSTEM_ACCOUNT = null;

    /** The one day */
    public static final Period ONE_DAY = Period.ofDays(1);

    /** Find a customer */
    Customer getCustomer(String login);

    /** Find enabled customer  */
    Customer findCustomer(String login, String password);

    /** Delete hotel if no related booking was found, or inactive it */
    void deleteHotel(Hotel hotel);

    /** Update hotel */
    void saveOrUpdateHotel(Hotel hotel);

    /** Delete customer if no related booking was found, or inactive it */
    void deleteCustomer(Customer customer);

    /** Insert or Update customer */
    void saveOrUpdateCustomer(Customer customer);

    /** Reload hotel from database and build new Booking model */
    IModel<Booking> prepareBooking(UjoEvent<Hotel> event);

    /** Create new booking */
    void saveBooking(Booking domain);

    /** Booking in the feature can be removed by its customer, or an administrator */
    void deleteBooking(Booking domain);

    /** Returns a booking criterion */
    Criterion<Booking> getBookingPreview();

    /** Is the measuring code enabled? */
    boolean isMeasuringCode();

    /** Find hotels according to condition. Max row count is 1000. */
    List<Hotel> findHotels(Criterion<? extends Hotel> condition);
}
