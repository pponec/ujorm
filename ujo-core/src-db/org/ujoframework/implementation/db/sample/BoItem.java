/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db.sample;

import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.db.TableUjo;

/**
 * Each Item have got a reference to a common Order.
 * @Table=bo_item
 */
public class BoItem extends TableUjo {

    /** Unique key
     * @id
     */
    public static final UjoProperty<BoItem,Long> ID = newProperty("id", Long.class);
    /** User key */
    public static final UjoProperty<BoItem,Integer> USER_ID = newProperty("usrId", Integer.class);
    /** Description of Item */
    public static final UjoProperty<BoItem,String> DESCR = newProperty("description", String.class);
    /** A reference to common Order */
    public static final UjoProperty<BoItem,BoOrder> ORDER = newProperty("order", BoOrder.class);


}
