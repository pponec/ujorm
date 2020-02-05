package org.ujorm2.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm2.Key;

/**
 * Abstract model of a domain class
 * @author Pavel Ponec
 */
public abstract class AbstractDomainModel<D, V> extends KeyImpl<D, V> {

    /** All direct keys */
    @Nonnull
    protected final KeyFactoryProvider directKeys;

    @Nullable
    protected final Key<D, ?> keyPrefix;

    private AbstractDomainModel(@Nonnull Class domainClass, @Nonnull final KeyFactoryProvider keyFactory, @Nullable Key<D, ?> prefix) {
        super(domainClass);
        this.directKeys = keyFactory;
        this.keyPrefix = prefix;
    }

    public AbstractDomainModel(@Nonnull final KeyFactoryProvider<?> keyFactory) {
        this(keyFactory.getKeyFactory().getDomainClass(), keyFactory, null);

    }

    public AbstractDomainModel(Key<D, ?> keyPrefix) {
        this(keyPrefix.getDomainClass(), null, keyPrefix);
    }

    public D createDomain() {
        try {
            return getDomainClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    protected final <V> Key<D, V> getKey(final @Nonnull Key<?, V> directKey) {
        return keyPrefix != null
                ? keyPrefix.add((Key) directKey)
                : (Key) directKey;
    }

    protected KeyFactoryProvider getDirecKey() {
        return directKeys;
    }
}
