/* License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */
package org.ujorm.hotels.entity;

import org.ujorm.Key;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;
import static org.ujorm.validator.impl.ValidatorFactory.MANDATORY;
import static org.ujorm.validator.impl.ValidatorFactory.length;
import static org.ujorm.validator.impl.ValidatorFactory.range;

/** City with Country */
@Table("demo_city")
public class City extends OrmTable<City> {

    /** Unique index name */
    private static final String UNIQUE_CITY = "idx_unique_city";

    /** Factory */
    private static final OrmKeyFactory<City> f = newFactory(City.class);

    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<City, Integer> ID = f.newKey();
    @Comment("City name")
    @Column(uniqueIndex=UNIQUE_CITY)
    public static final Key<City, String> NAME = f.newKey(length(MANDATORY, 40));
    @Comment("Two characters country code along the ISO 3166")
    public static final Key<City, String> COUNTRY = f.newKey("COUNTRY", length(MANDATORY, 3));
    @Comment("Country name will be located in the another entity in a real application")
    public static final Key<City, String> COUNTRY_NAME = f.newKey(length(MANDATORY, 40));
    @Comment("A geographic coordinate for north-south position on the Earth")
    public static final Key<City, Float> LATITUDE = f.newKey(range(MANDATORY, -90f, 90f));
    @Comment("A geographic coordinate for east-west position on the Earth")
    public static final Key<City, Float> LONGITUDE = f.newKey(range(MANDATORY, -180f, 180f));

    static {
        f.lock();
    }

    // --- Generated Getters / Setters powered by: UjoCodeGenerator-1.1.2.nbm ---

    /** The Primary Key */
    public Integer getId() {
        return ID.of(this);
    }

    /** The Primary Key */
    public void setId(Integer id) {
        ID.setValue(this, id);
    }

    /** City name */
    public String getName() {
        return NAME.of(this);
    }

    /** City name */
    public void setName(String name) {
        NAME.setValue(this, name);
    }

    /** Two characters country code along the ISO 3166 */
    public String getCountry() {
        return COUNTRY.of(this);
    }

    /** Two characters country code along the ISO 3166 */
    public void setCountry(String country) {
        COUNTRY.setValue(this, country);
    }

    /** Country name will be located in the another entity in a real application */
    public String getCountryName() {
        return COUNTRY_NAME.of(this);
    }

    /** Country name will be located in the another entity in a real application */
    public void setCountryName(String countryName) {
        COUNTRY_NAME.setValue(this, countryName);
    }

    /** A geographic coordinate for north-south position on the Earth */
    public Float getLatitude() {
        return LATITUDE.of(this);
    }

    /** A geographic coordinate for north-south position on the Earth */
    public void setLatitude(Float latitude) {
        LATITUDE.setValue(this, latitude);
    }

    /** A geographic coordinate for east-west position on the Earth */
    public Float getLongitude() {
        return LONGITUDE.of(this);
    }

    /** A geographic coordinate for east-west position on the Earth */
    public void setLongitude(Float longitude) {
        LONGITUDE.setValue(this, longitude);
    }

}
