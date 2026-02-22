package org.ujorm.core.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;

/** Ujo implementation for classes type of the Java Bean */
public final class UjoBean<D> extends AbstractUjo<D> {

    @NotNull
    private final D values;

    public UjoBean(@Nullable D values, @NotNull DomainHandler<D> domainHandler) {
        super(domainHandler);
        this.values = values != null ? values : domainHandler.newDomain();
    }

    @Override
    public <V> Object getValue(@NotNull Key<D, V> key) {
        return key.getValue(values);
    }

    @Override
    public <V> void setValue(@NotNull Key<D, V> key, @Nullable V value) {
        key.setValue(values, value);
    }

    @Override
    public DomainHandler<D> domainHandler() {
        return null;
    }

    @Override
    public D toDomainObject() {
        return values;
    }
}
