/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.tools.criteria;

/**
 * The abstract criteria operator
 * @author Pavel Ponec
 * @since 0.90
 */
public interface AbstractOperator {

    /** Is the operator a binary type ? */
    public boolean isBinary();

    /** Returns Enum */
    public Enum getEnum();

}
