/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.sample;

import java.sql.SQLException;

/**
 *
 * @author pavel
 */
public class DatabaseException extends RuntimeException {

    DatabaseException(String message, SQLException e, String sql, Object[] params) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
