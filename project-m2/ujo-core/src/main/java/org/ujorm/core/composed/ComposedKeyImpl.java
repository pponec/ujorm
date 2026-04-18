package org.ujorm.core.composed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.KeyInfo;

import java.util.Arrays;
import java.util.Objects;

/**
 * An implementation of the {@link ComposedKey} interface consisting of a chain of direct keys.
 * The key allows traversing a graph of objects.
 */
public final class ComposedKeyImpl<T, V> implements Key<T, V>, ComposedKey<T, V> {

    /** Empty composited key */
    public static final Key<?,?> EMPTY_KEY = ComposedKeyImpl.ofEmpty() ;

    /** A path of direct keys. */
    private final Key<?, ?>[] keyPath;
    /** Key path name (without domain class). */
    private final String name;

    /** Internal constructor. */
    private ComposedKeyImpl(@NotNull Key<?, ?>... keyPath) {
        this.keyPath = Objects.requireNonNull(keyPath, "keyPath");
        this.name = String.join(".", keyPath);
    }

    @Override
    public @NotNull Key<T, V> self() {
        return this;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @NotNull Class<V> type() {
        return last().type();
    }

    @Override
    public @NotNull Class<T> domainClass() {
        return first().domainClass();
    }

    /** Sets a value; throws a NullPointerException if any intermediate element is null. */
    @Override
    public void setValue(@NotNull T bean, @Nullable V value) throws UnsupportedOperationException {
        var result = (Object) bean;
        var lastIndex = keyPath.length - 1;
        for (var i = 0; i < lastIndex; i++) {
            var key = (Key<Object, Object>) keyPath[i];
            result = Objects.requireNonNull(key.getValue(result),
                    () -> "Intermediate value is null of the: " + key.fullName());
        }
        ((Key<Object, V>) keyPath[lastIndex]).setValue(result, value);
    }

    /** Returns the value or null if any intermediate element in the path is null. */
    @Override
    public V getValue(@NotNull T bean) {
        var result = (Object) bean;
        for (var key : keyPath) {
            result = ((Key<Object, Object>) key).getValue(result);
            if (result == null) {
                return null;
            }
        }
        return (V) result;
    }

    @Override
    public @Nullable V getDefaultValue() {
        return last().getDefaultValue();
    }

    @Override
    public short index() {
        return last().index();
    }

    @Override
    public @NotNull KeyInfo info() {
        return this.last().info();
    }

    @Override
    public int pathSize() {
        return keyPath.length;
    }

    @Override
    public Key<?, ?> pathItem(int index) {
        return keyPath[index < 0 ? keyPath.length + index : index];
    }

    @Override
    public @NotNull String fullName() {
        return domainClass().getSimpleName() + '.' + name;
    }

    @Override
    public String toString() {
        return name;
    }

    /** Returns the first key in the path. */
    private Key<T, ?> first() {
        return (Key<T, ?>) keyPath[0];
    }

    /** Returns the last key in the path. */
    private Key<?, V> last() {
        return (Key<?, V>) keyPath[keyPath.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ComposedKeyImpl<?, ?> that)
                && Objects.equals(this.domainClass(), that.domainClass())
                && Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(keyPath), name);
    }

    // --- STATIC METHODS ---

    /** Creates a new composed key from two parts. */
    public static <D, V1, V2> Key<D, V2> of(Key<D, V1> k1, Key<V1, V2> k2) {
        return ofDirtyKeys(k1, k2);
    }

    /** Creates a new composed key from three parts. */
    public static <D, V1, V2, V3> Key<D, V3> of(Key<D, V1> k1, Key<V1, V2> k2, Key<V2, V3> k3) {
        return ofDirtyKeys(k1, k2, k3);
    }

    /** Creates a new composed key from four parts. */
    public static <D, V1, V2, V3, V4> Key<D, V4> of(Key<D, V1> k1, Key<V1, V2> k2, Key<V2, V3> k3, Key<V3, V4> k4) {
        return ofDirtyKeys(k1, k2, k3, k4);
    }

    /** A factory for merging multiple keys without a strict generic check. */
    @SuppressWarnings("unchecked")
    public static <D, V> Key<D, V> ofDirtyKeys(Key<?, ?>... keys) {
        var size = 0;
        for (var key : keys) {
            size += Objects.requireNonNull(key, "Key is null").pathSize();
        }

        var path = new Key<?, ?>[size];
        var pos = 0;
        for (var k : keys) {
            for (int i = 0, max = k.pathSize(); i < max; i++) {
                path[pos++] = k.pathItem(i);
            }
        }

        return switch (path.length) {
            case 0 -> throw new IllegalArgumentException("Empty argument");
            case 1 -> (Key<D, V>) path[0];
            default -> new ComposedKeyImpl<>(path);
        };
    }

    /** Create empty Composite key. */
    public static <D,V> Key<D,V> ofEmpty() {
        return new ComposedKeyImpl<>();
    }
}