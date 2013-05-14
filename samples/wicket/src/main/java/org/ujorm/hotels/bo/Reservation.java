/*
 * It can be a generated class in a future.
 * License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */

package org.ujorm.hotels.bo;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Comment;
import org.ujorm.core.KeyFactory;
import static org.ujorm.Validator.Build.*;

/** Hotel */
abstract public class Reservation extends OrmTable<Reservation> {

    private static final String UNIQUE_HOTEL_NAME="idx_hotel_name";
    
    /** Factory */
    private static final KeyFactory<Reservation> f = newFactory(Reservation.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Reservation, Long> ID = f.newKey();
    /** Relation to hotel */
    @Comment("Relation to hotel")
    public static final Key<Reservation, Hotel> HOTEL = f.newKey(notNull());
    /** Relation to customer */
    @Comment("Relation to customer")
    public static final Key<Reservation, Customer> CUSTOMER = f.newKey(notNull());
    /** Date from */
    @Comment("Date from")
    public static final Key<Reservation, Date> DATE_FROM = f.newKey(mandatory());
    /** Date to */
    @Comment("Date to")
    public static final Key<Reservation, Date> DATE_TO = f.newKey(mandatory());
    /** Reservation date */
    @Comment("Reservation date")
    public static final Key<Reservation, Date> RESERVATION_DATE = f.newKey(mandatory());
    
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
        Reservation.ID.setValue(this, id);
    }
    
    /** Relation to hotel */
    public Hotel getHotel() {
        return HOTEL.of(this);
    }
    
    /** Relation to hotel */
    public void setHotel(Hotel hotel) {
        Reservation.HOTEL.setValue(this, hotel);
    }
    
    /** Relation to customer */
    public Customer getCustomer() {
        return CUSTOMER.of(this);
    }
    
    /** Relation to customer */
    public void setCustomer(Customer customer) {
        Reservation.CUSTOMER.setValue(this, customer);
    }
    
    /** Date from */
    public Date getDateFrom() {
        return DATE_FROM.of(this);
    }
    
    /** Date from */
    public void setDateFrom(Date dateFrom) {
        Reservation.DATE_FROM.setValue(this, dateFrom);
    }
    
    /** Date to */
    public Date getDateTo() {
        return DATE_TO.of(this);
    }
    
    /** Date to */
    public void setDateTo(Date dateTo) {
        Reservation.DATE_TO.setValue(this, dateTo);
    }
    
    /** Reservation date */
    public Date getReservationDate() {
        return RESERVATION_DATE.of(this);
    }
    
    /** Reservation date */
    public void setReservationDate(Date reservationDate) {
        Reservation.RESERVATION_DATE.setValue(this, reservationDate);
    }
    //</editor-fold>
    

}
