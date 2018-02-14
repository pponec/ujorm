package org.ujorm.wicket.component.form;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.SmartUjo;

/**
 * Dummy Ujo
 * @author Ponec
 */
public final class DummyUjo extends SmartUjo<DummyUjo> {

    /** Factory */
    private static final KeyFactory<DummyUjo> f = newFactory(DummyUjo.class);

    public static final Key<DummyUjo, Byte> ID = f.newKey();

    static {
        f.lock();
    }

}
