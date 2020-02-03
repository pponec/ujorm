package org.ujorm2.metamodel;

import java.time.LocalDateTime;
import javax.annotation.Nullable;
import org.ujorm2.Key;
import org.ujorm2.core.KeyFactory;
import org.ujorm2.core.KeyImpl;
import org.ujorm2.core.MetaInterface;
import org.ujorm2.core.UjoContext;
import org.ujorm2.doman.Item;
import org.ujorm2.doman.User;

/**
 * TODO: A helper class generated by a Maven module.
 * @author Pavel Ponec
 */
public class MetaUser<D> extends KeyImpl<D, User> implements MetaInterface<D> {

    /** All direct keys */
    protected static final class DirectKey<D> {

        final KeyFactory<User> keyFactory = new KeyFactory(User.class);

        final Key<User, Integer> id = keyFactory.newKey(
                (d) -> d.getId(),
                (d, v) -> d.setId(v));

        final Key<User, Short> pin = keyFactory.newKey(
                (d) -> d.getPin(),
                (d, v) -> d.setPin(v));

        final Key<User, String> firstName = keyFactory.newKey(
                (d) -> d.getFirstName(),
                (d, v) -> d.setFirstName(v));

        final MetaOrder<User> sureName = keyFactory.newRelation(
                (d) -> d.getSureName(),
                (d, v) -> d.setSureName(v));

        final Key<User, LocalDateTime> created = keyFactory.newKey(
                (d) -> d.getCreated(),
                (d, v) -> d.setCreated(v));

        final MetaUser<User> parent = keyFactory.newRelation(
                (d) -> d.getParent(),
                (d, v) -> d.setParent(v));

        public DirectKey() {
            keyFactory.close();
        }
    };

    /** All direct keys */
    private final DirectKey key = new DirectKey();

    public MetaUser(Class<D> domainClass, UjoContext context) {
        super(domainClass, context, null);
    }

    public MetaUser(@Nullable Key<D,?> keyPrefix, UjoContext context) {
        super(keyPrefix.getDomainClass(), context, keyPrefix);
    }

    @Override
    public D createDomain() {
        return (D) new Item();
    }

    // --- KEY PROVIDERS ---

    public Key<D, Integer> id() {
        return getKey(key.id);
    }

    public Key<D, Short> pin() {
        return getKey(key.pin);
    }

    public Key<D, String> firstName() {
        return getKey(key.firstName);
    }

    public Key<D, String> sureName() {
        return getKey(key.sureName);
    }

    public Key<D, LocalDateTime> created() {
        return getKey(key.created);
    }

    public MetaUser<D> parent() {
        return (MetaUser) getKey(key.parent);
    }

    public static final MetaUser<User> of(@Nullable UjoContext context) {
        return context.getMetaModel(User.class, MetaUser.class);
    }

    public static final MetaUser<User> of() {
        return of(UjoContext.of());
    }

}
