/*
 *  Copyright 2009 Paul Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package benchmark.bo;

import java.math.BigDecimal;
import java.util.Date;
import org.ujorm.UjoPropertyList;
import org.ujorm.extensions.PathProperty;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;

/**
 * Order item
 * @author Pavel Ponec
 */
public class UjoOrderItem extends OrmTable<UjoOrderItem> {

    @Column(pk=true)
    public static final Property<UjoOrderItem,Long> ID = newProperty("id", Long.class);
    public static final Property<UjoOrderItem,String> PUBLIC_ID = newProperty("publicId", String.class);
    public static final Property<UjoOrderItem,Boolean> DELETED = newProperty("deleted", false);
    public static final Property<UjoOrderItem,Date> DATE_DELETED = newProperty("dateDeleted", Date.class);
    public static final Property<UjoOrderItem,BigDecimal> PRICE = newProperty("price", BigDecimal.class);
    public static final Property<UjoOrderItem,BigDecimal> CHARGE = newProperty("charge", BigDecimal.class);
    public static final Property<UjoOrderItem,Boolean> ARRIVAL = newProperty("arrival", Boolean.class);
    @Column(length=100)
    public static final Property<UjoOrderItem,String> DESCRIPTION = newProperty("description", String.class);
    public static final Property<UjoOrderItem,UjoUser> USER = newProperty("user_id", UjoUser.class);
    public static final Property<UjoOrderItem,UjoOrder> ORDER = newProperty("order_id", UjoOrder.class);
    public static final Property<UjoOrderItem,UjoOrderItem> PARENT = newProperty("parent_id", UjoOrderItem.class);

    /** Indirect property: ORDER.DELETED */
    public static final PathProperty<UjoOrderItem,Boolean> _ORDER_DELETED = PathProperty.newInstance(UjoOrderItem.ORDER, UjoOrder.DELETED);


    // Optional code for better performance:
    private static UjoPropertyList properties = init(UjoOrderItem.class);
    @Override public UjoPropertyList readProperties() { return properties; }

}
