package org.ujorm.orm.model;

/** Supported database vendors for SQL dialect resolution. */
public enum DatabaseVendor {

    /** Standard JDBC (e.g. PostgreSQL, H2) */
    DEFAULT,

    /** MySQL database */
    MY_SQL,

    /** MariaDB database */
    MARIA_DB,

    /** Microsoft SQL Server */
    MS_SQL_SERVER,

    /** Oracle database */
    ORACLE
}