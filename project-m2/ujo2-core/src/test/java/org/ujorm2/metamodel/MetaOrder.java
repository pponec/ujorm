package org.ujorm2.metamodel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import org.ujorm2.Key;
import org.ujorm2.core.KeyFactory;
import org.ujorm2.core.KeyImpl;
import org.ujorm2.core.MetaInterface;
import org.ujorm2.core.UjoContext;
import org.ujorm2.doman.Order;
import org.ujorm2.doman.Order.State;

/**
 * TODO: A helper class generated by a Maven module.
 * @author Pavel Ponec
 */
public class MetaOrder<D> extends KeyImpl<D, Order> implements MetaInterface<D> {

    /** All direct keys */
    protected static final class DirectKey<D> {

        final KeyFactory<Order> keyFactory = new KeyFactory(Order.class);

        final Key<Order, Integer> id = keyFactory.newKey(
                (d) -> d.getId(),
                (d, v) -> d.setId(v));

        final Key<Order, State> state = keyFactory.newKey(
                (d) -> d.getState(),
                (d, v) -> d.setState(v));

        final Key<Order, BigDecimal> totalPrice = keyFactory.newKey(
                (d) -> d.getTotalPrice(),
                (d, v) -> d.setTotalPrice(v));

        final MetaUser<D> user = keyFactory.newRelation(
                (d) -> d.getUser(),
                (d, v) -> d.setUser(v));

        final Key<Order, String> note = keyFactory.newKey(
                (d) -> d.getNote(),
                (d, v) -> d.setNote(v));

        final Key<Order, LocalDateTime> created = keyFactory.newKey(
                (d) -> d.getCreated(),
                (d, v) -> d.setCreated(v));

        public DirectKey() {
            keyFactory.close();
        }
    };

    /** All direct keys */
    private final DirectKey key = new DirectKey();

    public MetaOrder(UjoContext context) {
        super(Order.class, context, null);
    }

    public MetaOrder(@Nullable Key<D,?> keyPrefix, UjoContext context) {
        super(keyPrefix.getDomainClass(), context, keyPrefix);
    }

    @Override
    public D createDomain() {
        return (D) new Order();
    }

    // --- KEY PROVIDERS ---

    public Key<D, Integer> id() {
        return getKey(key.id);
    }

    public Key<D, State> state() {
        return getKey(key.state);
    }

    public  Key<D, BigDecimal> totalPrice() {
        return getKey(key.totalPrice);
    }

    public MetaUser<D> user() {
        return (MetaUser) getKey(key.user);
    }

    public Key<D, String> note() {
        return getKey(key.note);
    }

    public Key<D, LocalDateTime> created() {
        return getKey(key.created);
    }

    public static final MetaOrder<Order> of(@Nullable UjoContext context) {
        return context.getMetaModel(Order.class, MetaOrder.class);
    }

    public static final MetaOrder<Order> of() {
        return of(UjoContext.of());
    }

}
