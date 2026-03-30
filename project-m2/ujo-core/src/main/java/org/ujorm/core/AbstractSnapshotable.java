package org.ujorm.core;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for cloning and managing object snapshots using the {@code Cloneable} mechanism.
 * <p>
 * <b>Usage Risks:</b>
 * <ul>
 * <li><b>Shallow Copy:</b> The default implementation performs a shallow copy. If the domain object
 *   contains mutable structures (like Lists, Maps, or other Objects), both the original and
 *   the snapshot will share the same references.</li>
 *   <li><b>Deep Copy Solution:</b> To ensure full isolation of the snapshot, the developer
 *   can override the {@code clone()} method in the subclass to perform a deep copy of any
 *   mutable fields.</li>
 *   <li><b>Constructor Bypass:</b> The {@code clone()} method creates a new instance without
 *   calling any constructor, which might bypass initialization logic or integrity checks.</li>
 *   <li><b>Final Fields:</b> Cloning does not work well with {@code final} fields that need
 *   to be unique per instance.</li>
 * </ul>
 * <b>Recommended use case:</b> Simple JavaBeans with a default constructor and primitive
 * or immutable wrappers (String, Long, etc.).
 *
 * @param <D> The type of the domain object (typically the subclass itself)
 * @author Pavel Ponec
 */
public abstract class AbstractSnapshotable<D> implements Cloneable, Snapshotable<D> {

    /** Internal storage for the object state snapshot. */
    @Nullable
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    private transient D snapshot;

    /**
     * Get the previously saved snapshot of the object.
     * @return The previously saved snapshot, or {@code null} if none exists.
     */
    @Override
    public @Nullable D readSnapshot() {
        return snapshot;
    }

    /**
     * Save a shallow copy (by default) of the current state internally.
     * <p>
     * Note: This method uses {@link #clone()} to create the snapshot.
     * @return The current instance for method chaining.
     * @throws IllegalStateException If the cloning fails (e.g. {@code CloneNotSupportedException}).
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull D saveSnapshot() throws IllegalStateException {
        try {
            this.snapshot = (D) clone();
            return (D) this;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Snapshot creation failed", e);
        }
    }

    /**
     * Creates and returns a shallow copy of this object.
     * The internal {@code snapshot} reference of the new copy is always reset to {@code null}.
     * <p>
     * <b>Note for developers:</b> You can override this method in subclasses to implement
     * a <strong>deep copy</strong> for mutable fields to prevent shared state between
     * the original object and its snapshot.
     *
     * @return A shallow copy of this instance.
     * @throws CloneNotSupportedException If the object's class does not support the {@code Cloneable} interface.
     */
    @Override
    @SuppressWarnings("S2975")
    protected Object clone() throws CloneNotSupportedException {
        var result = super.clone();
        ((AbstractSnapshotable<?>) result).snapshot = null;
        return result;
    }
}