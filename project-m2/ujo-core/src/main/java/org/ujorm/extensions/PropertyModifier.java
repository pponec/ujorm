/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm.extensions;

/**
 * Property Setter
 * @author Ponec
 */
public class PropertyModifier {

    /** Write name into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setType(Class type, Property property) {
        if (!property.isLock()) {
            final int index = property.getIndex();
            property.init(null, type, null, index, false);
        }
    }

    /** Write an item type into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setItemType(Class itemType, AbstracCollectionProperty property) {
        if (itemType==null) {
            throw new IllegalArgumentException("Item type is undefined for property: " + property);
        }
        if (!property.isLock()) {
            property.initItemType(itemType);
        }
    }
    
    /** Write name into property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setName(String name, Property property) {
        if (!property.isLock()) {
            property.init(name, null, null, Property.UNDEFINED_INDEX, false);
        }
    }

    /** Set the new index and lock the property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void setIndex(int anIndex, Property property) {
        if (!property.isLock() && property.getIndex()!=anIndex) {
            property.init(null, null, null, anIndex, true);
        }
    }

    /** Set the new index and lock the property if it is not locked yet. */
    @SuppressWarnings("unchecked")
    public static void lock(Property property) {
        if (!property.isLock()) {
            property.init(null, null, null, Property.UNDEFINED_INDEX, true);
        }
    }

    /** Lock the property */
    public static boolean isLock(Property property) {
        return property.isLock();
    }

}
