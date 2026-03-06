package org.ujorm.core.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;

/** Ujo implementation for classes type of the Java Record */
public final class UjoRecord<D> extends AbstractUjo<D> {

    private final Object[] values;

    public UjoRecord(@Nullable D domain, @NotNull DomainHandler<D> domainHandler) {
        super(domainHandler);
        this.values = new Object[domainHandler.count()];
        init(domain);
    }

    /** Init data */
    private void init(@Nullable final D domain) {
        if (domain == null) return;
        for (var key : domainHandler.getKeyList()) {
            values[key.index()] = key.getValue(domain);
        }
    }

    @Override
    public <V> Object getValue(@NotNull Key<D, V> key) {
        return values[key.index()];
    }

    @Override
    public <V> void setValue(@NotNull Key<D, V> key, @Nullable V value) {
        values[key.index()] = value;
    }

    @Override
    public DomainHandler<D> domainHandler() {
        return domainHandler;
    }

    @Override
    public D buildDomain() {
        return domainHandler.newDomain(values);
    }
}
