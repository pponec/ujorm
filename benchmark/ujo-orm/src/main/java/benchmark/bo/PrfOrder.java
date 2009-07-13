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
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.annot.Column;

/**
 * Objednávky
 * @author ponec
 */
public class PrfOrder extends OrmTable<PrfOrder> {

    @Column(pk=true)
    public static final Property<PrfOrder,Long> id = newProperty(Long.class);
    public static final Property<PrfOrder,Boolean> deleted = newProperty(false);
    public static final Property<PrfOrder,Date> dateDeleted = newProperty(Date.class);
    @Column(lenght=2)
    public static final Property<PrfOrder,String> deletionReason = newProperty(String.class);
    public static final Property<PrfOrder,Boolean> paid = newProperty(Boolean.class);
    @Column(lenght=8)
    public static final Property<PrfOrder,String> publicId = newProperty(String.class);
    public static final Property<PrfOrder,Date> dateOfOrder = newProperty(Date.class);
    @Column(lenght=2)
    public static final Property<PrfOrder,String> paymentType = newProperty(String.class);
    public static final Property<PrfOrder,BigDecimal> discount = newProperty(BigDecimal.class);
    @Column(lenght=2)
    public static final Property<PrfOrder,String> orderType = newProperty(String.class);
    public static final Property<PrfOrder,String> language = newProperty(String.class);
    public static final Property<PrfOrder,PrfOrder> parent = newProperty("parent_id", PrfOrder.class);
    public static final UjoProperty <PrfOrder,PrfUser>user = newProperty("user_id", PrfUser.class);

    public static final RelationToMany <PrfOrder,PrfOrderItem>items = newRelation("items", PrfOrderItem.class);


}
