package org.ujorm.orm;

import org.ujorm.orm.impl.ConfigImpl;

/** Configuration parameters */
public interface Config {

    /**
     * Determines the default strategy for identifying the primary key of an entity.
     * <p>
     * If this parameter is enabled ({@code true}), the framework automatically assumes
     * that the first declared field in the JavaBean or Record is the identifier.
     * Otherwise, or if you need to specify a different field as the primary key,
     * you must explicitly annotate the field using the JPA {@code @Id} annotation.
     */
    boolean isFirstPropertyIsIdentifier();

    /** Maximum size of the cache in the {@link org.ujorm.orm.jdbc.ResultSetMapper} */
    int getMaxCacheSize();

    /** Batch size for the INSERT */
    int getBatchSize();

    /** Prints all SQL templates to the log. */
    boolean isPrintSql();

    /** Enable quoting the SQL columns */
    boolean isEnableSqlQuoting();

    /** Write a warning if the column is not a relation and has no JDBC mapping. */
    boolean isColumnMappingWarning();

    /** Print warnings, if Connection autocommit is true in batch operations. */
    boolean isAutoCommitWarned();

    /** Enable or disable the service of the {@link org.ujorm.orm.UjormServiceProvider} object. */
    boolean isEnabledUjormServiceProvider();

    /** Build an immutable object with default arguments */
    static Config ofDefault() {
        return new ConfigImpl().lock();
    }
}
