/* Powered by the Ujorm, don't modify it */

package org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UnsupportedKey;
import org.ujorm.orm.annot.Column;
import org.ujorm.extensions.UjoMiddle;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.InternalUjo;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.Session;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.*;
import static org.ujorm.orm.InternalUjo.CONVERTER;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, how the Keys are created by the KeyFactory.
 * @hidden
 */
public final class $Customer extends Customer implements UjoMiddle<$Customer>, ExtendedOrmUjo {
    private static final OrmKeyFactory<$Customer> f = new OrmKeyFactory($Customer.class, true);

    /** Unique key */
    @Column(pk = true)
    public static final Key<$Customer, Long> ID = f.newKey();
    /** Personal Number */
    public static final Key<$Customer, Integer> PIN = f.newKey();
    /** Firstname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    public static final Key<$Customer, String> FIRSTNAME = f.newKey();
    /** Surname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    public static final Key<$Customer, String> SURNAME = f.newKey();
    /** Date of creation */
    public static final Key<$Customer, Date> CREATED = f.newKey();
    /** A parent (father or mother) with an alias called {@code "parent"} */
    public static final Key<$Customer, $Customer> PARENT = f.newKeyAlias("customerAlias");

    // Lock the Key factory
    static { f.lock(); }

    private final InternalUjo internal = new InternalUjo();

    /** Basic data */
    private final Customer data;

    /** Constructor */
    public $Customer(Customer pojo) {
        data = pojo;
    }

    /** Constructor */
    public $Customer() {
        this(null);
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key<?, ?> key, Object value) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends Ujo> KeyList<U> readKeys() {
        return (KeyList<U>) readKeyList();
    }

    @Override
    public KeyList<$Customer> readKeyList() {
        return f.getKeys();
    }

    /** Read an ORM session where the session is an transient key. */
    public Session readSession() {
        return internal.readSession();
    }

    /** Write an ORM session. */
    public void writeSession(Session session) {
        internal.writeSession(session);
    }

    /**
     * Returns keys of changed values in a time when any <strong>session</strong> is assigned.
     * The method is used by a SQL UPDATE statement to update assigned values only.
     * Implementation tip: create a new key type of {@link Set<Key>}
     * and in the method writeValue assign the current Key always.
     * @param clear True value clears all the key changes.
     * @return Key array of the modified values.
     */
    public Key[] readChangedProperties(boolean clear) {
         return internal.readChangedProperties(clear);
    }

    /** Get an original foreign key for an internal use only.
     * The {@code non null} value means the undefined object properties of the current object.
     * @return An original foreign key can be {@code nullable} */
    public ForeignKey readInternalFK() {
        return internal.readInternalFK();
    }

    /** A method to a foreign key for an internal use only.
     * @param fk New key to assign can be {@code null} */
    public void writeInternalFK(ForeignKey fk) {
        internal.writeInternalFK(fk);
    }

    @Override
    public ForeignKey readFK(Key key) throws IllegalStateException {
        return internal.readFK(this, readValue(key), key);
    }

    @Override
    public <VALUE> VALUE get(Key<? super $Customer, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> Ujo set(Key<? super $Customer, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super $Customer, VALUE> key) {
         return key.getList(this);
    }

    @Override
    public String getText(Key key) {
        return UjoManager.getInstance().getText(this, key, null);
    }

    @Override
    public void setText(Key key, String value) {
        UjoManager.getInstance().setText(this, key, value, null, null);
    }

    @Override
    public Object readValue(Key<?, ?> key) {
        if (this.data != null) {
            switch (key.getIndex()) {
                case 0: return data.getId();
                case 1: return data.getPin();
                case 2: return data.getFirstname();
                case 3: return data.getSurname();
                case 4: return data.getCreated();
                case 5: return data.getParent();
            }
        } else {
            switch (key.getIndex()) {
                case 0: return super.getId();
                case 1: return super.getPin();
                case 2: return super.getFirstname();
                case 3: return super.getSurname();
                case 4: return super.getCreated();
                case 5: return super.getParent();
            }
        }
        throw new UnsupportedKey(key);
    }

    @Override
    public void writeValue(Key<?, ?> key, Object value) {
       if (this.data != null) {
            switch (key.getIndex()) {
                case 0: data.setId((Long) value); return;
                case 1: data.setPin((Integer) value); return;
                case 2: data.setFirstname((String) value); return;
                case 3: data.setSurname((String) value); return;
                case 4: data.setCreated((Date) value); return;
                case 5: data.setParent((Customer) value); return;
            }
        } else {
            switch (key.getIndex()) {
                case 0: super.setId((Long) value); return;
                case 1: super.setPin((Integer) value); return;
                case 2: super.setFirstname((String) value); return;
                case 3: super.setSurname((String) value); return;
                case 4: super.setCreated((Date) value); return;
                case 5: super.setParent((Customer) value); return;
            }
        }
        throw new UnsupportedKey(key);

    }

    // --- Getters and Setters ---

    /** Unique key */
    @Override
    public Long getId() {
        return ID.of(this);
    }

    /** Unique key */
    @Override
    public void setId(Long id) {
        ID.setValue(this, id);
    }

    /** Personal Number */
    @Override
    public Integer getPin() {
        return PIN.of(this);
    }

    /** Personal Number */
    @Override
    public void setPin(Integer pin) {
        PIN.setValue(this, pin);
    }

    /** Firstname */
    @Override
    public String getFirstname() {
        return FIRSTNAME.of(this);
    }

    /** Firstname */
    @Override
    public void setFirstname(String firstname) {
        FIRSTNAME.setValue(this, firstname);
    }

    /** Surname */
    @Override
    public String getSurname() {
        return SURNAME.of(this);
    }

    /** Surname */
    @Override
    public void setSurname(String surname) {
        SURNAME.setValue(this, surname);
    }

    /** Date of creation */
    @Override
    public Date getCreated() {
        return CREATED.of(this);
    }

    /** Date of creation */
    @Override
    public void setCreated(Date created) {
        CREATED.setValue(this, created);
    }

    /** A parent (father or mother) with an alias called {@code "parent"} */
    @Override
    public $Customer getParent() {
        return PARENT.of(this);
    }

    /** A parent (father or mother) with an alias called {@code "parent"} */
    @Override
    public void setParent(Customer parent) {
        PARENT.setValue(this, ($Customer) CONVERTER.marshal(parent));
    }

}
