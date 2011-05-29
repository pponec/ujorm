/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client.cquery;

/**
 * The abstract criteria operator
 * @author Pavel Ponec
 * @since 0.90
 */
public interface AbstractCOperator {

    /** Is the operator a binary type ? */
    public boolean isBinary();

    /** Returns Enum */
    public Enum getEnum();

}
