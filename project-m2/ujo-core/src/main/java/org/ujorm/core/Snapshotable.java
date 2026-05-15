package org.ujorm.core;

/** Interface for cloning and managing object snapshots. */
public interface Snapshotable<D> extends SnapshotProvider<D> {

    /**
     * Save a shallow copy of the current state internally.
     * @return The current instance for method chaining
     * @throws IllegalStateException If the snapshot cannot be created
     */
    D saveSnapshot() throws IllegalStateException;
}