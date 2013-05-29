/*
 * It can be a generated class in a future.
 * License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */

package org.ujorm.hotels.domains;

import java.math.BigDecimal;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import static org.ujorm.Validator.Build.*;

/** Hotel */
public class Hotel extends OrmTable<Hotel> {

    private static final String UNIQUE_HOTEL_NAME="idx_hotel_name";
    
    /** Factory */
    private static final KeyFactory<Hotel> f = newFactory(Hotel.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Hotel, Long> ID = f.newKey();
    /** Description of the Company */
    @Comment("Name of the Hotel")
    @Column(index=UNIQUE_HOTEL_NAME)
    public static final Key<Hotel, String> NAME = f.newKey(length(MANDATORY, 3, 80));
    /** Description of the hotel */
    @Comment("Description of the hotel")
    public static final Key<Hotel, String> NOTE = f.newKey(length(256));
    /** Description of the Company */
    /** City of address */
    @Comment("City of address")
    public static final Key<Hotel, String> CITY = f.newKey(length(256));
    /** Street of address */
    @Comment("Street of address")
    public static final Key<Hotel, String> STREET = f.newKey(length(256));
    /** Description of the Company */
    @Comment("Phone")
    public static final Key<Hotel, String> PHONE = f.newKey(length(16));
    /** Stars */
    @Comment("Stars")
    public static final Key<Hotel, Integer> STARS = f.newKey(mandatory());
    /** URL to the HomePage */
    @Comment("URL to the HomePage")
    public static final Key<Hotel, String> HOME_PAGE = f.newKey(length(256));
    /** Price per night in EUR */
    @Comment("Price per night in EUR")
    public static final Key<Hotel, BigDecimal> PRICE = f.newKey(min(MANDATORY, BigDecimal.ZERO));
    /** Hotel state, default is ACTIVE (the true or null is required) */
    @Comment("Hotel state (the true or null is required)")
    @Column(index=UNIQUE_HOTEL_NAME)
    public static final Key<Hotel, Boolean> ACTIVE = f.newKey(required(true));
    
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

    /** Hotel state, default is ACTIVE (the true or null is required) */
    public Boolean getActive() {
        return ACTIVE.of(this);
    }

    /** Hotel state, default is ACTIVE (the true or null is required) */
    public void setActive(Boolean active) {
        Hotel.ACTIVE.setValue(this, active);
    }

    /** Description of the Company */
    public String getName() {
        return NAME.of(this);
    }

    /** Description of the Company */
    public void setName(String name) {
        Hotel.NAME.setValue(this, name);
    }

    /** Description of the Company */
    public String getCity() {
        return CITY.of(this);
    }

    /** Description of the Company */
    public void setCity(String city) {
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
    public Integer getStars() {
        return STARS.of(this);
    }

    /** Stars */
    public void setStars(Integer stars) {
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

    /** Price per night in EUR */
    public BigDecimal getPrice() {
        return PRICE.of(this);
    }

    /** Price per night in EUR */
    public void setPrice(BigDecimal price) {
        Hotel.PRICE.setValue(this, price);
    }

    public String getNote() {
        return NOTE.of(this);
    }

    public void setNote(String note) {
        Hotel.NOTE.setValue(this, note);
    }
    
    //</editor-fold>

}
