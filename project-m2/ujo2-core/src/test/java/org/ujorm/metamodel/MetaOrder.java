package org.ujorm.metamodel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.core.AbstractKey;
import org.ujorm.core.MetaInterface;
import org.ujorm.core.UjoContext;
import org.ujorm.core.lambda.Getter;
import org.ujorm.core.lambda.Setter;
import org.ujorm.doman.*;
import org.ujorm.doman.Order.State;

/**
 * TODO: A helper class generated by a Maven module.
 * @author Pavel Ponec
 */
public class MetaOrder<T> extends AbstractKey<T, Order> implements MetaInterface<T> {


    private final Key<T, Integer> id;
    private final Key<T, State> state;
    private final Key<T, BigDecimal> totalPrice;
    private final MetaUser<T> user;
    private final Key<T, String> note;
    private final Key<T, LocalDateTime> created;

    public MetaOrder(Class<T> domainClass, UjoContext context) {
        super(domainClass, context);


        // Init the keys:
        final Setter<Order, Integer> idSetter = (domain, value) -> domain.setId(value);
        final Getter<Order, Integer> idGetter = (domain) -> domain.getId();
        id = context.createKey(domainClass, Integer.class, idSetter, idGetter);


        state = context.createKey(domainClass, State.class, null, null);
        totalPrice = context.createKey(domainClass, BigDecimal.class, null, null);
        user = (MetaUser<T>) context.createEntity(domainClass, User.class, null, null);
        note = context.createKey(domainClass, String.class, null, null);
        created = context.createKey(domainClass, LocalDateTime.class, null, null);
    }

    public Key<T, Integer> keyId() {
        return id;
    }

    public Key<T ,State> keyState() {
        return state;
    }

    public  Key<T, BigDecimal> keyTotalPrice() {
        return totalPrice;
    }

    public MetaUser<T> keyUser() {
        return user;
    }

    public Key<T, String> keyNote() {
        return note;
    }

    public Key<T, LocalDateTime> keyCreated() {
        return created;
    }

    @Override
    public T newDomain() {
        return (T) new Order();
    }

    public static final MetaOrder<Order> of(@Nullable UjoContext context) {
        return context.getMetaModel(Order.class, MetaOrder.class);
    }

    public static final MetaOrder<Order> of() {
        return of(UjoContext.of());
    }

}
