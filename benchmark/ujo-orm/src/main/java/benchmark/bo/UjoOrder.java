/*
 *  Copyright 2009-2022 Pavel Ponec
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
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.annot.Column;

/**
 * Orders
 * @author Pavel Ponec
 */
public class UjoOrder extends OrmTable<UjoOrder> {
    private static final OrmKeyFactory<UjoOrder> f = newCamelFactory(UjoOrder.class);

    @Column(pk=true)
    public static final Key<UjoOrder,Long> ID = f.newKey("id");
    public static final Key<UjoOrder,Boolean> DELETED = f.newKey("deleted", false);
    public static final Key<UjoOrder,Date> DATE_DELETED = f.newKey("dateDeleted");
    @Column(length=2)
    public static final Key<UjoOrder,String> DELETION_REASON = f.newKey("deletionReason");
    public static final Key<UjoOrder,Boolean> PAID = f.newKey("paid");
    @Column(length=8)
    public static final Key<UjoOrder,String> PUBLIC_ID = f.newKey("publicId");
    public static final Key<UjoOrder,Date> DATE_OF_ORDER = f.newKey("dateOfOrder");
    @Column(length=2)
    public static final Key<UjoOrder,String> PAYMENT_TYPE = f.newKey("paymentType");
    public static final Key<UjoOrder,BigDecimal> DISCOUNT = f.newKey("discount");
    @Column(length=2)
    public static final Key<UjoOrder,String> ORDER_TYPE = f.newKey("orderType");
    @Column("language_code")
    public static final Key<UjoOrder,String> LANGUAGE = f.newKey("language");
    public static final Key<UjoOrder,UjoOrder> PARENT = f.newKey("parent_id");
    public static final Key<UjoOrder,UjoUser> USER = f.newKey("user_id");
    public static final RelationToMany<UjoOrder,UjoOrderItem> ITEMS = f.newRelation();

    static { f.lock(); }

    /** An optional method for a better performance */
    @Override
    public KeyList<UjoOrder> readKeys() {
        return f.getKeys();
    }

}
