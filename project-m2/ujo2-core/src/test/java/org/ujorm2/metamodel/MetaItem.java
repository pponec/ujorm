package org.ujorm2.metamodel;

import java.math.BigDecimal;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm2.Key;
import org.ujorm2.core.AbstractDomainModel;
import org.ujorm2.core.DirectKeyRing;
import org.ujorm2.core.KeyFactory;
import org.ujorm2.doman.Item;

/**
 * TODO: A helper class generated by a Maven module.
 * @author Pavel Ponec
 * @param <D> Domain
 */
public class MetaItem<D> extends AbstractDomainModel<D, Item> {

    /** All direct keys */
    static final class DirectKeys<T extends Item> extends DirectKeyRing<T> {

        final KeyFactory<T> keyFactory = new KeyFactory(Item.class);

        final Key<T, Integer> id = keyFactory.newKey(
                (d) -> d.getId(),
                (d, v) -> d.setId(v));

        final Key<T, String> note = keyFactory.newKey(
                (d) -> d.getNote(),
                (d, v) -> d.setNote(v));

        final Key<T, BigDecimal> price = keyFactory.newKey(
                (d) -> d.getPrice(),
                (d, v) -> d.setPrice(v));

        final MetaOrder<T> order = keyFactory.newRelation(
                (d) -> d.getOrder(),
                (d, v) -> d.setOrder(v));

        final Key<T, Boolean> descending = keyFactory.newKey(
                (d) -> d.getDescending(),
                (d, v) -> d.setDescending(v));

        final Key<T, Integer> codePoints = keyFactory.newKey(
                (d) -> d.getCodePoints(),
                (d, v) -> d.setCodePoints(v));

        @Override
        public KeyFactory<T> getKeyFactory() {
            return keyFactory;
        }
    };

    public MetaItem() {
        super(new DirectKeys());
    }

    public MetaItem(@Nullable final Key<D, ?> keyPrefix, @Nonnull final DirectKeyRing directKeyRing, final boolean descending) {
        super(keyPrefix, directKeyRing, descending);
    }

    @Override
    public <A> AbstractDomainModel<A, Item> prefix(Key<A, D> key) {
        return new MetaItem(key, keys(), false);
    }

    @Override
    public D createDomain() {
        return (D) new Item();
    }

    /** Provider of an instance of DirectKeys */
    @Override
    protected final DirectKeys keys() {
        return (DirectKeys) directKeyRing;
    }

    // --- KEY PROVIDERS ---

    public Key<D, Integer> id() {
        return getKey(keys().id);
    }

    public Key<D, String> note() {
        return getKey(keys().note);
    }

    public Key<D, BigDecimal> price() {
        return getKey(keys().price);
    }

    public MetaOrder<D> order() {
        return (MetaOrder) getKey(keys().order);
    }

    public Key<D, Boolean> descending$() {
        return getKey(keys().descending);
    }

    public Key<D, Integer> codePoints$() {
        return getKey(keys().codePoints);
    }

    // ---- Helper method

    public static final MetaItem<Item> of(@Nullable KeyFactory context) {
        return new MetaItem<>();
    }

}
