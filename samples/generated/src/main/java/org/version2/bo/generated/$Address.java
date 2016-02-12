/* Generated objedt, do not modify it */

package org.version2.bo.generated;

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
 * $Address
 * @author Pavel Ponec
 */
public class $Address extends Address implements UjoMiddle<$Address> {

    /** Key factory */
    private static final KeyFactory<$Address> f = new KeyFactory<$Address>($Address.class, false);
    //
    public static final Key<$Address, Integer> ID = f.newKey();
    public static final Key<$Address, String> STREET = f.newKey();
    public static final Key<$Address, String> CITY = f.newKey();
    public static final Key<$Address, String> COUNTRY = f.newKey();

    static {
        f.lock();
    }

    @Nullable
    private final Address data;

    public $Address(@Nullable Address data) {
        this.data = data;
    }

    public $Address() {
        this(null);
    }

    @Nonnull
    public Address original() {
        return data != null ? data : this;
    }

    @Override
    public <VALUE> VALUE get(Key<? super $Address, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> $Address set(Key<? super $Address, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super $Address, VALUE> key) {
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
    public KeyList<$Address> readKeyList() {
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
                case 1: return data.getStreet();
                case 2: return data.getCity();
                case 3: return data.getCountry();
            }
        } else {
            switch (key.getIndex()) {
                case 0: return super.getId();
                case 1: return super.getStreet();
                case 2: return super.getCity();
                case 3: return super.getCountry();
            }
        }
        throw new UnsupportedKey(key);
    }

    @Override
    public void writeValue(Key<?, ?> key, Object value) {
          if (this.data != null) {
            switch (key.getIndex()) {
                case 0: data.setId((Integer) value); return;
                case 1: data.setStreet((String) value); return;
                case 2: data.setCity((String) value); return;
                case 3: data.setCountry((String) value); return;
            }
        } else {
            switch (key.getIndex()) {
                case 0: super.setId((Integer) value); return;
                case 1: super.setStreet((String) value); return;
                case 2: super.setCity((String) value); return;
                case 3: super.setCountry((String) value); return;
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
    public String getStreet() {
        return STREET.of(this);
    }

    @Override
    public void setStreet(String street) {
        STREET.setValue(this, street);
    }

    @Override
    public String getCity() {
        return CITY.of(this);
    }

    @Override
    public void setCity(String city) {
        CITY.setValue(this, city);
    }

    @Override
    public String getCountry() {
        return COUNTRY.of(this);
    }

    @Override
    public void setCountry(String country) {
        COUNTRY.setValue(this, country);
    }

}
