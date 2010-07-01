/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.extensions;

/**
 * Property Setter
 * @author Ponec
 */
public class PropertyModifier {

    /** Write name into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setName(String name, Property property) {
        boolean lock = property.isLock();
        if (!lock) {
            int index = property.getIndex();
            property.init(name, null, null, index, false);
        }
    }

    /** Set the new index and lock the property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setIndex(int anIndex, Property property) {
        boolean lock = property.isLock();
        int index = property.getIndex();
        if (!lock && index!=anIndex) {
            property.init(null, null, null, anIndex, true);
        }
    }

}
