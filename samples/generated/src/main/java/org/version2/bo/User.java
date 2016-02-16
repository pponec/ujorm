package org.version2.bo;

import java.util.Date;

/**
 * User
 * @author Pavel Ponec
 */
public class User extends Account {

    /** First name */
    private String forename;
    /** Surname */
    private String surname;
    /** Birthday */
    private Date birthday;
    /** Height */
    private Float height;
    /** Male */
    private Boolean male;
    /** Address */
    private Address address;

    /**
     * First name
     * @return the forename
     */
    public String getForename() {
        return forename;
    }

    /**
     * First name
     * @param forename the forename to set
     */
    public void setForename(String forename) {
        this.forename = forename;
    }

    /**
     * Surname
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Surname
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Birthday
     * @return the birthday
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * Birthday
     * @param birthday the birthday to set
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * Height
     * @return the height
     */
    public Float getHeight() {
        return height;
    }

    /**
     * Height
     * @param height the height to set
     */
    public void setHeight(Float height) {
        this.height = height;
    }

    /**
     * Male
     * @return the male
     */
    public Boolean getMale() {
        return male;
    }

    /**
     * Male
     * @param male the male to set
     */
    public void setMale(Boolean male) {
        this.male = male;
    }

    /**
     * Address
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Address
     * @param address the address to set
     */
    public void setAddress(Address address) {
        this.address = address;
    }


}
