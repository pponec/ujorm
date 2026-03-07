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
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Interface for standard CRUD (Create, Read, Update, Delete) operations.
 * <p>
 * This interface provides a comprehensive set of methods to interact with the database,
 * ranging from single-entity operations to highly optimized, memory-efficient batch processing using Java Streams.
 * It is designed to fully support both traditional mutable JavaBeans and modern immutable objects (like Java Records).
 *
 * @param <D> The domain class type (e.g., Entity or Record)
 * @param <V> The primary key class type
 */
public interface Crud<D, V> {

    /**
     * Inserts a stream of domain objects into the database using JDBC batching.
     * <p>
     * <b>Performance & Memory:</b> The stream is processed sequentially and eagerly. Entities are chunked
     * into small batches, ensuring minimal memory footprint even when processing millions of records.
     *
     * @param domains    A stream of entities to be inserted. If a parallel stream is provided,
     *                   it will be forced to sequential execution to guarantee JDBC thread safety.
     * @param onInserted An optional callback invoked for each successfully inserted entity.
     *                   This is the recommended way to retrieve newly generated primary keys,
     *                   especially for immutable objects (Records) where a new instance is yielded.
     * @return The total number of successfully inserted rows.
     */
    long insertBatch(@NotNull Stream<D> domains, @Nullable Consumer<D> onInserted);

    /**
     * Inserts an array (or varargs) of domain objects using batching support.
     * <p>
     * The method processes the items and updates the array elements in-place with generated identifiers.
     * If the domain objects are immutable (like Java Records), new instances are created and placed back
     * into the corresponding array indices.
     * <p>
     * <b>CRITICAL WARNING:</b> Always capture and use the returned array!
     * If you pass elements directly via varargs (creating an implicit array), the method replaces
     * the instances inside that temporary array. You will lose all references to the updated entities
     * with generated keys if you ignore the return value.
     *
     * @param domains The array or varargs of entities to insert.
     * @return The original array containing the inserted entities, updated with generated identifiers.
     */
    @SuppressWarnings("unchecked")
    D[] insertBatch(@NotNull D... domains);

    /**
     * Inserts a single domain object into the database.
     *
     * @param domain The domain object to be inserted (either a mutable JavaBean or an immutable Record).
     * @return The domain object populated with the newly generated primary key.
     *         For mutable objects, this is typically the same instance passed as the parameter.
     *         For immutable Records, a completely new instance is returned.
     * @throws IllegalArgumentException If no generated ID value could be retrieved from the database.
     */
    D insert(@NotNull D domain);

    /**
     * Processes a stream of domain objects and decides dynamically whether to insert or update each entity.
     * <p>
     * The decision is based on the primary key: if the primary key is empty (null or zero), the entity
     * is inserted. Otherwise, it is updated.
     * <p>
     * <b>Performance:</b> Stream operations are evaluated sequentially. To ensure optimal memory usage,
     * entities are grouped into small batches for both INSERT and UPDATE statements under the hood.
     *
     * @param domains    A stream of entities to be processed.
     * @param onInserted An optional callback invoked <b>only</b> for newly inserted entities.
     *                   Useful for capturing the assigned primary keys.
     * @param properties Optional list of specific property names to update (applies to UPDATE operations only).
     *                   If empty, all columns except the primary key are updated.
     * @return The total number of rows affected (both inserted and updated).
     */
    long insertOrUpdate(@NotNull Stream<D> domains, @Nullable Consumer<D> onInserted, CharSequence... properties);

    /**
     * Retrieves a single domain object by its primary key.
     *
     * @param id The primary key identifier of the entity to fetch.
     * @return The populated domain object, or {@code null} if no record is found in the database.
     */
    @Nullable
    D findByIdNullable(@NotNull V id);

    /**
     * Retrieves a single domain object by its primary key, wrapped in an Optional.
     *
     * @param id The primary key identifier of the entity to fetch.
     * @return An {@link Optional} containing the domain object if found, otherwise {@link Optional#empty()}.
     */
    @NotNull
    Optional<D> findById(@NotNull V id);

    /**
     * Creates an instance of {@link SqlParamBuilder} to safely construct and execute SELECT queries
     * with bound parameters. The builder shares the database connection and configuration with this Crud instance.
     *
     * @param whereCondition The SQL WHERE clause (without the 'WHERE' keyword).
     *                       If undefined or empty, the query will target all records.
     * @return A fluent query builder for fetching data.
     */
    @NotNull
    public <R> R selectWhere(
            @Nullable String whereCondition,
            @NotNull SqlParamBuilder.SqlFunction<SqlParamBuilder, R> fun);

    /**
     * Updates a single domain object in the database.
     *
     * @param domain     The domain object containing the updated data.
     * @param properties An optional list of specific property (column) names to update.
     *                   If left empty, all mapped properties (excluding the primary key) are updated.
     * @return The number of rows affected by the update statement (typically 1).
     */
    long update(@NotNull D domain, CharSequence... properties);

    /**
     * Updates a stream of domain objects efficiently using JDBC batching.
     * <p>
     * Note: This method executes a full update for each entity based on the provided properties.
     * It does not check for actual changes. For optimized, partial updates, see {@link #updateChanged(Stream)}.
     *
     * @param domains    A stream of domain objects to update. Evaluated sequentially.
     * @param properties An optional list of specific property (column) names to update for all entities in the batch.
     *                   If empty, all columns (excluding the primary key) are updated.
     * @return The total number of rows affected by the batch update execution.
     */
    long update(@NotNull Stream<D> domains, CharSequence... properties);

    /**
     * Updates a single domain object optimally by detecting actual modifications.
     * <p>
     * <b>Important:</b> The entity must implement the {@link SnapshotProvider} interface.
     * Only the columns that have actually changed (compared to the entity's internal snapshot)
     * are included in the SQL UPDATE statement. If no changes are detected, the DB hit is skipped.
     *
     * @param domain The domain object to update.
     * @param <D2>   The type of the domain object, bounded by SnapshotProvider.
     * @return The number of affected rows (1 if updated, 0 if skipped due to no changes).
     * @throws IllegalStateException If the entity is missing a saved snapshot.
     */
    default <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull D2 domain) {
        Objects.requireNonNull(domain, "Domain object must not be null");
        return updateChanged(Stream.of(domain));
    }

    /**
     * Updates an array of domain objects optimally using batching and collision detection.
     * <p>
     * <b>Note:</b> It is highly recommended to disable auto-commit ({@code connection.setAutoCommit(false)})
     * for transactional safety and performance during batch operations.
     * <p>
     * <b>Important:</b> Entities must implement the {@link SnapshotProvider} interface. The underlying
     * SQL statements are dynamically generated and batched based on the specific columns changed in each entity.
     *
     * @param domains An array or varargs of domain objects to update.
     * @param <D2>    The type of the domain object, bounded by SnapshotProvider.
     * @return The total number of rows updated in the database.
     * @throws IllegalStateException If any entity is missing a saved snapshot.
     * @throws IllegalArgumentException If the array contains nulls or objects of an invalid type.
     */
    @SuppressWarnings("unchecked")
    default <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull D2... domains) {
        Objects.requireNonNull(domains, "Domains array must not be null");
        return updateChanged(Stream.of(domains));
    }

    /**
     * Updates a stream of domain objects optimally using batching and collision detection.
     * <p>
     * <b>Note:</b> It is highly recommended to disable auto-commit ({@code connection.setAutoCommit(false)})
     * for transactional safety and performance during batch operations.
     * <p>
     * <b>Important:</b> Entities must implement the {@link SnapshotProvider} interface. The internal engine
     * dynamically groups and batches entities that share the exact same modification pattern (same changed columns),
     * maximizing JDBC batching efficiency while skipping entities with zero changes.
     *
     * @param domains A stream of domain objects to process.
     * @param <D2>    The type of the domain object, bounded by SnapshotProvider.
     * @return The total number of rows updated in the database.
     * @throws IllegalStateException If any entity is missing a saved snapshot.
     * @throws IllegalArgumentException If the stream contains nulls or objects of an invalid type.
     */
    <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull Stream<D2> domains);

    /**
     * Deletes a single domain object from the database based on its primary key.
     *
     * @param domain The domain object to delete. Must have a valid primary key.
     * @return The number of rows deleted (typically 1).
     */
    int delete(@NotNull D domain);

    /**
     * Deletes a single domain object from the database by its primary key identifier.
     *
     * @param id The identifier of the record to be deleted.
     * @return The number of rows deleted (typically 1).
     */
    int deleteById(@NotNull V id);

    /**
     * Deletes multiple domain objects from the database using JDBC batching.
     *
     * @param domains An array or varargs of entities to delete.
     * @return The total number of rows successfully deleted.
     */
    @SuppressWarnings("unchecked")
    default int deleteBatchEntities(@NotNull D... domains) {
        Objects.requireNonNull(domains, "Domains array must not be null");
        return delete(Stream.of(domains));
    }

    /**
     * Deletes a stream of domain objects from the database efficiently using JDBC batching.
     * Evaluates the stream sequentially and processes deletions in chunks to save memory.
     *
     * @param domains A stream of domain objects to delete.
     * @return The total number of rows successfully deleted.
     */
    int delete(@NotNull Stream<D> domains);
}