/*
 * Copyright 2014, Pavel Ponec
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
package org.ujorm.hotels.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.config.SpringContext;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.CommonService;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.spring.CommonDao;
import org.ujorm.tools.Assert;
import org.ujorm.validator.ValidationException;
import org.ujorm.wicket.UjoEvent;
import static org.ujorm.hotels.service.CommonService.ONE_DAY;
import static org.ujorm.hotels.tools.EncTool.getHash;
import static org.ujorm.tools.Check.hasLength;
/**
 * Common database service implementations
 * @author ponec
 */
@Transactional(SpringContext.TRANSACTION_MANAGER)
public class CommonServiceImpl implements CommonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonServiceImpl.class);

    /** Common DAO layer */
    private final CommonDao<OrmUjo> dao;

    @Inject
    private AuthService authService;

    public CommonServiceImpl(CommonDao<OrmUjo> dao) {
        this.dao = dao;
    }

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
        final Query<Customer> query = dao.createQuery(Customer.LOGIN.whereEq(login));
        return query.uniqueResult();
    }

    /** {@inheritDoc } */
    @Override
    public void deleteHotel(Hotel hotel) {
        LOGGER.info("Delete hotel {}", hotel);
        checkReadOnly(hotel);

        boolean booking = dao.exists(Booking.HOTEL.whereEq(hotel));
        if (booking) {
            hotel.setActive(false);
            dao.update(hotel);
        } else {
            dao.delete(hotel);
        }
    }

    /** {@inheritDoc } */
    @Override
    public void saveOrUpdateHotel(Hotel hotel) {
        LOGGER.info("Save or update hotel {}", hotel);
        checkReadOnly(hotel);
        dao.insertOrUpdate(hotel);
    }

    @Override
    public void deleteCustomer(Customer customer) {
        LOGGER.info("Delete customer {}", customer);
        checkReadOnly(customer);

        boolean booking = dao.exists(Booking.CUSTOMER.whereEq(customer));
        if (booking) {
            customer.setActive(false);
            dao.update(customer);
        } else {
            dao.delete(customer);
        }
    }

    /** Insert or update customer */
    @Override
    public void saveOrUpdateCustomer(Customer customer) {
        final boolean newMode = customer.getId() == null;
        LOGGER.info("{} customer {}", newMode ? "Save" : "Update", customer);
        checkReadOnly(customer);

        // Check a unique login:
        if (newMode && dao.exists(Customer.LOGIN.whereEq(customer.getLogin()))) {
            throw new ValidationException("login.occupied", "Login is occupied");
        }

        // Check the not-null password:
        if (newMode && customer.getPassword()==null) {
            throw new ValidationException("password.empty", "The password must not be empty");
        }

        final String password = customer.getPassword();
        if (hasLength(password)) {
            customer.writeSession(newMode ? null : dao.getSession() ); // Activate modifications for EditMode
            customer.setPasswordHash(getHash(password));
        }
        dao.insertOrUpdate(customer);
    }

    /** Authenticate the user */
    @Override
    public Customer findCustomer(String login, String password) {
        final Criterion<Customer> crn1, crn2, crn3, crn4;
        crn1 = Customer.LOGIN.whereEq(login);
        crn2 = Customer.PASSWORD_HASH.whereEq(getHash(password));
        crn3 = Customer.ACTIVE.whereEq(true);
        crn4 = crn1.and(crn2).and(crn3);

        return dao.createQuery(crn4).uniqueResult();
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
        result.setHotel(dao.getSession().loadBy(event.getDomain()));
        result.setPrice(result.getHotel().getPrice());
        result.setCurrency(result.getHotel().getCurrency());
        result.setDateFrom(java.time.LocalDate.now().plus(ONE_DAY));
        result.setCustomer(authService.getLoggedCustomer(new Customer()));
        result.getHotel().getCity(); // Fetching City

        return Model.of(result);
    }

    /** Insert new booking */
    @Override
    public void saveBooking(Booking booking) {
        Customer cust = Assert.notNull(booking.getCustomer(), Booking.CUSTOMER.getFullName());
        if (cust.getId()==null) {
            if (!authService.authenticate(cust)) {
                throw new ValidationException("login.failed", "Login failed");
            }
            booking.setCustomer(authService.getLoggedCustomer());
        }
        booking.setPrice(booking.getHotel().getPrice()
            .multiply(new BigDecimal(booking.getPersons().intValue() * booking.getNights().intValue())));

        // TODO: validations ...

        booking.setPrice(totalPrice(booking));
        booking.setCreationDate(LocalDateTime.now());
        dao.insert(booking);
    }

    /** Booking in the feature can be removed by its customer, or an administrator */
    @Override
    public void deleteBooking(Booking booking) {
        // TODO: check permissions, ...
        LOGGER.info("Delete Booking {}", booking);
        dao.delete(booking);
    }

    /** Returns a booking criterion */
    @Override
    public Criterion<Booking> getBookingPreview() {
        Customer cust = authService.getLoggedCustomer();
        if (cust == null) {
            return Booking.ID.forNone();
        }
        Criterion<Booking> result = Booking.DATE_FROM.whereGe(now(ONE_DAY.negated()));
        if (!cust.getAdmin()) {
            result = result.and(Booking.CUSTOMER.whereEq(cust));
        }
        return result;
    }

    /** Get Current SQL time */
    private java.time.LocalDate now(Period shift) {
        return java.time.LocalDate.now().plus(shift);
    }

    /** Calculate total price */
    private BigDecimal totalPrice(Booking booking) {
        return booking.getHotel().getPrice().multiply(new BigDecimal(booking.getNights() * booking.getPersons()));
    }

    @Override
    public List<Hotel> findHotels(Criterion<? extends Hotel> condition) {
        final Criterion<Hotel> crn = (Criterion<Hotel>) condition;
        final Query<Hotel> query = dao.createQuery(crn)
                .addColumn(Hotel.CITY)
                .setLimit(1000);
        return query.list();
    }
}
