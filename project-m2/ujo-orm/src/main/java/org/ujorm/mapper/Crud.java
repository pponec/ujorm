/*
 * Copyright 2026-2026 Pavel Ponec
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
package org.ujorm.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.SnapshotProvider;
import org.ujorm.tools.jdbc.SqlParamBuilder;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface for CRUD operations.
 * @param <D> Domain class
 * @param <V> Primary key class
 */
public interface Crud<D, V> {

    /**
     * Inserts multiple domain objects using batching support.
     * The method returns the original array (or a new one created implicitly by varargs)
     * where individual elements are updated with generated identifiers.
     * <p>
     * <b>CRITICAL WARNING:</b> Always capture and use the returned array!
     * If the domain objects are immutable (e.g., Java Records) and you pass elements directly
     * via varargs (creating an implicit array), the method replaces the instances in the array.
     * You will lose all references to the updated entities if you ignore the return value.
     * </p>
     *
     * @param domains Entities to insert.
     * @return The same array containing updated entities with newly generated identifiers.
     */
    @SuppressWarnings("unchecked")
    D[] insertBatch(@NotNull D... domains);

    /**
     * Inserts a domain object into the database.
     *
     * @param domain The domain object to be inserted (either a bean or a record)
     * @return The object with an assigned identifier.
     * Whenever possible, it returns the same instance provided as a parameter.
     */
    D insert(@NotNull D domain);

    /** Reads a domain object by its identifier. */
    @Nullable
    D findByIdNullable(@NotNull V id);

    /** Reads a domain object by its identifier. */
    @NotNull
    Optional<D> findById(@NotNull V id);

    /**
     * Create instance of SqlParamBuilder to bind parameters and SELECT.
     * The builder has shared database connection with this object.
     * @param whereCondition Undefined or empty value returns all records.
     * @return Query builder
     */
    @NotNull
    SqlParamBuilder select(@Nullable String whereCondition);

    /**
     * Updates a single domain object.
     * @param domain Domain object to update.
     * @param properties Optional list of property names to update. If empty, all properties are updated (excluding id).
     * @return The number of affected rows.
     */
    long update(@NotNull D domain, CharSequence... properties);

    /**
     * Updates a stream of domain objects using batching.
     * @param domains Domain objects to update. If the stream is empty, update all columns excluding PK.
     * @param properties Optional list of property names to update. If empty, all properties are updated (excluding id).
     * @return The number of affected rows.
     */
    long updateBatch(@NotNull Stream<D> domains, CharSequence... properties);

    /**
     * Updates a single domain object with collision detection.
     * Note: Requires connection.setAutoCommit(false) for transactional safety.
     * <br><b>Important:</b> Entities must be of the domain type and implement the {@link SnapshotProvider} interface.
     *
     * @throws IllegalStateException If the entity is missing a saved snapshot.
     */
    default <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull D2 domain) {
        Objects.requireNonNull(domain, "Domain object must not be null");
        return updateChanged(Stream.of(domain));
    }

    /**
     * Updates multiple domain objects using batching and collision detection.
     * <br><b>Note:</b> Requires {@code connection.setAutoCommit(false)} for transactional safety.
     * <br><b>Important:</b> Entities must be of the domain type and implement the {@link SnapshotProvider} interface.
     *
     * @throws IllegalStateException If an entity is missing a saved snapshot.
     * @throws IllegalArgumentException If any entity in the stream is null or of an invalid type.
     */
    @SuppressWarnings("unchecked")
    default <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull D2... domains) {
        Objects.requireNonNull(domains, "Domains array must not be null");
        return updateChanged(Stream.of(domains));
    }

    /**
     * Updates multiple domain objects using batching and collision detection.
     * <br><b>Note:</b> Requires {@code connection.setAutoCommit(false)} for transactional safety.
     * <br><b>Important:</b> Entities must be of the domain type and implement the {@link SnapshotProvider} interface.
     *
     * @throws IllegalStateException If an entity is missing a saved snapshot.
     * @throws IllegalArgumentException If any entity in the stream is null or of an invalid type.
     */
    <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull Stream<D2> domains);

    /** Deletes a domain object. */
    int delete(@NotNull D domain);

    /** Deletes a domain object by its identifier. */
    int deleteById(@NotNull V id);

    /** Deletes multiple domain objects using batching support. */
    @SuppressWarnings("unchecked")
    default int deleteBatchEntities(@NotNull D... domains) {
        Objects.requireNonNull(domains, "Domains array must not be null");
        return deleteBatch(Stream.of(domains));
    }

    /** Deletes multiple domain objects using batching support. */
    int deleteBatch(@NotNull Stream<D> domains);
}