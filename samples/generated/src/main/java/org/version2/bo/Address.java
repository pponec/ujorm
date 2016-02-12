package org.version2.bo;

import org.version2.tools.MyConverter;
import org.version2.tools.UjoConverter;

/**
 * Address
 * @author Pavel Ponec
 */
@UjoConverter(MyConverter.class)
public class Address {

    /** ID */
    private Integer id;
    /** Street */
    private String street;
    /** City */
    private String city;
    /** Country */
    private String country;

    /**
     * ID
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * ID
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Street
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * Street
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * City
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * City
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Country
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Country
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }



}
