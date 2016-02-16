/* Powered by the Ujorm, don't modify it */

package org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated;

import org.ujorm.core.DefaultUjoConverter;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Customer;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Item;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Order;

/**
 * Generated $UjoConverter
 * @author Pavel Ponec
 */
public class $UjoConverter {

    private static final DefaultUjoConverter<OrmUjo> CONVERTER = org.ujorm.orm.InternalUjo.CONVERTER;

    public static $Customer ujo(Customer customer) {
        return ($Customer) CONVERTER.marshal(customer);
    }

    public static $Order ujo(Order order) {
        return ($Order) CONVERTER.marshal(order);
    }

    public static $Item ujo(Item item) {
        return ($Item) CONVERTER.marshal(item);
    }

}
