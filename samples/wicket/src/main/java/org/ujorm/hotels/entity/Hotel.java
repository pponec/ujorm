/*
 * It can be a generated class in a future.
 * License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */

package org.ujorm.hotels.entity;

import java.math.BigDecimal;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import static org.ujorm.Validator.Build.*;

/** Hotel */
public class Hotel extends OrmTable<Hotel> {

    private static final String INDEX_HOTEL_NAME="idx_hotel_name";

    /** Factory */
    private static final KeyFactory<Hotel> f = newFactory(Hotel.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Hotel, Long> ID = f.newKey();
    /** Description of the Company */
    @Comment("Name of the Hotel")
    @Column(index=INDEX_HOTEL_NAME)
    public static final Key<Hotel, String> NAME = f.newKey(length(MANDATORY, 2, 40));
    /** Description of the hotel */
    @Comment("Description of the hotel")
    public static final Key<Hotel, String> NOTE = f.newKey(length(256));
    /** Relation to the City */
    @Comment("Relation to the City address")
    public static final Key<Hotel, City> CITY = f.newKey(mandatory(City.class));
    /** Street of address */
    @Comment("Street of address")
    public static final Key<Hotel, String> STREET = f.newKey(length(MANDATORY, 128));
    /** Description of the Company */
    @Comment("Phone")
    public static final Key<Hotel, String> PHONE = f.newKey(length(20));
    /** Stars */
    @Comment("Stars")
    public static final Key<Hotel, Float> STARS = f.newKey(min(MANDATORY, 0f));
    /** URL to the HomePage */
    @Comment("URL to the HomePage")
    public static final Key<Hotel, String> HOME_PAGE = f.newKey(length(100));
    /** Price per night in USD */
    @Comment("Price per night in USD")
    public static final Key<Hotel, BigDecimal> PRICE = f.newKey(min(MANDATORY, BigDecimal.ZERO));
    /** Currency of the price */
    @Comment("Currency of the price")
    public static final Key<Hotel, String> CURRENCY = f.newKeyDefault("USD", length(MANDATORY, 3, 3));
    /** Hotel state, default is ACTIVE (the true or null is required) */
    @Comment("Hotel state (the true or null is required)")
    @Column(index=INDEX_HOTEL_NAME)
    public static final Key<Hotel, Boolean> ACTIVE = f.newKeyDefault(true, mandatory());

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
        Hotel.ID.setValue(this, id);
    }

    /** Description of the Company */
    public String getName() {
        return NAME.of(this);
    }

    /** Description of the Company */
    public void setName(String name) {
        Hotel.NAME.setValue(this, name);
    }

    /** Description of the hotel */
    public String getNote() {
        return NOTE.of(this);
    }

    /** Description of the hotel */
    public void setNote(String note) {
        Hotel.NOTE.setValue(this, note);
    }

    /** Relation to the City */
    public City getCity() {
        return CITY.of(this);
    }

    /** Relation to the City */
    public void setCity(City city) {
        Hotel.CITY.setValue(this, city);
    }

    /** Street of address */
    public String getStreet() {
        return STREET.of(this);
    }

    /** Street of address */
    public void setStreet(String street) {
        Hotel.STREET.setValue(this, street);
    }

    /** Description of the Company */
    public String getPhone() {
        return PHONE.of(this);
    }

    /** Description of the Company */
    public void setPhone(String phone) {
        Hotel.PHONE.setValue(this, phone);
    }

    /** Stars */
    public Float getStars() {
        return STARS.of(this);
    }

    /** Stars */
    public void setStars(Float stars) {
        Hotel.STARS.setValue(this, stars);
    }

    /** URL to the HomePage */
    public String getHomePage() {
        return HOME_PAGE.of(this);
    }

    /** URL to the HomePage */
    public void setHomePage(String homePage) {
        Hotel.HOME_PAGE.setValue(this, homePage);
    }

    /** Price per night in USD */
    public BigDecimal getPrice() {
        return PRICE.of(this);
    }

    /** Price per night in USD */
    public void setPrice(BigDecimal price) {
        Hotel.PRICE.setValue(this, price);
    }

    /** Currency of the price */
    public String getCurrency() {
        return CURRENCY.of(this);
    }

    /** Currency of the price */
    public void setCurrency(String currency) {
        Hotel.CURRENCY.setValue(this, currency);
    }

    /** Hotel state, default is ACTIVE (the true or null is required) */
    public Boolean getActive() {
        return ACTIVE.of(this);
    }

    /** Hotel state, default is ACTIVE (the true or null is required) */
    public void setActive(Boolean active) {
        Hotel.ACTIVE.setValue(this, active);
    }

    //</editor-fold>

}
