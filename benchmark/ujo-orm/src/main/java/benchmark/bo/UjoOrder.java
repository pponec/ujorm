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
import org.ujoframework.UjoProperty;
import org.ujoframework.UjoPropertyList;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.annot.Column;

/**
 * Objednávky
 * @author ponec
 */
public class UjoOrder extends OrmTable<UjoOrder> {

    @Column(pk=true)
    public static final Property<UjoOrder,Long> id = newProperty(Long.class);
    public static final Property<UjoOrder,Boolean> deleted = newProperty(false);
    public static final Property<UjoOrder,Date> dateDeleted = newProperty(Date.class);
    @Column(length=2)
    public static final Property<UjoOrder,String> deletionReason = newProperty(String.class);
    public static final Property<UjoOrder,Boolean> paid = newProperty(Boolean.class);
    @Column(length=8)
    public static final Property<UjoOrder,String> publicId = newProperty(String.class);
    public static final Property<UjoOrder,Date> dateOfOrder = newProperty(Date.class);
    @Column(length=2)
    public static final Property<UjoOrder,String> paymentType = newProperty(String.class);
    public static final Property<UjoOrder,BigDecimal> discount = newProperty(BigDecimal.class);
    @Column(length=2)
    public static final Property<UjoOrder,String> orderType = newProperty(String.class);
    @Column("language_code")
    public static final Property<UjoOrder,String> language = newProperty(String.class);
    public static final Property<UjoOrder,UjoOrder> parent = newProperty("parent_id", UjoOrder.class);
    public static final UjoProperty<UjoOrder,UjoUser> user = newProperty("user_id", UjoUser.class);
    public static final RelationToMany<UjoOrder,UjoOrderItem> items = newRelation("items", UjoOrderItem.class);


    // Optional code for better performance:
    private static UjoPropertyList properties = init(UjoOrder.class);
    @Override public UjoPropertyList readProperties() { return properties; }

}
