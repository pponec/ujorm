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
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.extensions.PathProperty;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;

/**
 * Order item
 * @author Pavel Ponec
 */
public class UjoOrderItem extends OrmTable<UjoOrderItem> {

    @Column(pk=true)
    public static final Key<UjoOrderItem,Long> ID = newKey("id");
    public static final Key<UjoOrderItem,String> PUBLIC_ID = newKey("publicId");
    public static final Key<UjoOrderItem,Boolean> DELETED = newKey("deleted", false);
    public static final Key<UjoOrderItem,Date> DATE_DELETED = newKey("dateDeleted");
    public static final Key<UjoOrderItem,BigDecimal> PRICE = newKey("price");
    public static final Key<UjoOrderItem,BigDecimal> CHARGE = newKey("charge");
    public static final Key<UjoOrderItem,Boolean> ARRIVAL = newKey("arrival");
    @Column(length=100)
    public static final Key<UjoOrderItem,String> DESCRIPTION = newKey("description");
    public static final Key<UjoOrderItem,UjoUser> USER = newKey("user_id");
    public static final Key<UjoOrderItem,UjoOrder> ORDER = newKey("order_id");
    public static final Key<UjoOrderItem,UjoOrderItem> PARENT = newKey("parent_id");

    /** Indirect property: ORDER.DELETED */
    public static final PathProperty<UjoOrderItem,Boolean> _ORDER_DELETED = PathProperty.newInstance(UjoOrderItem.ORDER, UjoOrder.DELETED);


    // Optional code for better performance:
    private static KeyList properties = init(UjoOrderItem.class);
    @Override public KeyList readProperties() { return properties; }

}
