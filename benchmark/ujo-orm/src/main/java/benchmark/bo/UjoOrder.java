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
import org.ujorm.extensions.Property;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.annot.Column;

/**
 * Objednávky
 * @author Pavel Ponec
 */
public class UjoOrder extends OrmTable<UjoOrder> {

    @Column(pk=true)
    public static final Key<UjoOrder,Long> ID = newKey("id");
    public static final Key<UjoOrder,Boolean> DELETED = newKey("deleted", false);
    public static final Key<UjoOrder,Date> DATE_DELETED = newKey("dateDeleted");
    @Column(length=2)
    public static final Key<UjoOrder,String> DELETION_REASON = newKey("deletionReason");
    public static final Key<UjoOrder,Boolean> PAID = newKey("paid");
    @Column(length=8)
    public static final Key<UjoOrder,String> PUBLIC_ID = newKey("publicId");
    public static final Key<UjoOrder,Date> DATE_OF_ORDER = newKey("dateOfOrder");
    @Column(length=2)
    public static final Key<UjoOrder,String> PAYMENT_TYPE = newKey("paymentType");
    public static final Key<UjoOrder,BigDecimal> DISCOUNT = newKey("discount");
    @Column(length=2)
    public static final Key<UjoOrder,String> ORDER_TYPE = newKey("orderType");
    @Column("language_code")
    public static final Key<UjoOrder,String> LANGUAGE = newKey("language");
    public static final Key<UjoOrder,UjoOrder> PARENT = newKey("parent_id");
    public static final Key<UjoOrder,UjoUser> USER = newKey("user_id");
    public static final RelationToMany<UjoOrder,UjoOrderItem> ITEMS = newRelation("items", UjoOrderItem.class);


    // Optional code for better performance:
    private static KeyList properties = init(UjoOrder.class);
    @Override public KeyList readKeys() { return properties; }

}
