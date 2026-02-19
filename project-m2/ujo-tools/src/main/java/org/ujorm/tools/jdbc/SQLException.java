package org.ujorm.tools.jdbc;

/** Unchecked SQL exception */
public class SQLException extends IllegalStateException {

    public SQLException(String s) {
        super(s);
    }

    public SQLException(Throwable cause) {
        super(cause);
    }

    public SQLException(Throwable cause, String message) {
        super(message, cause);
    }

    public SQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SQLException of(java.sql.SQLException ex) {
        throw new SQLException(ex);
    }
}
