package org.ujorm2.doman;

import java.time.LocalDateTime;

/**
 *
 * @author Pavel Ponec
 */
public class User {
    
    /** Unique key */
    private Integer id;
    private Short pin;
    private String firstName;
    private String sureName;
    private LocalDateTime created;
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

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime crated) {
        this.created = crated;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    
    
    
}
