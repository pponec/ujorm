/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db;

import org.ujoframework.core.UjoIterator;
import org.ujoframework.implementation.map.MapProperty;

/**
 * Relation 1:N to another table
 * @author pavel
 */
public class UjoRelative<UJO extends TableUjo, ITEM  extends TableUjo>
    extends MapProperty<UJO, UjoIterator<ITEM>>
{

    private final Class<ITEM> itemType;

    /** Constructor */
    @SuppressWarnings("unchecked")
    public UjoRelative(String name, Class<ITEM> itemType) {
        super(name, (Class<UjoIterator<ITEM>>) (Class) UjoIterator.class );
        this.itemType = itemType;
    }

    /** Returns ItemType */
    public Class<ITEM> getItemType() {
        return itemType;
    }


}