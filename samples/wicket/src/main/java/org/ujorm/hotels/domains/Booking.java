/*
 * It can be a generated class in a future.
 * License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */

package org.ujorm.hotels.domains;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import static org.ujorm.Validator.Build.*;

/** Hotel */
public class Booking extends OrmTable<Booking> {

    private static final String UNIQUE_BOOKING_NAME="idx_booking";
    
    /** Factory */
    private static final KeyFactory<Booking> f = newFactory(Booking.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Booking, Long> ID = f.newKey();
    /** Relation to hotel */
    @Comment("Relation to hotel")
    @Column(uniqueIndex=UNIQUE_BOOKING_NAME)
    public static final Key<Booking, Hotel> HOTEL = f.newKey(notNull());
    /** Relation to customer */
    @Comment("Relation to customer")
    @Column(uniqueIndex=UNIQUE_BOOKING_NAME)
    public static final Key<Booking, Customer> CUSTOMER = f.newKey(notNull());
    /** Date from */
    @Comment("Date from")
    public static final Key<Booking, Date> DATE_FROM = f.newKey(mandatory());
    /** Date to */
    @Comment("Date to")
    public static final Key<Booking, Date> DATE_TO = f.newKey(mandatory());
    /** Reservation date */
    @Comment("Reservation date")
    public static final Key<Booking, Date> RESERVATION_DATE = f.newKey(mandatory());
    
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
    
    /** Date to */
    public Date getDateTo() {
        return DATE_TO.of(this);
    }
    
    /** Date to */
    public void setDateTo(Date dateTo) {
        Booking.DATE_TO.setValue(this, dateTo);
    }
    
    /** Reservation date */
    public Date getReservationDate() {
        return RESERVATION_DATE.of(this);
    }
    
    /** Reservation date */
    public void setReservationDate(Date reservationDate) {
        Booking.RESERVATION_DATE.setValue(this, reservationDate);
    }
    //</editor-fold>
    

}
