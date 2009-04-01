/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.core;

import org.ujoframework.UjoProperty;

/**
 * Return a changed properties.
 * @author pavel
 */
public interface ChangeRegister {

    /**
     *  Returns changed properties.
     * @param clear True value clears the property changes.
     */
    public UjoProperty[] readChangedProperties(boolean clear);

}
