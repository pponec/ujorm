package org.version2.tools;

import org.ujorm.Key;
import org.ujorm.tools.MsgFormatter;

/**
 * UnsupportedKey
 * @author Pavel Ponec
 */
public class UnsupportedKey extends IllegalArgumentException {

    public UnsupportedKey(Key<?,?> key) {
        super(MsgFormatter.format("Unsupported key: {}[{}]"
                , key.getFullName()
                , key.getIndex()));
    }

}
