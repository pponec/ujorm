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
package org.ujorm.hotels.services.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.services.*;
import org.ujorm.validator.ValidationException;
import org.ujorm.wicket.UjoEvent;
import static org.ujorm.core.UjoManager.*;
/**
 * Common database service implementations
 * @author ponec
 */
@Transactional
public class DbServiceImpl extends AbstractServiceImpl implements DbService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbServiceImpl.class);

    @Autowired
    private AuthService authService;

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
        return super.createQuery(Customer.LOGIN.whereEq(login)).uniqueResult();
    }

    /** {@inheritDoc } */
    @Override
    public void deleteHotel(Hotel hotel) {
        LOGGER.info("Delete hotel {}", hotel);
        checkReadOnly(hotel);

        boolean booking = getSession().exists(Booking.HOTEL.whereEq(hotel));
        if (booking) {
            hotel.setActive(false);
            getSession().update(hotel);
        } else {
           getSession().update(hotel);
        }
    }

    /** {@inheritDoc } */
    @Override
    public void saveOrUpdateHotel(Hotel hotel) {
        LOGGER.info("Save or update hotel {}", hotel);
        checkReadOnly(hotel);
        getSession().saveOrUpdate(hotel);
    }

    @Override
    public void deleteCustomer(Customer customer) {
        LOGGER.info("Delete customer {}", customer);
        checkReadOnly(customer);

        boolean booking = getSession().exists(Booking.CUSTOMER.whereEq(customer));
        if (booking) {
            customer.setActive(false);
            getSession().update(customer);
        } else {
           getSession().update(customer);
        }
    }

    /** Insert or update customer */
    @Override
    public void saveOrUpdateCustomer(Customer customer) {
        final boolean newMode = customer.getId() == null;
        LOGGER.info("{} customer {}", newMode ? "Save" : "Update", customer);
        checkReadOnly(customer);

        // Check a unique login:
        if (newMode && getSession().exists(Customer.LOGIN.whereEq(customer.getLogin()))) {
            throw new ValidationException("login.occupied", "Login is occupied");
        }

        // Check the not-null password:
        if (newMode && customer.getPassword()==null) {
            throw new ValidationException("password.empty", "The password must not be empty");
        }

        final String password = customer.getPassword();
        if (isFilled(password)) {
            customer.writeSession(newMode ? null : getSession() ); // Activate modifications for EditMode
            customer.setPasswordHash(authService.getHash(password));
        }
        getSession().saveOrUpdate(customer);
    }

    /** Authenticate the user */
    @Override
    public Customer findCustomer(String login, String password) {
        final Criterion<Customer> crn1, crn2, crn3, crn4;
        crn1 = Customer.LOGIN.whereEq(login);
        crn2 = Customer.PASSWORD_HASH.whereEq(authService.getHash(password));
        crn3 = Customer.ACTIVE.whereEq(true);
        crn4 = crn1.and(crn2).and(crn3);

        return getSession().createQuery(crn4).uniqueResult();
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
        result.setHotel(getSession().loadBy(event.getDomain()));
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
        Customer cust = Args.notNull(booking.getCustomer(), Booking.CUSTOMER.toStringFull());
        if (cust.getId()==null) {
            if (!authService.authenticate(cust)) {
                throw new ValidationException("login.failed", "Login failed");
            }
            booking.setCustomer(authService.getCurrentCustomer());
        }
        // TODO: validations ...

        booking.setPrice(totalPrice(booking));
        booking.setReservationDate(new java.sql.Date(System.currentTimeMillis()));
        getSession().save(booking);
    }

    /** Booking in the feature can be removed by its customer, or an administrator */
    @Override
    public void deleteBooking(Booking booking) {
        // TODO: check permissions, ...
        LOGGER.info("Delete Booking {}", booking);
        getSession().delete(booking);
    }

    /** Returns a booking criterion */
    @Override
    public Criterion<Booking> getBookingPreview() {
        Customer cust = authService.getCurrentCustomer();
        if (cust == null) {
            return Booking.ID.forNone();
        }
        Criterion<Booking> result = Booking.DATE_FROM.whereGe(now(-DAY_AS_MILISEC));
        if (!cust.getAdmin()) {
            result = result.and(Booking.CUSTOMER.whereEq(cust));
        }
        return result;
    }

    /** Get Current SQL time */
    private java.sql.Date now(long shift) {
        return new java.sql.Date(System.currentTimeMillis() + shift);
    }

    /** Calculate total price */
    private BigDecimal totalPrice(Booking booking) {
        return booking.getHotel().getPrice().multiply(new BigDecimal(booking.getNights() * booking.getPersons()));
    }

}
