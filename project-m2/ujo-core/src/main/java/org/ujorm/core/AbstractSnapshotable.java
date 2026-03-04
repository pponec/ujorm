package org.ujorm.core;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/** Base class for cloning and managing object snapshots. */
public abstract class AbstractSnapshotable<D> implements Cloneable, Snapshotable<D> {

    @Nullable @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    private transient D snapshot;

    /**
     * Get the previously saved snapshot of the object.
     * @return The previously saved snapshot, or null if none exists
     */
    @Override
    public @Nullable D readSnapshot() {
        return snapshot;
    }

    /**
     * Save a shallow copy of the current state internally.
     * @return The current instance for method chaining
     * @throws IllegalStateException If the snapshot cannot be created
     */
    @Override
    @SuppressWarnings("unchecked")
    public D saveSnapshot() throws IllegalStateException {
        try {
            snapshot = (D) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
        return (D) this;
    }

    /** Clone the object and clear the internal snapshot reference. */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        var result = super.clone();
        ((AbstractSnapshotable<?>) result).snapshot = null;
        return result;
    }
}