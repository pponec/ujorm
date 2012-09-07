/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm.core;

import org.ujorm.UjoAction;

/**
 * A default implementation of the UjoAction.
 * @author Pavel Ponec
 */
public class UjoActionImpl implements UjoAction {
    
    private final int type;
    private final Object context;
    
    public UjoActionImpl(int type, Object context) {
        this.type = type;
        this.context = context;
    }
     
    public UjoActionImpl(Object context) {
        this(ACTION_UNDEFINED, context);
    }
     
    public UjoActionImpl(int type) {
        this(type, null);
    }
     
    /** Returns a type of the action. The default type is ACTION_UNDEFINED. 
     * <ul>
     * <li>Numbers are reserved in range (from 0 to 999, inclusive) for an internal usage of the Ujorm.</li>
     * <li>Zero is an undefined action</li>
     * <li>Negative values are free for general usage too</li>
     * </ul>
     * <br>The number can be useful for a resolution of an action for a different purpose (e.g. export to 2 different XML files).
     */
    public final int getType() {
        return type;
    }

    /** Returns a conetxt of the action. The value is dedicated to a user usage and the value can be null. */
    public final Object getContext() {
        return context;
    }

    /** String value */
    @Override
    public String toString() {
        String result = String.valueOf(type) + ", " + context;
        return result;
    }
}
