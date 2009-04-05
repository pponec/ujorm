/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.extensions;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * A property for a direction of sorting.
 * @author Ponec
 */
final public class SortingProperty<UJO extends Ujo,VALUE> extends PathProperty<UJO,VALUE> {
    
    private final boolean ascending;

    public SortingProperty(UjoProperty<UJO,VALUE> property, boolean ascending) {
        super(property);
        this.ascending = ascending;
    }

    /** A flag for a direction of sorting. This method returns a value by a constructor parameter */
    @Override
    public final boolean isAscending() {
        return false;
    }



}
