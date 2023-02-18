/*
 * Copyright 2013-2017, Pavel Ponec
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.config.SpringContext;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.template.AliasTable;
import org.ujorm.spring.CommonDao;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.ujorm.orm.template.AliasTable.Build.*;

/**
 * Sample code for new article
 * @author Pavel Ponec
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = SpringContext.class)
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseTest {
    /** The one day in MILIS */
    private static final Period ONE_DAY = Period.ofDays(1);

    @Inject
    private CommonDao<OrmUjo> dao;

    /** Create a one reservation in the Prague */
//    @BeforeEach
//    @Transactional(SpringContext.TRANSACTION_MANAGER)
    public void setUp() {
        final String login = "test";
        final String city = "Prague";
        if (!dao.exists(Booking.ID.forAll())) {
            Customer customer = dao.createQuery(Customer.LOGIN.whereEq(login)).uniqueResult();
            Hotel hotel = dao.createQuery(Hotel.CITY.add(City.NAME).whereEq(city)).setLimit(1).uniqueResult();
            //
            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setHotel(hotel);
            booking.setDateFrom(LocalDate.now().plus(ONE_DAY));
            booking.setPrice(hotel.getPrice());
            booking.setCreationDate(LocalDateTime.now());

            dao.insert(booking);
        }
    }

    /** Database query using the Ujorm <strong>Keys</strong> */
    @Disabled
    @Transactional(SpringContext.TRANSACTION_MANAGER)
    @Test
    public void testDbQueries() {

        // Simple criterion:
        Key<Booking, LocalDate> dateFrom = Booking.DATE_FROM;
        Criterion<Booking> crn1 = dateFrom.whereGt(now());
        List<Booking> futureAccommodations = crn1.findAll(getBookings());
        assertEquals(1, futureAccommodations.size());

        // Composite keys:
        Key<Booking, Hotel> bookingHotel = Booking.HOTEL;
        Key<Hotel, City> hotelCity = Hotel.CITY;
        Key<City, String> cityName = City.NAME;
        Key<Booking, String> bookingCityName;
        bookingCityName = bookingHotel.add(hotelCity).add(cityName);
        assertEquals("hotel.city.name", bookingCityName.toString());

        // Building criterions:
        Criterion<Booking> crn2 = bookingCityName.whereEq("Prague");
        Criterion<Booking> crn3 = crn1.and(crn2);
        Criterion<Booking> crn4 = crn1.and(crn2.or(anotherCriterion()));
        assertEquals(1, crn4.findAll(getBookings()).size());

        // Build query:
        Query<Booking> bookings = dao.createQuery(crn3);
        List<Booking> result = bookings.list();
        assertFalse(result.isEmpty());

        // Fetch columns:
        bookings.addColumn(bookingHotel.add(hotelCity).add(City.ID));
        // Ordering:
        bookings.orderBy(Booking.DATE_FROM);
        bookings.orderBy(Booking.PRICE.descending());
    }

    /** Database query using the Ujorm <strong>Keys</strong> */
    @Disabled
    @Test
    @Transactional(SpringContext.TRANSACTION_MANAGER)
    public <T extends Booking> void testNativeCriterion() {
        Key<Booking, String> bookingCityName = Booking.HOTEL
                .add(Hotel.CITY).add(City.NAME);

        String[] cities  = {"Prague", "Amsterdam"};
        Criterion crn = bookingCityName.forSqlUnchecked("{0} IN ({1})", cities);

        Query<T> bookings = dao.createQuery(crn);
        List<T> result = bookings.list();
        assertFalse(result.isEmpty());
    }

    /** Database query using the Ujorm <strong>Keys</strong> */
    //@Test
    @Disabled
    @Transactional(SpringContext.TRANSACTION_MANAGER)
    @Test
    public void testNativeQuery_1() {
        OrmHandler handler = dao.getSession().getHandler();
        AliasTable<Booking> booking = handler.tableOf(Booking.class, "a");
        AliasTable<Hotel> hotel = handler.tableOf(Hotel.class, "b");
        AliasTable<City> city = handler.tableOf(City.class, "c");

        String sql
                = SELECT( booking.column(Booking.ID)
                        , booking.column(Booking.DATE_FROM))
                + FROM (booking)
                + INNER_JOIN(hotel, hotel.column(Hotel.ID), "=", booking.column(Booking.HOTEL))
                + INNER_JOIN(city, city.column(City.ID), "=", hotel.column(Hotel.CITY))
                + WHERE(city.column(City.NAME), "=", "Prague");

        System.out.println("sql: " + sql);
    }

    // ---------- HELPFUL METHODS ----------

    /** Returns the current day */
    private LocalDate now() {
        return LocalDate.now();
    }

    /** Returns two Booking objects with different DateFrom attribute value */
    private List<Booking> getBookings() {
        List<Booking> result = new ArrayList<Booking>();

        Booking item1 = new Booking();
        result.add(item1);
        item1.setDateFrom(LocalDate.now().plus(ONE_DAY.negated()));

        Booking item2 = new Booking();
        result.add(item2);
        item2.setDateFrom(LocalDate.now().plus(ONE_DAY));

        return result;
    }

    /** Returns some next Criterion */
    private Criterion<Booking> anotherCriterion() {
        return Booking.CURRENCY.whereEq("USD");
    }

}
