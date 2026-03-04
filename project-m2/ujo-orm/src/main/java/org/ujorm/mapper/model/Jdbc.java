package org.ujorm.mapper.model;

public record Jdbc(
        boolean isOracleDb,
        char quoteChar )
{}
