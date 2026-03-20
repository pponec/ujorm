package org.ujorm.orm.model;

/** Represents a pair of delimiters used for quoting SQL identifiers or values. */
public record SqlQuote(

        /** Returns the open quote character. */
        char open,

        /** Returns the close quote character. */
        char close
) {

    /** The default ANSI SQL quote instance. */
    private static final SqlQuote DEFAULT_QUOTE = new SqlQuote('"', '"');

    /** Returns the default ANSI SQL quotes. */
    public static SqlQuote ofDefault() {
        return DEFAULT_QUOTE;
    }

    /** Returns quotes for MySQL dialects. */
    public static SqlQuote ofMySql() {
        return new SqlQuote('`', '`');
    }

    /** Returns quotes for MS SQL Server dialect. */
    public static SqlQuote ofSqlServer() {
        return new SqlQuote('[', ']');
    }
}