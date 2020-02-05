package org.ujorm2.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
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

    @Nullable
    protected UjoContext context;

    private AbstractDomainModel(@Nonnull Class domainClass, @Nonnull final KeyFactoryProvider keyFactory, @Nullable Key<D, ?> prefix) {
        super(domainClass);
        this.directKeys = keyFactory;
        this.keyPrefix = prefix;
    }

    public AbstractDomainModel(@Nonnull final KeyFactoryProvider<?> keyFactory) {
        this(keyFactory.getKeyFactory().getDomainClass(), keyFactory, null);
    }

    public AbstractDomainModel(@Nonnull final KeyFactoryProvider<?> keyFactory, Key<D, ?> keyPrefix) {
        this(keyPrefix.getDomainClass(), null, keyPrefix);
    }

    public abstract <A> AbstractDomainModel<A, V> prefix(@Nonnull final Key<A, D> key);


    /** Provider of an instance of DirectKeys */
    protected abstract KeyFactoryProvider keys();

    public UjoContext getContext$() {
        return context;
    }

    public void setContext$(@Nonnull final UjoContext context) {
        Assert.validState(this.context == null, "Context is clocked");
        this.context = context;
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
        if (keyPrefix != null) {
            final AbstractDomainModel domainModel = context.getStore$().getDomainModel(directKey.getValueClass());
            Assert.validState(domainModel != null, "No model found for the key: {}.{}",
                    directKey.getDomainClass().getSimpleName(),
                    directKey);
            return domainModel.prefix(directKey);
        } else {
            return (Key) directKey;
        }
    }

    protected KeyFactoryProvider getDirecKey() {
        return directKeys;
    }
}
