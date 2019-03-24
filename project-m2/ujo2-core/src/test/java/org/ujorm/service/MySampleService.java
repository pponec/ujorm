package org.ujorm.service;

import java.time.LocalDateTime;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.doman.Item;
import org.ujorm.doman.Order;
import org.ujorm.doman.User;
import org.ujorm.metamodel.MetaItem;
import org.ujorm.metamodel.MetaOrder;

/**
 *
 * @author Pavel Ponec
 */
public class MySampleService {

    /** Reading / writing */
    public void doOrderAccess() {
        MetaOrder<Order> metaOrder = MetaOrder.of();

        Key<Order, Integer> keyOrderId = metaOrder.keyId();
        Key<Order, String> keyUserName = metaOrder.keyUser().keyFirstName();

        Order order = metaOrder.newDomain();
        keyOrderId.setValue(order, 1);
        keyUserName.setValue(order, "Pavel");
        Integer id = keyOrderId.getValue(order);
        String name = keyUserName.getValue(order);
    }

    /** Reading / writing */
    public void doItemAccess() {
        MetaItem<Item> metaItem = MetaItem.of();

        Key<Item, Integer> keyItemId = metaItem.keyId();
        Key<Item, User> keyUser = metaItem.keyOrder().keyUser();
        Key<Item, Short> keyPin = metaItem.keyOrder().keyUser().keyPin();

        Item item = metaItem.newDomain();
        keyItemId.setValue(item, 1);
        Integer orderId1 = keyItemId.getValue(item);
        keyUser.setValue(item, new User());
        User user = keyUser.getValue(item);
        keyPin.setValue(item, (short) 125);
        Short userPin = keyPin.getValue(item);
    }

    /** Criterions */
    public void doItemCondition() {
        MetaItem<Item> mItem = MetaItem.of();

        Criterion<Item> itemCrn1 = mItem.forAll();
        List<Item> items = itemCrn1.select(findItemsService());

        Criterion<Item> criton1 = mItem.keyOrder().keyId().forEq(10);
        Criterion<Item> criton2 = mItem.keyOrder().keyCreated().forLe(LocalDateTime.now());
        Criterion<Item> criton3 = criton1.and(criton2);
        List<Item> result = criton3.select(findItemsService());
    }

    private Iterable<Item> findItemsService() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
