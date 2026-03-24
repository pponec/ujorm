package org.ujorm.orm.model;

/** Represents a pair of delimiters used for quoting SQL identifiers or values. */

public enum DatabaseVendor {
    /** Standard JDBC */
    DEFAULT,
    ORACLE,
    MS_SQL_SERVER
}