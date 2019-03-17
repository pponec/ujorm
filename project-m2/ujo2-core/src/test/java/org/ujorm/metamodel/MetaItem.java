package org.ujorm.metamodel;

import org.ujorm.doman.*;
import java.math.BigDecimal;
import org.ujorm.Key;
import org.ujorm.core.AbstractKey;

/**
 *
 * @author Pavel Ponec
 */
public class MetaItem<T> extends AbstractKey<T, Item>{

    public Key<Item, Integer> id() {
        return null;
    }

    public Key<Item, String> note() {
        return null;
    }

    public Key<Item, BigDecimal> price() {
        return null;
    }

    public MetaOrder<T> order() {
        return null;
    }

}
