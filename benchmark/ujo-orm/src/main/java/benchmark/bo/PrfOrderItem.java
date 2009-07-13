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
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.orm.annot.Column;

/**
 * Rodič vešech objednávek
 * @author ponec
 */
public class PrfOrderItem extends OrmTable<PrfOrderItem> {

    @Column(pk=true)
    public static final Property<PrfOrderItem,Long> id = newProperty(Long.class);
    public static final Property<PrfOrderItem,String> publicId = newProperty(String.class);
    public static final Property<PrfOrderItem,Boolean> deleted = newProperty(false);
    public static final Property<PrfOrderItem,Date> dateDeleted = newProperty(Date.class);
    public static final Property<PrfOrderItem,BigDecimal> price = newProperty(BigDecimal.class);
    public static final Property<PrfOrderItem,BigDecimal> charge = newProperty(BigDecimal.class);
    public static final Property<PrfOrderItem,Boolean> arrival = newProperty(Boolean.class);
    @Column(lenght=100)
    public static final Property<PrfOrderItem,String> description = newProperty(String.class);
    public static final Property<PrfOrderItem,PrfUser> user = newProperty("user_id", PrfUser.class);
    public static final Property<PrfOrderItem,PrfOrder> order = newProperty("order_id", PrfOrder.class);
    public static final Property<PrfOrderItem,PrfOrderItem> parent = newProperty("parent_id", PrfOrderItem.class);


}
