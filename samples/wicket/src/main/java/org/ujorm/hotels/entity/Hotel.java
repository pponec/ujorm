/* License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */
package org.ujorm.hotels.entity;

import java.math.BigDecimal;
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

/** Hotel */
@Table("demo_hotel")
public class Hotel extends OrmTable<Hotel> {
    /** Index name */
    private static final String INDEX_HOTEL_NAME="idx_hotel_name";

    /** Factory */
    private static final OrmKeyFactory<Hotel> f = newFactory(Hotel.class);

    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Hotel, Long> ID = f.newKey();
    @Comment("Name of the Hotel")
    @Column(index=INDEX_HOTEL_NAME)
    public static final Key<Hotel, String> NAME = f.newKey(length(MANDATORY, 2, 40));
    @Comment("Description of the hotel")
    public static final Key<Hotel, String> NOTE = f.newKey(length(256));
    @Comment("Relation to the City address")
    public static final Key<Hotel, City> CITY = f.newKey(mandatory(City.class));
    @Comment("Street of address")
    public static final Key<Hotel, String> STREET = f.newKey(length(MANDATORY, 128));
    @Comment("Phone")
    public static final Key<Hotel, String> PHONE = f.newKey(length(20));
    @Comment("Stars")
    public static final Key<Hotel, Float> STARS = f.newKey(min(MANDATORY, 0f));
    @Comment("URL to the HomePage")
    public static final Key<Hotel, String> HOME_PAGE = f.newKey(length(100));
    @Comment("Price per night in USD")
    public static final Key<Hotel, BigDecimal> PRICE = f.newKey(min(MANDATORY, BigDecimal.ZERO));
    @Comment("Currency of the price")
    public static final Key<Hotel, String> CURRENCY = f.newKeyDefault("USD", length(MANDATORY, 3, 3));
    @Comment("Hotel state (the true or null is required)")
    @Column(index=INDEX_HOTEL_NAME)
    public static final Key<Hotel, Boolean> ACTIVE = f.newKeyDefault(true, mandatory(Boolean.class));

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

    /** Name of the Hotel */
    public String getName() {
        return NAME.of(this);
    }

    /** Name of the Hotel */
    public void setName(String name) {
        NAME.setValue(this, name);
    }

    /** Description of the hotel */
    public String getNote() {
        return NOTE.of(this);
    }

    /** Description of the hotel */
    public void setNote(String note) {
        NOTE.setValue(this, note);
    }

    /** Relation to the City address */
    public City getCity() {
        return CITY.of(this);
    }

    /** Relation to the City address */
    public void setCity(City city) {
        CITY.setValue(this, city);
    }

    /** Street of address */
    public String getStreet() {
        return STREET.of(this);
    }

    /** Street of address */
    public void setStreet(String street) {
        STREET.setValue(this, street);
    }

    /** Phone */
    public String getPhone() {
        return PHONE.of(this);
    }

    /** Phone */
    public void setPhone(String phone) {
        PHONE.setValue(this, phone);
    }

    /** Stars */
    public Float getStars() {
        return STARS.of(this);
    }

    /** Stars */
    public void setStars(Float stars) {
        STARS.setValue(this, stars);
    }

    /** URL to the HomePage */
    public String getHomePage() {
        return HOME_PAGE.of(this);
    }

    /** URL to the HomePage */
    public void setHomePage(String homePage) {
        HOME_PAGE.setValue(this, homePage);
    }

    /** Price per night in USD */
    public BigDecimal getPrice() {
        return PRICE.of(this);
    }

    /** Price per night in USD */
    public void setPrice(BigDecimal price) {
        PRICE.setValue(this, price);
    }

    /** Currency of the price */
    public String getCurrency() {
        return CURRENCY.of(this);
    }

    /** Currency of the price */
    public void setCurrency(String currency) {
        CURRENCY.setValue(this, currency);
    }

    /** Hotel state (the true or null is required) */
    public Boolean getActive() {
        return ACTIVE.of(this);
    }

    /** Hotel state (the true or null is required) */
    public void setActive(Boolean active) {
        ACTIVE.setValue(this, active);
    }

}
