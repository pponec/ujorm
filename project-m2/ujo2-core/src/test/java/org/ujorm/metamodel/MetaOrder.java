package org.ujorm.metamodel;

import java.math.BigDecimal;
import org.ujorm.doman.*;
import java.time.LocalDateTime;
import org.ujorm.Key;
import org.ujorm.core.AbstractKey;
import org.ujorm.doman.Order.State;
import org.ujorm.metamodel.factory.KeyFactory;

/**
 *
 * @author Pavel Ponec
 */
public class MetaOrder<T> extends AbstractKey<T, Order> {
    
    private KeyFactory f = KeyFactory.of();
    
    private final Key<Order, Integer> id =  f.create();
    private final Key<Order, State> state =  f.create();
    private final Key<Order, BigDecimal> totalPrice =  f.create();
    private final MetaUser<T> user =  (MetaUser<T>) f.createEntity();
    private final Key<Order, String> note =  f.create();
    private final Key<Order, LocalDateTime> created =  f.create();

    public Key<Order, Integer> id() {
        return id;
    }

    public Key<Order, State> state() {
        return state;
    }

    public  Key<Order, BigDecimal> totalPrice() {
        return totalPrice;
    }

    public MetaUser<T> user() {
        return user;
    }

    public Key<Order, String> note() {
        return note;
    }

    public Key<Order, LocalDateTime> created() {
        return created;
    }

}
