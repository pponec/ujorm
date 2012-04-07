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
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
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
    public static final Property<UjoOrder,Long> ID = newProperty("id", Long.class);
    public static final Property<UjoOrder,Boolean> DELETED = newProperty("deleted", false);
    public static final Property<UjoOrder,Date> DATE_DELETED = newProperty("dateDeleted", Date.class);
    @Column(length=2)
    public static final Property<UjoOrder,String> DELETION_REASON = newProperty("deletionReason", String.class);
    public static final Property<UjoOrder,Boolean> PAID = newProperty("paid", Boolean.class);
    @Column(length=8)
    public static final Property<UjoOrder,String> PUBLIC_ID = newProperty("publicId", String.class);
    public static final Property<UjoOrder,Date> DATE_OF_ORDER = newProperty("dateOfOrder", Date.class);
    @Column(length=2)
    public static final Property<UjoOrder,String> PAYMENT_TYPE = newProperty("paymentType", String.class);
    public static final Property<UjoOrder,BigDecimal> DISCOUNT = newProperty("discount", BigDecimal.class);
    @Column(length=2)
    public static final Property<UjoOrder,String> ORDER_TYPE = newProperty("orderType", String.class);
    @Column("language_code")
    public static final Property<UjoOrder,String> LANGUAGE = newProperty("language", String.class);
    public static final Property<UjoOrder,UjoOrder> PARENT = newProperty("parent_id", UjoOrder.class);
    public static final UjoProperty<UjoOrder,UjoUser> USER = newProperty("user_id", UjoUser.class);
    public static final RelationToMany<UjoOrder,UjoOrderItem> ITEMS = newRelation("items", UjoOrderItem.class);


    // Optional code for better performance:
    private static UjoPropertyList properties = init(UjoOrder.class);
    @Override public UjoPropertyList readProperties() { return properties; }

}
