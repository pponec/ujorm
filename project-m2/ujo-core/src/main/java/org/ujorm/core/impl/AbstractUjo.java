package org.ujorm.core.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Ujo;

/** Extended API */
@RequiredArgsConstructor
public abstract class AbstractUjo<D> implements Ujo<D> {

    final DomainHandler<D> domainHandler;

    @Override
    public DomainHandler<D> domainHandler() {
        return domainHandler;
    }

    /** Common method to get object value by the keyName.
     *
     * @param keyName Property must be a direct type only!
     */
    public final <V> V getValue(@NotNull String keyName) {
        return (V) getValue(domainHandler.getKey(keyName));
    }

    /** Common method to assign object value by the keyName.
     *
     * @param keyName Property must be a direct type only!
     * @param value Value
     */
    public final <V> void setValue(@NotNull String keyName, @Nullable V value) {
        setValue(domainHandler.getKey(keyName), value);
    }

    /** Provides an original domain object. */
    public abstract D buildDomain();

    /** Create a logical empty instance of the UJO */
    public static <D> AbstractUjo<D> of(@NotNull DomainHandler<D> handler) {
        return of(null, handler);
    }

    /** Create new instance of the UJO */
    public static <D> AbstractUjo<D> of(@Nullable D domainObject, @NotNull DomainHandler<D> handler) {
        return handler.getDomainClass().isRecord()
                ? new UjoRecord(domainObject, handler)
                : new UjoBean(domainObject, handler);
    }
}
