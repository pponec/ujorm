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
 * $Account
 * @author Pavel Ponec
 */
public class $Account extends Account implements UjoMiddle<$Account> {

    private static final KeyFactory<$Account> f = new KeyFactory<$Account>($Account.class, false);
    //
    public static final Key<$Account, Integer> ID = f.newKey();
    public static final Key<$Account, String> LOGIN = f.newKey();
    public static final Key<$Account, byte[]> PASSWORD = f.newKey();
    public static final Key<$Account, Boolean> ENABLED = f.newKey();

    static {
        f.lock(); // Lock the factory
    }

    /** Basic data */
    @Nullable
    private final Account data;

    public $Account(@Nullable Account data) {
        this.data = data;
    }

    public $Account() {
        this(null);
    }

    @Nonnull
    public Account original() {
        return data != null ? data : this;
    }

    @Override
    public <VALUE> VALUE get(Key<? super $Account, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> Ujo set(Key<? super $Account, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super $Account, VALUE> key) {
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
    public KeyList<$Account> readKeyList() {
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
            }
        } else {
            switch (key.getIndex()) {
                case 0: return super.getId();
                case 1: return super.getLogin();
                case 2: return super.getPassword();
                case 3: return super.getEnabled();
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
            }
        } else {
            switch (key.getIndex()) {
                case 0: super.setId((Integer) value); return;
                case 1: super.setLogin((String) value); return;
                case 2: super.setPassword((byte[]) value); return;
                case 3: super.setEnabled((Boolean) value); return;
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

}
