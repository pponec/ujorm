package org.ujorm.orm.model;

import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Check;

/** Represents a pair of delimiters used for quoting SQL identifiers or values. */
public record QuotePair(

        /** Returns the open quote character. */
        char open,

        /** Returns the close quote character. */
        char close
) {

    /** Returns the combined string representation of the quotes or empty string if none. */
    public String toQuotes() {
        return open == ' ' ? ""
                : open == close ? String.valueOf(open)
                : (String.valueOf(open) + close);
    }

    /** The default ANSI SQL quote instance. */
    private static final QuotePair DEFAULT_QUOTE = new QuotePair('"', '"');

    /** Returns the default ANSI SQL quotes. */
    public static QuotePair ofDefault() {
        return DEFAULT_QUOTE;
    }

    /** The first and last characters serve as opening and closing delimiters. Otherwise, use the default value. */
    public static QuotePair ofString(@Nullable String quotePair) {
        return Check.isEmpty(quotePair) ? ofDefault() : new QuotePair(
                quotePair.charAt(0),
                quotePair.charAt(quotePair.length() - 1));
    }

    /** Returns quotes for MySQL and MariaDB dialects. */
    public static QuotePair ofMySql() {
        return new QuotePair('`', '`');
    }

    /** Returns quotes for MS SQL Server dialect. */
    public static QuotePair ofMsSqlServer() {
        return new QuotePair('[', ']');
    }

    /** No Quote. */
    public static QuotePair ofNone() {
        return new QuotePair(' ', ' ');
    }
}