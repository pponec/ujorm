package org.ujorm.orm.model;

public record Jdbc(
        boolean isOracleDb,
        char quoteChar )
{}
