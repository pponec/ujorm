/*
 * ZCounter.java
 *
 * Created on 23. March 2008, 9:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xmlSpeed;

/**
 * ZCounter
 * 
 * @author Pavel Ponec
 */
public class ZCounter {
    
    private int count = 0;
    
    /**
     * Creates a new instance of ZCounter
     */
    public ZCounter(int initCount) {
        count = initCount;
    }
    
    /** Substract Count,<br> 
     * returns TRUE, if count is ZERO. 
     */
    public boolean substract() {
        return (--count) <= 0 ;
    }
    
    /** Returns true, if count is great than zero. */
    public boolean isPositive() {
        return count>0;
    }

    /** Returns true, if count is equals or less than zero. */
    boolean isZero() {
        return count<=0;
    }

    @Override
    public String toString() {
        String retValue = String.valueOf(count);
        return retValue;
    }
    
    
}
