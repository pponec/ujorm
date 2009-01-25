/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db.sample;

import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.db.TableUjo;

/**
 *
 * @Table=bo_item
 */
public class BoItem extends TableUjo {

    /** @id */
    public static final UjoProperty<BoItem,Long> ID = newProperty("id", Long.class);
    public static final UjoProperty<BoItem,Integer> USER_ID = newProperty("usrId", Integer.class);
    public static final UjoProperty<BoItem,String> DESCR = newProperty("description", String.class);
    public static final UjoProperty<BoItem,BoOrder> ORDER = newProperty("order", BoOrder.class);


}
