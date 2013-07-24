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
package org.ujorm.hotels.services;

import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;

/**
 * Common database service
 * @author ponec
 */
public interface DbService {

    /** Find a customer */
    public Customer getCustomer(String login);

    /** Find enabled customer  */
    public Customer findCustomer(String login, String password);

    /** Delete hotel */
    public void deleteHotel(Hotel hotel);

    /** Update hotel */
    public void updateHotel(Hotel hotel);

    /** Delete customer */
    public void deleteCustomer(Customer customer);

    /** Update customer */
    public void updateCustomer(Customer customer);

    /** Create new booking */
    public void createBooking(Booking domain);

}
