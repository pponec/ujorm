/*
 * Copyright 2020-2020 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/JdbcBuilder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.common;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;

/**
 * The wrapper implements a common {@code hash()}, {@code equals()} and {@code compareTo()} methods
 *
 * <h3>Usage:</h3>
 * <pre class="pre">
 *   Person p1 = new Person(1, "A");
 *   Person p2 = new Person(1, "B");
 *   Wrapper&lt;Person&gt; w1 = Wrapper.of(p1, Person::getId, Person::getName);
 *   Wrapper&lt;Person&gt; w2 = w1.wrap(p2);
 *   assertTrue(w1.compareTo(w2) &lt; 0);
 * </pre>
 *
 * @author Pavel Ponec
 * @param <V> Object value
 */
public final class Wrapper<V> implements Comparable<Wrapper<V>>, Confessionable {

    /** The original value */
    @Nonnull
    private final V value;
    @Nonnull
    private final Function<V, ?>[] functions;
    /** Sort null values first */
    private final boolean nullFirst;

    private Wrapper(@Nonnull final V value, boolean nullFirst, @Nonnull final Function<V, ?>[] functions) {
        this.value = value;
        this.functions = functions;
        this.nullFirst = nullFirst;
    }

    /** Returns the original value */
    @Nonnull
    public V getValue() {
        return value;
    }

    /** Create a new wrapper for the value */
    public Wrapper<V> wrap(@Nonnull final V value) {
        return new Wrapper(value, nullFirst, functions);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final V ext = ((Wrapper<V>) o).value;
        for (Function<V, ?> f : functions) {
            if (!Objects.equals(f.apply(value), f.apply(ext))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final Object[] values = new Object[functions.length];
        for (int i = 0; i < functions.length; i++) {
            values[i] = functions[i].apply(value);
        }
        return Objects.hash(values);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(@Nullable final Wrapper<V> wrapper) {
        if (wrapper == null) {
            return nullFirst ? 1 : -1;
        }
        final V ext = wrapper.value;
        for (Function<V, ?> f : functions) {
            final Comparable o1 = (Comparable) f.apply(value);
            final Comparable o2 = (Comparable) f.apply(ext);
            if (o1 == o2) {
                continue;
            }
            if (o1 == null) {
                return nullFirst ? -1 : 1;
            }
            if (o2 == null) {
                return nullFirst ? 1 : -1;
            }
            final int result = o1.compareTo(o2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return value.toString();
    }
    
    @Override
    public Appendable confessTo(Appendable writer) throws IOException {
        writer.append(value.toString());
        return writer;
    }

    /** Create a new wrapper */
    public static final <D, P> Wrapper<D> of(
            @Nonnull final D value,
            @Nonnull final Function<D, P>... functions) {
        return Wrapper.of(value, true, functions);
    }

    /** Create a new wrapper */
    public static final <D, P> Wrapper<D> of(
            @Nonnull final D value,
            final boolean nullFirst,
            @Nonnull final Function<D, P>... functions) {
        Assert.hasLength(functions, "Function is required");
        return new Wrapper<>(value, nullFirst, functions);
    }

}
