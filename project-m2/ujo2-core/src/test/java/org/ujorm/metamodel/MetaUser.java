package org.ujorm.metamodel;

import org.ujorm.doman.*;
import java.time.LocalDateTime;
import org.ujorm.Key;
import org.ujorm.core.AbstractKey;

/**
 *
 * @author Pavel Ponec
 */
public class MetaUser<T> extends AbstractKey<T, User> {

    public Key<T, Integer> id() {
        return null;
    }

    public Key<T, Short> pin() {
        return null;
    }

    public Key<T, String> firstName() {
        return null;
    }

    public Key<T, String> sureName() {
        return null;
    }

    public Key<T, LocalDateTime> created() {
        return null;
    }

    public Key<T, MetaUser> parent() {
        return null;
    }

}
