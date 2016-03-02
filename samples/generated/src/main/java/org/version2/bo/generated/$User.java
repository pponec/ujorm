/* Generated objedt, do not modify it */

package org.version2.bo.generated;

import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.UjoMiddle;
import org.version2.bo.*;
import org.version2.tools.UnsupportedKey;

/**
 * $User
 * @author Pavel Ponec
 */
public class $User extends User implements UjoMiddle<$User> {

    private static final KeyFactory<$User> f = new KeyFactory<$User>($User.class, false);
    //
    public static final Key<$User, Integer> ID = f.newKey();
    public static final Key<$User, String> LOGIN = f.newKey();
    public static final Key<$User, byte[]> PASSWORD = f.newKey();
    public static final Key<$User, Boolean> ENABLED = f.newKey();
    public static final Key<$User, String> FORENAME = f.newKey();
    public static final Key<$User, String> SURNAME = f.newKey();
    public static final Key<$User, Date> BIRTHDAY = f.newKey();
    public static final Key<$User, Float> HEIGHT = f.newKey();
    public static final Key<$User, Boolean> MALE = f.newKey();
    public static final Key<$User, $Address> ADDRESS = f.newKey();

    static {
        f.lock(); // Lock the factory
    }

    @Nullable
    private final User data;

    public $User(@Nullable User data) {
        this.data = data;
    }

    public $User() {
        this(null);
    }

    @Nonnull
    public User original() {
        return data != null ? data : this;
    }

    @Override
    public <VALUE> VALUE get(Key<? super $User, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> Ujo set(Key<? super $User, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super $User, VALUE> key) {
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
    public KeyList<$User> readKeyList() {
        return f.getKeys();
    }

    @Override
    public <U extends Ujo> KeyList<U> readKeys() {
        return (KeyList<U>) readKeyList();
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key<?, ?> key, Object value) {
        return true;
    }

    @Override
    public Object readValue(Key<?, ?> key) {
        if (this.data != null) {
            switch (key.getIndex()) {
                case 0: return data.getId();
                case 1: return data.getLogin();
                case 2: return data.getPassword();
                case 3: return data.getEnabled();
                case 4: return data.getForename();
                case 5: return data.getSurname();
                case 6: return data.getBirthday();
                case 7: return data.getHeight();
                case 8: return data.getMale();
                case 9: return data.getAddress();
            }
        } else {
            switch (key.getIndex()) {
                case 0: return super.getId();
                case 1: return super.getLogin();
                case 2: return super.getPassword();
                case 3: return super.getEnabled();
                case 4: return super.getForename();
                case 5: return super.getSurname();
                case 6: return super.getBirthday();
                case 7: return super.getHeight();
                case 8: return super.getMale();
                case 9: return super.getAddress();
            }
        }
        throw new UnsupportedKey(key);
    }

    @Override
    public void writeValue(Key<?, ?> key, Object value) {
          if (this.data != null) {
            switch (key.getIndex()) {
                case 0: data.setId((Integer) value); return;
                case 1: data.setLogin((String) value); return;
                case 2: data.setPassword((byte[]) value); return;
                case 3: data.setEnabled((Boolean) value); return;
                case 4: data.setForename((String) value); return;
                case 5: data.setSurname((String) value); return;
                case 6: data.setBirthday((Date) value); return;
                case 7: data.setHeight((Float) value); return;
                case 8: data.setMale((Boolean) value); return;
                case 9: data.setAddress((Address) value); return;
            }
        } else {
            switch (key.getIndex()) {
                case 0: super.setId((Integer) value); return;
                case 1: super.setLogin((String) value); return;
                case 2: super.setPassword((byte[]) value); return;
                case 3: super.setEnabled((Boolean) value); return;
                case 4: super.setForename((String) value); return;
                case 5: super.setSurname((String) value); return;
                case 6: super.setBirthday((Date) value); return;
                case 7: super.setHeight((Float) value); return;
                case 8: super.setMale((Boolean) value); return;
                case 9: super.setAddress((Address) value); return;
            }
        }
        throw new UnsupportedKey(key);
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
    public $Address getAddress() {
        return ADDRESS.of(this);
    }

    @Override
    public void setAddress(Address address) {
        final $Address localAddress
                = address instanceof $Address
                ? ($Address) address
                : new $Address(address);
        setAddress(localAddress);
    }

    // @Override
    public void setAddress($Address address) {
        ADDRESS.setValue(this, address);
    }

}
