/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.tools.criteria;

/**
 *
 * @author pavel
 */
public interface AbstractOperator {

    /** Is the operator binary type ? */
    public boolean isBinary();

    /** Returns Enum */
    public Enum getEnum();

}
