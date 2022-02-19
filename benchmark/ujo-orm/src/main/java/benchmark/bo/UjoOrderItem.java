/*
 *  Copyright 2020-2022 Pavel Ponec
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
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.annot.Column;

/**
 * Order item
 * @author Pavel Ponec
 */
public class UjoOrderItem extends OrmTable<UjoOrderItem> {
    private static final OrmKeyFactory<UjoOrderItem> f = newCamelFactory(UjoOrderItem.class);

    @Column(pk=true)
    public static final Key<UjoOrderItem,Long> ID = f.newKey("id");
    public static final Key<UjoOrderItem,String> PUBLIC_ID = f.newKey("publicId");
    public static final Key<UjoOrderItem,Boolean> DELETED = f.newKey("deleted", false);
    public static final Key<UjoOrderItem,Date> DATE_DELETED = f.newKey("dateDeleted");
    public static final Key<UjoOrderItem,BigDecimal> PRICE = f.newKey("price");
    public static final Key<UjoOrderItem,BigDecimal> CHARGE = f.newKey("charge");
    public static final Key<UjoOrderItem,Boolean> ARRIVAL = f.newKey("arrival");
    @Column(length=100)
    public static final Key<UjoOrderItem,String> DESCRIPTION = f.newKey("description");
    public static final Key<UjoOrderItem,UjoUser> USER = f.newKey("user_id");
    public static final Key<UjoOrderItem,UjoOrder> ORDER = f.newKey("order_id");
    public static final Key<UjoOrderItem,UjoOrderItem> PARENT = f.newKey("parent_id");

    /** Indirect key: ORDER.DELETED */
    public static final Key<UjoOrderItem,Boolean> _ORDER_DELETED = ORDER.add(UjoOrder.DELETED);

    static { f.lock(); }


    /** An optional method for a better performance */
    @Override
    public KeyList<UjoOrderItem> readKeys() {
        return f.getKeys();
    }

}
