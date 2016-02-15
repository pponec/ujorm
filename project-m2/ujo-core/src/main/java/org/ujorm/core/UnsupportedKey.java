package org.ujorm.core;

import org.ujorm.Key;

/**
 * UnsupportedKey
 * @author Pavel Ponec
 */
public class UnsupportedKey extends IllegalArgumentException {

    public UnsupportedKey(Key<?,?> key) {
        super(String.format("Unsupported key: %s[%s]"
                , key.getFullName()
                , key.getIndex()));
    }

}
