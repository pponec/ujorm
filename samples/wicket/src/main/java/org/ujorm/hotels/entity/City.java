/*
 * It can be a generated class in a future.
 * License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */
package org.ujorm.hotels.entity;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import static org.ujorm.Validator.Build.*;

/** City with Country */
public class City extends OrmTable<City> {

    /** Index name */
    private static final String UNIQUE_CITY = "idx_unique_city";

    /** Factory */
    private static final KeyFactory<City> f = newFactory(City.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<City, Integer> ID = f.newKey();
    /** City name */
    @Comment("City name")
    @Column(uniqueIndex=UNIQUE_CITY)
    public static final Key<City, String> CITY = f.newKey(length(MANDATORY, 256));
    /** Two characters country code along the ISO 3166 */
    @Comment("Two characters country code along the ISO 3166")
    public static final Key<City, String> COUNTRY_CODE = f.newKey("COUNTRY", length(MANDATORY, 3));
    /** Country name will be located in the another entity in a real application */
    @Comment("Country name will be located in the another entity in a real application")
    public static final Key<City, String> COUNTRY_NAME = f.newKey(length(MANDATORY, 50));

    static {
        f.lock();
    }

    // --- Getters / Setters ---

    //<editor-fold defaultstate="collapsed" desc="Setters and Getters generated from NetBeans">

    /** The Primary Key */
    public Integer getId() {
        return ID.of(this);
    }

    /** The Primary Key */
    public void setId(Integer id) {
        City.ID.setValue(this, id);
    }

    /** City name */
    public String getCity() {
        return CITY.of(this);
    }

    /** City name */
    public void setCity(String city) {
        City.CITY.setValue(this, city);
    }

    /** Two characters country code along the ISO 3166 */
    public String getCountryCode() {
        return COUNTRY_CODE.of(this);
    }

    /** Two characters country code along the ISO 3166 */
    public void setCountryCode(String countryCode) {
        City.COUNTRY_CODE.setValue(this, countryCode);
    }

    /** Country name will be located in the another entity in a real application */
    public String getCountryName() {
        return COUNTRY_NAME.of(this);
    }

    /** Country name will be located in the another entity in a real application */
    public void setCountryName(String countryName) {
        City.COUNTRY_NAME.setValue(this, countryName);
    }

    //</editor-fold>



}
