package org.ujorm2.doman;

import java.time.LocalDateTime;

/**
 * Domain object type of POJO
 * @author Pavel Ponec
 */
public class User /* TODO: extends Anonymous */ {

    /** Unique key */
    private Integer id;
    private Short pin;
    private String firstName;
    private String sureName;
    private LocalDateTime born;
    private User parent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Short getPin() {
        return pin;
    }

    public void setPin(Short pin) {
        this.pin = pin;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSureName() {
        return sureName;
    }

    public void setSureName(String sureName) {
        this.sureName = sureName;
    }

    public LocalDateTime getBorn() {
        return born;
    }

    public void setBorn(LocalDateTime born) {
        this.born = born;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }




}
