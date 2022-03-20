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

    @Nullable
    protected final Key<D, ?> keyPrefix;

    /** All direct keys */
    @Nonnull
    protected final DirectKeyRing directKeyRing;
    
    /** Model context */
    @Nullable
    protected ModelContext context;

    /** All direct keys */
    protected final boolean descending;

    protected AbstractDomainModel(
            @Nullable Key<D, ?> keyPrefix, 
            @Nonnull DirectKeyRing directKeyRing, 
            boolean descending) {
        super(keyPrefix != null 
                ? keyPrefix.getDomainClass() 
                : directKeyRing.getKeyFactory().getDomainClass());
        this.keyPrefix = keyPrefix;
        this.directKeyRing = directKeyRing;
        this.descending = descending;
        // Close directKeyRing:
        directKeyRing.getKeyFactory().close(directKeyRing);
    }

    public AbstractDomainModel(@Nonnull DirectKeyRing keyRing) {
        this(null, keyRing, false);
    }

    public abstract <A> AbstractDomainModel<A, V> prefix(@Nonnull final Key<A, D> key);


    /** Provider of an instance of DirectKeys */
    protected abstract DirectKeyRing keys();

    public D createDomain() {
        try {
            return getDomainClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    protected final <V> AbstractDomainModel<D, V> getKey(final @Nonnull ProxyKey<V> directKey) {
        if (keyPrefix != null) {
            final AbstractDomainModel domainModel = null; // TODO.pop ??? directKeyRing.getContext().getStore$().getDomainModel(directKey.getValueClass());
            Assert.validState(domainModel != null, "No model found for the key: {}.{}",
                    directKey.get().getDomainClass().getSimpleName(),
                    directKey);
            return domainModel.prefix(directKey.get());
        } else {
            if (context == null) {
                throw new IllegalStateException("Context was not assigned");
            }
            final AbstractDomainModel<D, V> result = context.getDomainModel(directKey.get());
            return result;
        }
    }

    protected DirectKeyRing getDirectKey() {
        return directKeyRing;
    }

    /** Assign a model context*/
    public void setContext(@Nonnull ModelContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "Domain model '" + super.toString() + "' : "
                + directKeyRing.getKeyFactory().getDomainClass().getSimpleName();
    }
}
