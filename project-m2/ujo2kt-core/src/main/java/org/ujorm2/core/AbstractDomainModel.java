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

    /** All direct keys */
    protected final boolean descending;

    protected AbstractDomainModel(@Nullable Key<D, ?> keyPrefix, @Nonnull DirectKeyRing directKeyRing, boolean descending) {
        super(keyPrefix != null ? keyPrefix.getDomainClass() : directKeyRing.getKeyFactory().getDomainClass());
        this.keyPrefix = keyPrefix;
        this.directKeyRing = directKeyRing;
        this.descending = descending;
    }

    public AbstractDomainModel(@Nonnull DirectKeyRing keyRing) {
        this(null, keyRing, false);
    }

    public abstract <A> AbstractDomainModel<A, V> prefix(@Nonnull final Key<A, D> key);


    /** Provider of an instance of DirectKeys */
    protected abstract DirectKeyRing keys();

    @Deprecated
    protected ModelContext getContext$() {
        return directKeyRing.getContext();
    }

    @Deprecated
    public void setContext$(@Nonnull final ModelContext context) {
        throw new UnsupportedOperationException("Move the method to: DirectKeyRing");
//        Assert.validState(this.context == null, "Context is clocked");
//        this.context = context;
    }

    public D createDomain() {
        try {
            return getDomainClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    protected final <V> Key<D, V> getKey(final @Nonnull MKey<V> directKey) {
        if (keyPrefix != null) {
            final AbstractDomainModel domainModel = null; // TODO.pop ??? directKeyRing.getContext().getStore$().getDomainModel(directKey.getValueClass());
            Assert.validState(domainModel != null, "No model found for the key: {}.{}",
                    directKey.get().getDomainClass().getSimpleName(),
                    directKey);
            return domainModel.prefix(directKey.get());
        } else {
            // An exception is throwed due:
            //   java.lang.ClassCastException: org.ujorm2.core.MKey 
            //   cannot be cast to org.ujorm2.Key
	    //   at org.ujorm2.Ujo2Test.mainUjo2Test(Ujo2Test.java:34)
            return (Key) directKey;
        }
    }

    protected DirectKeyRing getDirecKey() {
        return directKeyRing;
    }
}
