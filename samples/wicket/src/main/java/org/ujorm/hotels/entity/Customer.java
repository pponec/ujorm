/* License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */
package org.ujorm.hotels.entity;

import org.ujorm.Key;
import org.ujorm.core.annot.Transient;
import org.ujorm.hotels.entity.enums.TitleEnum;
import org.ujorm.implementation.orm.OrmTableLockable;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;
import org.ujorm.wicket.component.form.FieldProvider;
import static org.ujorm.validator.impl.ValidatorFactory.MANDATORY;
import static org.ujorm.validator.impl.ValidatorFactory.NULLABLE;
import static org.ujorm.validator.impl.ValidatorFactory.email;
import static org.ujorm.validator.impl.ValidatorFactory.length;
import static org.ujorm.validator.impl.ValidatorFactory.notNull;

/** Common User */
@Table("demo_customer")
public class Customer extends OrmTableLockable<Customer> {

    /** Unique index name */
    private static final String UNIQUE_LOGIN = "idx_unique_login";

    /** Factory */
    private static final OrmKeyFactory<Customer> f = newFactory(Customer.class);

    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Customer, Integer> ID = f.newKey();
    @Comment("Unique login")
    @Column(uniqueIndex=UNIQUE_LOGIN)
    public static final Key<Customer, String> LOGIN = f.newKey(length(MANDATORY, 4, 30));
    @Comment("A form field only where the {@code null} value means: no password chage")
    @Transient
    public static final Key<Customer, String> PASSWORD = f.newKey(FieldProvider.PASSWORD_KEY_NAME, length(NULLABLE, 4, 100));
    @Comment("Password hash")
    public static final Key<Customer, Long> PASSWORD_HASH = f.newKey(notNull(Long.class));
    @Comment("Title")
    public static final Key<Customer, TitleEnum> TITLE = f.newKey();
    @Comment("First name")
    public static final Key<Customer, String> FIRSTNAME = f.newKey(length(MANDATORY, 2, 60));
    @Comment("Surname")
    public static final Key<Customer, String> SURNAME = f.newKey(length(MANDATORY, 2, 60));
    @Comment("Email")
    public static final Key<Customer, String> EMAIL = f.newKey(email(MANDATORY));
    @Comment("Administrator role sign")
    public static final Key<Customer, Boolean> ADMIN = f.newKeyDefault(false);
    @Comment("Customer is allowed to login (the true or null values are required)")
    @Column(uniqueIndex=UNIQUE_LOGIN)
    public static final Key<Customer, Boolean> ACTIVE = f.newKeyDefault(true);

    static {
        f.lock();
    }

    /** Returns a full name of the Customer */
    public String getFullName() {
        final StringBuilder result = new StringBuilder(32);
        result.append(FIRSTNAME.of(this));
        result.append(" ");
        result.append(SURNAME.of(this));
        return result.toString();
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

    /** Unique login */
    public String getLogin() {
        return LOGIN.of(this);
    }

    /** Unique login */
    public void setLogin(String login) {
        LOGIN.setValue(this, login);
    }

    /** A form field only where the {@code null} value means: no password chage */
    public String getPassword() {
        return PASSWORD.of(this);
    }

    /** A form field only where the {@code null} value means: no password chage */
    public void setPassword(String password) {
        PASSWORD.setValue(this, password);
    }

    /** Password hash */
    public Long getPasswordHash() {
        return PASSWORD_HASH.of(this);
    }

    /** Password hash */
    public void setPasswordHash(Long passwordHash) {
        PASSWORD_HASH.setValue(this, passwordHash);
    }

    /** Title */
    public TitleEnum getTitle() {
        return TITLE.of(this);
    }

    /** Title */
    public void setTitle(TitleEnum title) {
        TITLE.setValue(this, title);
    }

    /** First name */
    public String getFirstname() {
        return FIRSTNAME.of(this);
    }

    /** First name */
    public void setFirstname(String firstname) {
        FIRSTNAME.setValue(this, firstname);
    }

    /** Surname */
    public String getSurname() {
        return SURNAME.of(this);
    }

    /** Surname */
    public void setSurname(String surname) {
        SURNAME.setValue(this, surname);
    }

    /** Email */
    public String getEmail() {
        return EMAIL.of(this);
    }

    /** Email */
    public void setEmail(String email) {
        EMAIL.setValue(this, email);
    }

    /** Administrator role sign */
    public Boolean getAdmin() {
        return ADMIN.of(this);
    }

    /** Administrator role sign */
    public void setAdmin(Boolean admin) {
        ADMIN.setValue(this, admin);
    }

    /** Customer is allowed to login (the true or null values are required) */
    public Boolean getActive() {
        return ACTIVE.of(this);
    }

    /** Customer is allowed to login (the true or null values are required) */
    public void setActive(Boolean active) {
        ACTIVE.setValue(this, active);
    }

}
