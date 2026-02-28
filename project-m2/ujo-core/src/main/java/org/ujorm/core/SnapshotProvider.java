package org.ujorm.core;

import org.jetbrains.annotations.Nullable;

/** Interface for cloning and managing object snapshots. */
public interface SnapshotProvider<D> {

    /** Get the previously saved snapshot of the object */
    @Nullable
    D readSnapshot();
}