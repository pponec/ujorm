package org.ujorm.tools.jdbc;

/** Unchecked SQL exception */
public final class SQLExceptionBuilder {

    /** Static methods only */
    private SQLExceptionBuilder() {
    }

    public static SQLException build(String s) {
       return new SQLException(s);
    }

    public static SQLException build(Throwable cause) {
        return new SQLException(cause);
    }

    public static SQLException build(String message, Throwable cause) {
        return new SQLException(message, cause);
    }
}
