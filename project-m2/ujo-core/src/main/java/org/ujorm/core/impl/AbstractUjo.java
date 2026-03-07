package org.ujorm.core.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.DomainHandler;
import org.ujorm.Ujo;

/** Extended API */
@RequiredArgsConstructor
public abstract class AbstractUjo<D> implements Ujo<D> {

    final DomainHandler<D> domainHandler;

    @Override
    public DomainHandler<D> domainHandler() {
        return domainHandler;
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
