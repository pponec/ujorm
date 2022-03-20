package org.ujorm2.service;

import java.time.LocalDateTime;
import java.util.List;
import org.ujorm2.Key;
import org.ujorm2.criterion.Criterion;
import org.ujorm2.doman.Item;
import org.ujorm2.doman.Order;
import org.ujorm2.doman.User;
import org.ujorm2.metamodel.MetaItem;
import org.ujorm2.metamodel.MetaOrder;
import org.ujorm2.metamodel.DomainModelProvider;
import org.ujorm2.metamodel.MetaUser;

/**
 *
 * @author Pavel Ponec
 */
public class MySampleService {

    private final HelpService helpService = new HelpService();

    /** Reading / writing */
    public void doOrderAccess() {
        DomainModelProvider modelProvider = new DomainModelProvider();
        MetaOrder<Order> metaOrder = modelProvider.order();

        Key<Order, Integer> orderIdKey = metaOrder.id();
        MetaUser<Order> orderUserKey = metaOrder.user();
        Key<Order, String> userNameKey = metaOrder.user().firstName();

        Order order = metaOrder.createDomain();
        orderIdKey.setValue(1, order);
        userNameKey.setValue("Pavel", order);
        Integer id = orderIdKey.getValue(order);
        String name = userNameKey.getValue(order);
    }

    /** Reading / writing */
    public void doItemAccess() {
        DomainModelProvider modelProvider = new DomainModelProvider();
        MetaItem<Item> metaItem = modelProvider.item();

        Key<Item, Integer> itemIdKey = metaItem.id();
        Key<Item, User> userKey = metaItem.order().user();
        Key<Item, Short> pinKey = metaItem.order().user().pin();

        Item item = metaItem.createDomain();
        itemIdKey.setValue(1, item);
        Integer orderId1 = itemIdKey.getValue(item);
        userKey.setValue(new User(), item);
        User user = userKey.getValue(item);
        pinKey.setValue((short) 125, item);
        Short userPin = pinKey.getValue(item);
    }

    /** Criterions */
    public void doItemCondition() {
        DomainModelProvider modelProvider = new DomainModelProvider();
        MetaItem<Item> mItem = modelProvider.item();

        Criterion<Item> itemCrn1 = mItem.forAll();
        List<Item> items = itemCrn1.select(helpService.findItemsService());

        Criterion<Item> crn1 = mItem.order().id().forEq(10);
        Criterion<Item> crn2 = mItem.order().created().forLe(LocalDateTime.now());
        Criterion<Item> crn3 = crn1.and(crn2);
        List<Item> result = crn3.select(helpService.findItemsService());
    }

}
