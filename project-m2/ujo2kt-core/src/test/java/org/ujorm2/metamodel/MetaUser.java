package org.ujorm2.metamodel;

import java.time.LocalDateTime;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm2.Key;
import org.ujorm2.core.AbstractDomainModel;
import org.ujorm2.core.DirectKeyRing;
import org.ujorm2.core.KeyFactory;
import org.ujorm2.core.ProxyKey;
import org.ujorm2.doman.Item;
import org.ujorm2.doman.User;

/**
 * TODO: A helper class generated by a Maven module.
 * @author Pavel Ponec
 */
public class MetaUser<D> extends AbstractDomainModel<D, User> {

    /** All direct keys */
    static final class DirectKeys<T extends User> extends DirectKeyRing<T> {

        final KeyFactory<T> keyFactory = new KeyFactory(User.class);

        final ProxyKey<Integer> id = keyFactory.newKey(
                (d) -> d.getId(),
                (d, v) -> d.setId(v));

        final ProxyKey<Short> pin = keyFactory.newKey(
                (d) -> d.getPin(),
                (d, v) -> d.setPin(v));

        final ProxyKey<String> firstName = keyFactory.newKey(
                (d) -> d.getFirstName(),
                (d, v) -> d.setFirstName(v));

        final ProxyKey<String> sureName = keyFactory.newKey(
                (d) -> d.getSureName(),
                (d, v) -> d.setSureName(v));

        final ProxyKey<LocalDateTime> born = keyFactory.newKey(
                (d) -> d.getBorn(),
                (d, v) -> d.setBorn(v));

        final ProxyKey<User> parent = keyFactory.newKey(
                (d) -> d.getParent(),
                (d, v) -> d.setParent(v));

        @Override
        public KeyFactory<T> getKeyFactory() {
            return keyFactory;
        }
    };

    public MetaUser() {
        super(new DirectKeys());
    }

    public MetaUser(@Nullable final Key<D, ?> keyPrefix, @Nonnull final DirectKeyRing directKeyRing, final boolean descending) {
        super(keyPrefix, directKeyRing, descending);
    }

    @Override
    public <A> AbstractDomainModel<A, User> prefix(Key<A, D> key) {
        return new MetaUser(key, keys(), true);
    }

    @Override
    public D createDomain() {
        return (D) new Item();
    }

    @Override
    protected final DirectKeys keys() {
       return (DirectKeys) directKeyRing;
    }

    // --- KEY PROVIDERS ---

    public Key<D, Integer> id() {
        return getKey(keys().id);
    }

    public Key<D, Short> pin() {
        return getKey(keys().pin);
    }

    public Key<D, String> firstName() {
        return getKey(keys().firstName);
    }

    public Key<D, String> sureName() {
        return getKey(keys().sureName);
    }

    public Key<D, LocalDateTime> born() {
        return getKey(keys().born);
    }

    public MetaUser<D> parent() {
        return (MetaUser) getKey(keys().parent);
    }
}
