/*
 * It can be a generated class in a future.
 * License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */

package org.ujorm.hotels.entity;

import java.math.BigDecimal;
import java.sql.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import static org.ujorm.Validator.Build.*;

/** Hotel */
public class Booking extends OrmTable<Booking> {

    private static final String INDEX_NAME="idx_booking";

    /** Factory */
    private static final KeyFactory<Booking> f = newFactory(Booking.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Booking, Long> ID = f.newKey();
    /** Relation to hotel */
    @Comment("Relation to hotel")
    @Column(index=INDEX_NAME)
    public static final Key<Booking, Hotel> HOTEL = f.newKey(notNull());
    /** Relation to customer */
    @Comment("Relation to customer")
    @Column(index=INDEX_NAME)
    public static final Key<Booking, Customer> CUSTOMER = f.newKey(notNull());
    /** Date from */
    @Comment("Date from")
    public static final Key<Booking, Date> DATE_FROM = f.newKey(mandatory());
    /** Number of nights */
    @Comment("Number of nights")
    public static final Key<Booking, Short> NIGHTS = f.newKeyDefault((short)1, between((short)1, (short)365));
    /** Number of persons (limit from 1 to 20) */
    @Comment("Number of persons (limit from 1 to 50)")
    public static final Key<Booking, Short> PERSONS = f.newKeyDefault((short)1, between((short)1, (short)50));
    /** Total price */
    @Comment("Total price")
    public static final Key<Booking, BigDecimal> PRICE = f.newKeyDefault(BigDecimal.ZERO, min(MANDATORY, BigDecimal.ZERO));
    /** Currency of the price */
    @Comment("Currency of the total price")
    public static final Key<Booking, String> CURRENCY = f.newKeyDefault("USD", length(MANDATORY, 3, 3));
    /** Creation date of booking. */
    @Comment("Creation date of booking.")
    public static final Key<Booking, Date> CREATION_DATE = f.newKey(mandatory());

    static {
        f.lock();
    }

    // --- Getters / Setters ---

    //<editor-fold defaultstate="collapsed" desc="Setters and Getters generated from NetBeans">

    /** The Primary Key */
    public Long getId() {
        return ID.of(this);
    }

    /** The Primary Key */
    public void setId(Long id) {
        Booking.ID.setValue(this, id);
    }

    /** Relation to hotel */
    public Hotel getHotel() {
        return HOTEL.of(this);
    }

    /** Relation to hotel */
    public void setHotel(Hotel hotel) {
        Booking.HOTEL.setValue(this, hotel);
    }

    /** Relation to customer */
    public Customer getCustomer() {
        return CUSTOMER.of(this);
    }

    /** Relation to customer */
    public void setCustomer(Customer customer) {
        Booking.CUSTOMER.setValue(this, customer);
    }

    /** Date from */
    public Date getDateFrom() {
        return DATE_FROM.of(this);
    }

    /** Date from */
    public void setDateFrom(Date dateFrom) {
        Booking.DATE_FROM.setValue(this, dateFrom);
    }

    /** Number of nights */
    public Short getNights() {
        return NIGHTS.of(this);
    }

    /** Number of nights */
    public void setNights(Short nights) {
        Booking.NIGHTS.setValue(this, nights);
    }

    /** Number of persons (limit from 1 to 20) */
    public Short getPersons() {
        return PERSONS.of(this);
    }

    /** Number of persons (limit from 1 to 20) */
    public void setPersons(Short persons) {
        Booking.PERSONS.setValue(this, persons);
    }

    /** Total price */
    public BigDecimal getPrice() {
        return PRICE.of(this);
    }

    /** Total price */
    public void setPrice(BigDecimal price) {
        Booking.PRICE.setValue(this, price);
    }

    /** Currency of the price */
    public String getCurrency() {
        return CURRENCY.of(this);
    }

    /** Currency of the price */
    public void setCurrency(String currency) {
        Booking.CURRENCY.setValue(this, currency);
    }

    /** Reservation date */
    public Date getReservationDate() {
        return CREATION_DATE.of(this);
    }

    /** Reservation date */
    public void setReservationDate(Date reservationDate) {
        Booking.CREATION_DATE.setValue(this, reservationDate);
    }

    //</editor-fold>

}
