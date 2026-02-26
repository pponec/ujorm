package org.ujorm.tools.jdbc;

/** Unchecked SQL exception */
public final class SQLExceptionBuilder {

    /** Static methods only */
    private SQLExceptionBuilder() {
    }

    public static final SQLException build(String s) {
       return new SQLException(s);
    }

    public static final SQLException build(Throwable cause) {
        return new SQLException(cause);
    }

    public static final SQLException build(String message, Throwable cause) {
        return new SQLException(message, cause);
    }
}
