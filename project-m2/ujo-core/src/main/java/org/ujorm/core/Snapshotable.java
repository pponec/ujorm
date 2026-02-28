package org.ujorm.core;

import org.jetbrains.annotations.NotNull;

/** Interface for cloning and managing object snapshots. */
public interface Snapshotable<D> extends Cloneable, SnapshotProvider<D> {

    /** Save a shallow copy of the current state internally */
    void saveSnapshot() throws IllegalStateException;

    /**
     * Creates a shallow copy or throws an exception.
     * Recommended implementation:
     * <pre>
     * try {
     *     return (D) super.clone();
     * } catch (CloneNotSupportedException e) {
     *     throw new IllegalStateException(e);
     * }
     * </pre>
     */
    @NotNull
    D clone() throws IllegalStateException;
}