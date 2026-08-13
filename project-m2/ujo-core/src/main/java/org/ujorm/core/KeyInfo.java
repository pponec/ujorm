/*
 * Copyright 2007-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * This interface is a descriptor of the {@link Ujo} attribute. The Key contains only meta-data;
 * therefore, the implementation never contains business data.
 * Each instance of the Key must be located in a {@code public static final} field of a Ujo implementation.
 * The Key is not intended to be serializable because each instance is unique to its related Java field.
 * An appropriate solution for serialization is to use a decorator class, such as {@code KeyRing}.
 * <br>See <a href="package-summary.html#UJO">general information</a> about the framework or explore existing implementations.
 *
 * @author Pavel Ponec
 * @see Ujo
 */
@Unmodifiable
@SuppressWarnings("deprecation")
public interface KeyInfo<VALUE> {

    // --- Low priority ---

    /** Returns the name of the database column label. */
    @NotNull String columnLabel();

    /** Indicates whether the database column is required. */
    boolean required();

    /** Indicates whether the column is a primary key. */
    boolean primaryKey();

    /** Indicates whether the column is a foreign key. */
    boolean foreignKey();

    /** Determines whether to map the Enum by its ordinal (true) or name (false). */
    boolean mapEnumByOrdinal();

    /**
     * Indicates whether the value can be assigned to an existing domain object by the
     * {@link Key#setValue(Object, Object)} method.
     * A record component and a bean property with a getter only are not writable,
     * so the setter raises an {@link UnsupportedOperationException}.
     * <p>Note that the {@code ujo-orm} module maps writable properties only,
     * because reading an entity from a database means assigning its values.
     * <p>The default value {@code true} suits a bean, because this interface knows no domain class.
     * The {@link org.ujorm.core.impl.AbstractKey} implementation replaces the default by
     * a record-safe one, so a key built on another base class must override this method itself.
     * <p><strong>The default implementation is temporary.</strong> It keeps the binary compatibility
     * of a patch release, but it answers {@code true} for every key that fails to override it -
     * including a key whose {@code setValue()} raises an exception. The method becomes abstract
     * in the release 3.1.0, so implement it explicitly.
     */
    default boolean writable() {
        return true;
    }
}
