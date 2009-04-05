/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.orm;

import org.ujoframework.core.UjoIterator;
import org.ujoframework.implementation.map.MapProperty;

/**
 * The relation 1:N to another UJO type items
 * @author Pavel Ponec
 * @see org.ujoframework.core.UjoIterator
 */
public class RelationToMany<UJO extends TableUjo, ITEM  extends TableUjo>
    extends MapProperty<UJO, UjoIterator<ITEM>>
{

    private final Class<ITEM> itemType;

    /** Constructor */
    @SuppressWarnings("unchecked")
    public RelationToMany(String name, Class<ITEM> itemType) {
        super(name, (Class<UjoIterator<ITEM>>) (Class) UjoIterator.class );
        this.itemType = itemType;
    }

    /** Constructor
     * @param name Property name.
     * @param itemType The type of item.
     * @param index An property order
     */
    @SuppressWarnings("unchecked")
    public RelationToMany(String name, Class<ITEM> itemType, int index) {
        super(name, (Class<UjoIterator<ITEM>>) (Class) UjoIterator.class, index );
        this.itemType = itemType;
    }


    /** Returns ItemType */
    public Class<ITEM> getItemType() {
        return itemType;
    }


}