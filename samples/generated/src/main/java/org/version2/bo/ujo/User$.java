/* Generated objedt, do not modify it */
package org.version2.bo.ujo;

import java.util.Date;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.UjoMiddle;
import org.version2.bo.*;

/**
 * User$
 * @author Pavel Ponec
 */
public class User$ extends User implements UjoMiddle<User$> {

    private static final KeyFactory<User$> f = new KeyFactory<User$>(User$.class, false);
    //
    public static final Key<User$, Integer> ID = f.newKey();
    public static final Key<User$, String> LOGIN = f.newKey();
    public static final Key<User$, byte[]> PASSWORD = f.newKey();
    public static final Key<User$, Boolean> ENABLED = f.newKey();
    public static final Key<User$, String> FORENAME = f.newKey();
    public static final Key<User$, String> SURNAME = f.newKey();
    public static final Key<User$, Date> BIRTHDAY = f.newKey();
    public static final Key<User$, Float> HEIGHT = f.newKey();
    public static final Key<User$, Boolean> MALE = f.newKey();
    public static final Key<User$, Address> ADDRESS = f.newKey();

    private final User data;

    public User$(User data) {
        this.data = data;
    }

    public User$() {
        this(new User());
    }

    public User original() {
        return data;
    }

    @Override
    public <VALUE> VALUE get(Key<? super User$, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> Ujo set(Key<? super User$, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super User$, VALUE> key) {
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
    public KeyList<User$> readKeyList() {
        return f.getKeys();
    }

    @Override
    public <U extends Ujo> KeyList<U> readKeys() {
        return (KeyList<U>) readKeyList();
    }

    @Override
    public Object readValue(Key<?, ?> key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void writeValue(Key<?, ?> key, Object value) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key<?, ?> key, Object value) {
        return true;
    }

    // ---------- GETTERS AND SETTERS ----------

    @Override
    public Integer getId() {
        return ID.of(this);
    }

    @Override
    public void setId(Integer id) {
        ID.setValue(this, id);
    }

    @Override
    public String getLogin() {
        return LOGIN.of(this);
    }

    @Override
    public void setLogin(String login) {
        LOGIN.setValue(this, login);
    }

    @Override
    public byte[] getPassword() {
        return PASSWORD.of(this);
    }

    @Override
    public void setPassword(byte[] password) {
        PASSWORD.setValue(this, password);
    }

    @Override
    public Boolean getEnabled() {
        return ENABLED.of(this);
    }

    @Override
    public void setEnabled(Boolean enabled) {
        ENABLED.setValue(this, enabled);
    }

    @Override
    public String getForename() {
        return FORENAME.of(this);
    }

    @Override
    public void setForename(String forename) {
        FORENAME.setValue(this, forename);
    }

    @Override
    public String getSurname() {
        return SURNAME.of(this);
    }

    @Override
    public void setSurname(String surname) {
        SURNAME.setValue(this, surname);
    }

    @Override
    public Date getBirthday() {
        return BIRTHDAY.of(this);
    }

    @Override
    public void setBirthday(Date birthday) {
        BIRTHDAY.setValue(this, birthday);
    }

    @Override
    public Float getHeight() {
        return HEIGHT.of(this);
    }

    @Override
    public void setHeight(Float height) {
        HEIGHT.setValue(this, height);
    }

    @Override
    public Boolean getMale() {
        return MALE.of(this);
    }

    @Override
    public void setMale(Boolean male) {
        MALE.setValue(this, male);
    }

    @Override
    public Address getAddress() {
        return ADDRESS.of(this);
    }

    @Override
    public void setAddress(Address address) {
        ADDRESS.setValue(this, address);
    }
    
}
