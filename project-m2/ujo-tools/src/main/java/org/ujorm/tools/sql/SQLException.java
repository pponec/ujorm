package org.ujorm.tools.sql;

/** Unchecked SQL exception
 * @deprecated Use the same class from the package {@code org.ujorm.tools.jdbc} rather.
 */
@Deprecated
public class SQLException extends org.ujorm.tools.jdbc.SQLException {

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
