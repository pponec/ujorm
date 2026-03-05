package org.ujorm.mapper.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public interface ResultSetMapper<D> {
    /**
     * Converts the given Stream of ResultSets into a stream of domain objects.
     *
     * @param rs the Stream of ResultSets to process
     * @param columnLabels optional explicitly defined column labels
     * @return a stream of populated domain objects
     * @throws NoSuchElementException if explicit columns don't match the ResultSet metadata
     */
    @NotNull Stream<D> convert(@NotNull Stream<ResultSet> rs, @Nullable CharSequence... columnLabels);

    /**
     * Converts the given Stream of ResultSets into a stream of domain objects.
     *
     * @param resultSet the Stream of ResultSets to process
     * @param columnLabels optional explicitly defined column labels
     * @return a stream of populated domain objects
     * @throws NoSuchElementException if explicit columns don't match the ResultSet metadata
     */
    D map(@NotNull ResultSet resultSet, @Nullable CharSequence... columnLabels);

    /**
     * Converts the given ResultSet into a stream of domain objects.
     *
     * @param rs the ResultSet to process
     * @param columnLabels optional explicitly defined column labels
     * @return a stream of populated domain objects
     */
    @NotNull Stream<D> convert(@NotNull ResultSet rs, @Nullable CharSequence... columnLabels);

    /**
     * Type-safe mapping using Keys for a selection without relations.
     *
     * @param rs A stream of ResultSets to process
     * @param columnLabels Explicitly defined column keys (labels)
     * @return A stream of populated domain objects
     */
    @NotNull Stream<D> convertFlat(@NotNull Stream<ResultSet> rs, @NotNull Key<D, ?>... columnLabels);

    /** Get the last timestamp of the cache clearing */
    Instant getCacheCleared();
}
