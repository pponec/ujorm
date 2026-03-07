package org.ujorm.mapper;

import org.ujorm.mapper.impl.ConfigImpl;

/** Configuration parameters */
public interface Config {

    /** The first key in the sequence represents the primary key. */
    boolean isFirstPropertyIsIdentifier();

    /** Maximum size of the cache in the ResultSet Mapper */
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

    /** Enable or disable the service of the {@link org.ujorm.mapper.UjormServiceProvider} object. */
    boolean isEnabledUjormServiceProvider();

    static Config ofDefault() {
        return new ConfigImpl();
    }
}
