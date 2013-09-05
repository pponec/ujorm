/*
 * It can be a generated class in a future.
 * License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */
package org.ujorm.hotels.entity;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.Transient;
import org.ujorm.hotels.entity.enums.TitleEnum;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.wicket.component.form.FieldProvider;
import static org.ujorm.Validator.Build.*;

/** Common User */
public class Customer extends OrmTable<Customer> {

    /** Index name */
    private static final String UNIQUE_LOGIN = "idx_unique_login";

    /** Factory */
    private static final KeyFactory<Customer> f = newFactory(Customer.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Customer, Integer> ID = f.newKey();
    /** Unique login */
    @Comment("Unique login")
    @Column(uniqueIndex=UNIQUE_LOGIN)
    public static final Key<Customer, String> LOGIN = f.newKey(length(MANDATORY, 3, 6));
    /** A form field only where the {@code null} value means: no password chage */
    @Transient
    public static final Key<Customer, String> PASSWORD = f.newKey(FieldProvider.PASSWORD_KEY_NAME, length(NULLABLE, 3, 100));
    /** Password hash */
    @Comment("Password hash")
    public static final Key<Customer, Long> PASSWORD_HASH = f.newKey(notNull(Long.class));
    /** Title */
    @Comment("Title")
    public static final Key<Customer, TitleEnum> TITLE = f.newKey();
    /** Firstname */
    @Comment("Firstname")
    public static final Key<Customer, String> FIRSTNAME = f.newKey(length(MANDATORY, 2, 60));
    /** Firstname */
    @Comment("Lastname")
    public static final Key<Customer, String> SURENAME = f.newKey(length(MANDATORY, 2, 60));
    /** Email */
    @Comment("Email")
    public static final Key<Customer, String> EMAIL = f.newKey(email(MANDATORY));
    /** Administrator role sign */
    @Comment("Administrator role sign")
    public static final Key<Customer, Boolean> ADMIN = f.newKeyDefault(false);
    /** Customer state (the true or null values are required) */
    @Comment("Customer is allowed to login (the true or null values are required)")
    @Column(uniqueIndex=UNIQUE_LOGIN)
    public static final Key<Customer, Boolean> ACTIVE = f.newKeyDefault(true);

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
        Customer.ID.setValue(this, id);
    }

    /** Unique login */
    public String getLogin() {
        return LOGIN.of(this);
    }

    /** Unique login */
    public void setLogin(String login) {
        Customer.LOGIN.setValue(this, login);
    }

    /** A form field only where the {@code null} value means: no password chage */
    public String getPassword() {
        return PASSWORD.of(this);
    }

    /** A form field only where the {@code null} value means: no password chage */
    public void setPassword(String password) {
        Customer.PASSWORD.setValue(this, password);
    }

    /** Password hash */
    public Long getPasswordHash() {
        return PASSWORD_HASH.of(this);
    }

    /** Password hash */
    public void setPasswordHash(Long passwordHash) {
        Customer.PASSWORD_HASH.setValue(this, passwordHash);
    }

    /** Title */
    public TitleEnum getTitle() {
        return TITLE.of(this);
    }

    /** Title */
    public void setTitle(TitleEnum title) {
        Customer.TITLE.setValue(this, title);
    }

    /** Firstname */
    public String getFirstname() {
        return FIRSTNAME.of(this);
    }

    /** Firstname */
    public void setFirstname(String firstname) {
        Customer.FIRSTNAME.setValue(this, firstname);
    }

    /** Firstname */
    public String getSurename() {
        return SURENAME.of(this);
    }

    /** Firstname */
    public void setSurename(String surename) {
        Customer.SURENAME.setValue(this, surename);
    }

    /** Email */
    public String getEmail() {
        return EMAIL.of(this);
    }

    /** Email */
    public void setEmail(String email) {
        Customer.EMAIL.setValue(this, email);
    }

    /** Administrator role sign */
    public Boolean getAdmin() {
        return ADMIN.of(this);
    }

    /** Administrator role sign */
    public void setAdmin(Boolean admin) {
        Customer.ADMIN.setValue(this, admin);
    }

    /** Customer state (the true or null values are required) */
    public Boolean getActive() {
        return ACTIVE.of(this);
    }

    /** Customer state (the true or null values are required) */
    public void setActive(Boolean active) {
        Customer.ACTIVE.setValue(this, active);
    }

    //</editor-fold>

}
