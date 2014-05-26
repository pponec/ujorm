/*
 * Copyright 2013 ponec.
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
package org.ujorm.wicketForms.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.ujorm.criterion.Criterion;
import org.ujorm.validator.ValidationException;
import org.ujorm.wicket.UjoEvent;
import org.ujorm.wicketForms.config.demoData.DataLoader;
import org.ujorm.wicketForms.entity.Booking;
import org.ujorm.wicketForms.entity.City;
import org.ujorm.wicketForms.entity.Customer;
import org.ujorm.wicketForms.entity.Hotel;
import org.ujorm.wicketForms.services.AuthService;
import org.ujorm.wicketForms.services.DbService;
/**
 * Common database service implementations
 * @author ponec
 */
public class DbServiceImpl extends AbstractServiceImpl implements DbService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbServiceImpl.class);

    @Autowired
    private AuthService authService;

    /** Hotels */
    private List<Hotel> hotels;

    /** Read only sign */
    private boolean readOnly;
    /** Is the measuring code enabled? */
    private boolean measuringCode;

    /** Read only sign */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** Is the measuring code enabled? */
    @Override
    public boolean isMeasuringCode() {
        return measuringCode;
    }

    /** Is the measuring code enabled? */
    public void setMeasuringCode(boolean measuringCode) {
        this.measuringCode = measuringCode;
    }

    /** Load Customer by login using a transaction. */
    @Override
    public Customer getCustomer(String login) {
        return null;
    }

    /** {@inheritDoc } */
    @Override
    public void deleteHotel(Hotel hotel) {
        LOGGER.info("Delete hotel {}", hotel);
    }

    /** {@inheritDoc } */
    @Override
    public void saveOrUpdateHotel(Hotel hotel) {
        LOGGER.info("Save or update hotel {}", hotel);
    }

    @Override
    public void deleteCustomer(Customer customer) {
        LOGGER.info("Delete customer {}", customer);
    }

    /** Insert or update customer */
    @Override
    public void saveOrUpdateCustomer(Customer customer) {
        LOGGER.info("Update customer {}", customer);
    }

    /** Authenticate the user */
    @Override
    public Customer findCustomer(String login, String password) {
        final Criterion<Customer> crn1, crn2, crn3, crn4;
        crn1 = Customer.LOGIN.whereEq(login);
        crn2 = Customer.PASSWORD_HASH.whereEq(authService.getHash(password));
        crn3 = Customer.ACTIVE.whereEq(true);
        crn4 = crn1.and(crn2).and(crn3);

        return null;
    }

    /** Check a read-only state */
    private void checkReadOnly(Hotel ujo) throws ValidationException {
        final Long id = ujo.getId();
        if (readOnly
        && id !=null
        && id.compareTo(0L) < 0) {
            throwReadOnlyException();
        }
    }

    /** Check a read-only state */
    private void checkReadOnly(Customer ujo) throws ValidationException {
        if (readOnly
        && ujo.getId() != null
        && Arrays.asList("demo","test","admin").contains(ujo.getLogin())) {
            throwReadOnlyException();
        }
    }

    /** Throws a read-only exception */
    private void throwReadOnlyException() throws ValidationException {
        throw new ValidationException("exception.readOnly"
            , "There is not allowed to modify a demo data"
            + ", download the project for all features.");
    }

    /** Reload hotel from database and build new Booking model */
    @Override
    public IModel<Booking> prepareBooking(final UjoEvent<Hotel> event) {
        Booking result = new Booking();
        result.setHotel(null);
        result.setPrice(result.getHotel().getPrice());
        result.setCurrency(result.getHotel().getCurrency());
        result.setDateFrom(new java.sql.Date(System.currentTimeMillis() + DAY_AS_MILISEC));
        result.setCustomer(authService.getCurrentCustomer(new Customer()));
        result.getHotel().getCity(); // Fetching City

        return Model.of(result);
    }

    /** Save new booking */
    @Override
    public void saveBooking(Booking booking) {
    }

    /** Booking in the feature can be removed by its customer, or an administrator */
    @Override
    public void deleteBooking(Booking booking) {
        // TODO: check permissions, ...
        LOGGER.info("Delete Booking {}", booking);
    }

    /** Returns a booking criterion */
    @Override
    public Criterion<Booking> getBookingPreview() {
        return null;
    }

    /** Get Current SQL time */
    private java.sql.Date now(long shift) {
        return new java.sql.Date(System.currentTimeMillis() + shift);
    }

    /** Calculate total price */
    private BigDecimal totalPrice(Booking booking) {
        return booking.getHotel().getPrice().multiply(new BigDecimal(booking.getNights() * booking.getPersons()));
    }

    /** Return all hotels with City attribues */
    @Override
    public synchronized List<Hotel> getHotels() {
        if (hotels == null) {
            final DataLoader dataLoader = new DataLoader();
            final Map<Integer,City> cityMap = dataLoader.getCityMap();

            hotels = dataLoader.getHotels();
            for (Hotel hotel : hotels) {
                final Integer cityId = hotel.getCity().getId();
                hotel.setCity(cityMap.get(cityId));
            }
        }
        return new ArrayList<>(hotels);
    }

}
