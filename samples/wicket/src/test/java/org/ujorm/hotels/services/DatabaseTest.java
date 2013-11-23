/*
 ** Copyright 2013, Pavel Ponec
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

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Booking;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.services.impl.AbstractServiceImpl;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.Query;
import org.ujorm.orm.template.AliasTable;
import static org.junit.Assert.*;
import static org.ujorm.orm.template.AliasTable.Build.*;

/**
 * Sample code for new article
 * @author Pavel Ponec
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:org/ujorm/hotels/config/applicationContext.xml"})
public class DatabaseTest extends AbstractServiceImpl {
    /** The one day in MILIS */
    private static final int ONE_DAY = 24 * 60 * 60 * 1000;

    /** Create a one reservation in the Prague */
    @Before
    @Transactional
    public void setUp() {
        final String login = "test";
        final String city = "Prague";
        if (!createQuery(Booking.ID.forAll()).exists()) {
            Customer customer = createQuery(Customer.LOGIN.whereEq(login)).uniqueResult();
            Hotel hotel = createQuery(Hotel.CITY.add(City.NAME).whereEq(city)).setLimit(1).uniqueResult();
            //
            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setHotel(hotel);
            booking.setDateFrom(new Date(System.currentTimeMillis() + ONE_DAY));
            booking.setPrice(hotel.getPrice());
            booking.setReservationDate(now());

            getSession().save(booking);
        }
    }

    /** Database query using the Ujorm <strong>Keys</strong> */
    @Test
    @Transactional
    public void testDbQueries() {

        // Simple criterion:
        Key<Booking, Date> dateFrom = Booking.DATE_FROM;
        Criterion<Booking> crn1 = dateFrom.whereGt(now());
        List<Booking> futureAccommodations = crn1.evaluate(getBookings());
        assertEquals(1, futureAccommodations.size());

        // Composite keys:
        Key<Booking, Hotel> bookingHotel = Booking.HOTEL;
        Key<Hotel, City> hotelCity = Hotel.CITY;
        Key<City, String> cityName = City.NAME;
        Key<Booking, String> bookingCityName;
        bookingCityName = bookingHotel.add(hotelCity).add(cityName);
        assertEquals("HOTEL.CITY.NAME", bookingCityName.toString());

        // Building criterions:
        Criterion<Booking> crn2 = bookingCityName.whereEq("Prague");
        Criterion<Booking> crn3 = crn1.and(crn2);
        Criterion<Booking> crn4 = crn1.and(crn2.or(anotherCriterion()));
        assertEquals(1, crn4.evaluate(getBookings()).size());

        // Build query:
        Query<Booking> bookings = createQuery(crn3);
        List<Booking> result = bookings.list();
        assertFalse(result.isEmpty());

        // Fetch columns:
        bookings.addColumn(bookingHotel.add(hotelCity).add(City.ID));
        // Ordering:
        bookings.orderBy(Booking.DATE_FROM);
        bookings.orderBy(Booking.PRICE.descending());
    }

    /** Database query using the Ujorm <strong>Keys</strong> */
    @Test
    @Transactional
    public void testNativeCriterion() {
        Key<Booking, String> bookingCityName = Booking.HOTEL
                .add(Hotel.CITY).add(City.NAME);

        String[] cities  = {"Prague", "Amsterdam"};
        Criterion crn = bookingCityName.forSqlUnchecked("{0} IN ({1})", cities);

        Query<Booking> bookings = createQuery(crn);
        List<Booking> result = bookings.list();
        assertFalse(result.isEmpty());
    }

    /** Database query using the Ujorm <strong>Keys</strong> */
    //@Test
    @Transactional
    public void testNativeQuery_1() {
        OrmHandler handler = getSession().getHandler();
        AliasTable<Booking> booking = handler.alias(Booking.class, "a");
        AliasTable<Hotel> hotel = handler.alias(Hotel.class, "b");
        AliasTable<City> city = handler.alias(City.class, "c");

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
    private Date now() {
        return new Date(System.currentTimeMillis());
    }

    /** Returns two Booking objects with different DateFrom attribute value */
    private List<Booking> getBookings() {
        List<Booking> result = new ArrayList<Booking>();

        Booking item1 = new Booking();
        result.add(item1);
        item1.setDateFrom(new Date(System.currentTimeMillis() - ONE_DAY));

        Booking item2 = new Booking();
        result.add(item2);
        item2.setDateFrom(new Date(System.currentTimeMillis() + ONE_DAY));

        return result;
    }

    /** Returns some next Criterion */
    private Criterion<Booking> anotherCriterion() {
        return Booking.CURRENCY.whereEq("USD");
    }
}
