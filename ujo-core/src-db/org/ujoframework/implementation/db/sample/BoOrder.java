/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db.sample;

import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.db.TableUjo;
import org.ujoframework.implementation.db.UjoRelative;

/**
 *
 * @Table=bo_order
 */
public class BoOrder extends TableUjo {

    /** @id */
    public static final UjoProperty<BoOrder,Long> ID = newProperty("id", Long.class);
    public static final UjoProperty<BoOrder,Integer> USER_ID = newProperty("usrId", Integer.class);
    public static final UjoProperty<BoOrder,String> DESCR = newProperty("description", String.class);
    public static final UjoProperty<BoOrder,Date> DATE = newProperty("date", Date.class);

    /** Select BoOrder */
    public static final UjoRelative<BoOrder,BoItem> ITEMS = newRelation("items", BoItem.class);

}
