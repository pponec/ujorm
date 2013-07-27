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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.Key;
import org.ujorm.Ujo;
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

    /** Read only sign */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
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
    public void updateHotel(Hotel hotel) {
        LOGGER.info("Update hotel {}", hotel);
        checkReadOnly(hotel);
        getSession().update(hotel);
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

    /** Update customer */
    @Override
    public void updateCustomer(Customer customer) {
        LOGGER.info("Update customer {}", customer);
        checkReadOnly(customer);

        String password = customer.get(Customer.PASSWORD);
        if (isFilled(password)) {
            customer.writeSession(getSession()); // Activate modifications
            customer.set(Customer.PASSWORD_HASH, authService.getHash(password));
        }
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
    private void checkReadOnly(Ujo ujo) throws ValidationException {
        if (readOnly) {

            Key<Ujo,Integer> key = (Key<Ujo,Integer>) ujo.readKeys().find("ID");
            if (key != null
            &&  key.isTypeOf(Integer.class)
            &&  key.of(ujo).compareTo(0) > 0) {
                return; // User data can be modified only.
            }

            throw new ValidationException("exception.readOnly"
                , "There is not allowed to modify a demo data"
                + ", download the project for all features.");
        }
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
                throw new ValidationException("wrong.login", "Login failed");
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
        Criterion<Booking> result = Booking.DATE_FROM.whereGe(now());
        if (!cust.getAdmin()) {
            result = result.and(Booking.CUSTOMER.whereEq(cust));
        }
        return result;
    }

    /** Get Current SQL time */
    private java.sql.Date now() {
        return new java.sql.Date(System.currentTimeMillis());
    }

    /** Calculate total price */
    private BigDecimal totalPrice(Booking booking) {
        return booking.getHotel().getPrice().multiply(new BigDecimal(booking.getNights() * booking.getPersons()));
    }

}
