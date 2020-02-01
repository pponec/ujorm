package org.ujorm.metamodel;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.core.AbstractKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.MetaInterface;
import org.ujorm.core.UjoContext;
import org.ujorm.doman.*;

/**
 * TODO: A helper class generated by a Maven module.
 * @author Pavel Ponec
 */
public class MetaItem<D extends Item> extends AbstractKey<D, Item> implements MetaInterface<D> {

    public MetaItem(Class<D> domainClass, UjoContext context) {
        super(domainClass, context);
    }

    private final KeyFactory<Item> keyFactory = new KeyFactory(Item.class);


    private final BiConsumer<D, Integer> writer = (d, u) -> d.setId(u);
    private final Function<D, Integer> reader = d -> d.getId();
    private final Key<D, Integer> id = keyFactory.newKey("id", Integer.class, writer, reader);


    private final Key<D, String> note = null;

    private final Key<D, BigDecimal> price = null;

    private final MetaOrder<D> order = null;

    private final Key<D, Boolean> descending$ = null;

    private final Key<D, Integer> codePoints = null;

    // - - - -

    public Key<D, Integer> id() {
        return null;
    }

    public Key<D, String> note() {
        return null;
    }

    public Key<D, BigDecimal> price() {
        return null;
    }

    public MetaOrder<D> order() {
        return null;
    }

    public Key<D, Boolean> descending$() {
        return null;
    }

    public Key<D, Integer> codePoints$() {
        return null;
    }

    // ---- Helper method

    @Override
    public D newDomain() {
        return (D) new Item();
    }

    public static final MetaItem<Item> of(@Nullable UjoContext context) {
        return context.getMetaModel(Item.class, MetaItem.class);
    }

    public static final MetaItem<Item> of() {
        return of(UjoContext.of());
    }


}
