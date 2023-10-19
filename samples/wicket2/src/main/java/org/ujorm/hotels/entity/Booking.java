/* License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */
package org.ujorm.hotels.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.ujorm.Key;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;
import static org.ujorm.validator.impl.ValidatorFactory.MANDATORY;
import static org.ujorm.validator.impl.ValidatorFactory.length;
import static org.ujorm.validator.impl.ValidatorFactory.mandatory;
import static org.ujorm.validator.impl.ValidatorFactory.min;
import static org.ujorm.validator.impl.ValidatorFactory.notNull;
import static org.ujorm.validator.impl.ValidatorFactory.range;

/** Reservation */
@Table("demo_booking")
public final class Booking extends OrmTable<Booking> {
    /** Index name */
    private static final String INDEX_NAME="idx_booking";

    /** Factory */
    private static final OrmKeyFactory<Booking> f = newCamelFactory(Booking.class);

    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Booking, Long> ID = f.newKey();
    @Comment("Relation to hotel")
    @Column(index=INDEX_NAME)
    public static final Key<Booking, Hotel> HOTEL = f.newKey(notNull(Hotel.class));
    @Comment("Relation to customer")
    @Column(index=INDEX_NAME)
    public static final Key<Booking, Customer> CUSTOMER = f.newKey(notNull(Customer.class));
    @Comment("Date from")
    public static final Key<Booking, LocalDate> DATE_FROM = f.newKey(mandatory(LocalDate.class));
    @Comment("Number of nights")
    public static final Key<Booking, Short> NIGHTS = f.newKeyDefault((short)1, range((short)1, (short)365));
    @Comment("Number of persons (limit from 1 to 50)")
    public static final Key<Booking, Short> PERSONS = f.newKeyDefault((short)1, range((short)1, (short)50));
    @Comment("Total price")
    public static final Key<Booking, BigDecimal> PRICE = f.newKeyDefault(BigDecimal.ZERO, min(MANDATORY, BigDecimal.ZERO));
    @Comment("Currency of the total price")
    public static final Key<Booking, String> CURRENCY = f.newKeyDefault("USD", length(MANDATORY, 3, 3));
    @Comment("Creation datetime of booking.")
    public static final Key<Booking, LocalDateTime> CREATION_DATE = f.newKey(mandatory(LocalDateTime.class));

    static {
        f.lock();
    }

    // --- Generated Getters / Setters powered by: UjoCodeGenerator-1.1.2.nbm ---

    /** The Primary Key */
    public Long getId() {
        return ID.of(this);
    }

    /** The Primary Key */
    public void setId(Long id) {
        ID.setValue(this, id);
    }

    /** Relation to hotel */
    public Hotel getHotel() {
        return HOTEL.of(this);
    }

    /** Relation to hotel */
    public void setHotel(Hotel hotel) {
        HOTEL.setValue(this, hotel);
    }

    /** Relation to customer */
    public Customer getCustomer() {
        return CUSTOMER.of(this);
    }

    /** Relation to customer */
    public void setCustomer(Customer customer) {
        CUSTOMER.setValue(this, customer);
    }

    /** Date from */
    public LocalDate getDateFrom() {
        return DATE_FROM.of(this);
    }

    /** Date from */
    public void setDateFrom(LocalDate dateFrom) {
        DATE_FROM.setValue(this, dateFrom);
    }

    /** Number of nights */
    public Short getNights() {
        return NIGHTS.of(this);
    }

    /** Number of nights */
    public void setNights(Short nights) {
        NIGHTS.setValue(this, nights);
    }

    /** Number of persons (limit from 1 to 50) */
    public Short getPersons() {
        return PERSONS.of(this);
    }

    /** Number of persons (limit from 1 to 50) */
    public void setPersons(Short persons) {
        PERSONS.setValue(this, persons);
    }

    /** Total price */
    public BigDecimal getPrice() {
        return PRICE.of(this);
    }

    /** Total price */
    public void setPrice(BigDecimal price) {
        PRICE.setValue(this, price);
    }

    /** Currency of the total price */
    public String getCurrency() {
        return CURRENCY.of(this);
    }

    /** Currency of the total price */
    public void setCurrency(String currency) {
        CURRENCY.setValue(this, currency);
    }

    /** Creation datetime of booking. */
    public LocalDateTime getCreationDate() {
        return CREATION_DATE.of(this);
    }

    /** Creation datetime of booking. */
    public void setCreationDate(LocalDateTime creationDate) {
        CREATION_DATE.setValue(this, creationDate);
    }

}
