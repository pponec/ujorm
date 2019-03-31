# Description of key-value architecture with POJO support inspired by Ujorm framework

## Introduction

This document describes a design for an `key-value` API that will work with plain POJOs. 
The solution assumes new meta-model classes generated according to the POJO template - for example by a Maven plugin.
An interface [Ujo](http://ujorm.org/javadoc/org/ujorm/Ujo.html) will be unnecessary and therefore the API cannot be
backward compatible with the current [Ujorm](https://ujorm.org/) framework.
However, removing the Ujo interface will complicate implementation
some services, such as managing parameters in a project
[DemoHotels](https://hotels.ujorm.org/source?src=org.ujorm.hotels.gui.hotel.HotelTable) .


## New Features

* An instance of [Key](http://ujorm.org/javadoc/org/ujorm/Key.html) interface will support direct Java serialization
  (the original implementation requires wrapping the keys before serialization into a KeyRing object). 
  However a unique instance of two identical direct keys can no longer be guaranteed.
* A new meta-model context is created with the (working) name `UjoContext` to provide meta-object instances to
  minimize the number of their number instances.
  Developers can provide a different context, so the number of meta-object instances can be multiple.
* Each meta-model will contain a reference to the POJO class and vice versa.
* After obtaining the meta-model, the creation of compound keys is simplified because only methods without static constants are used.
* POJO objects in ORM will be able to use standard JPA annotations, but only a subset will be supported.

## Class model

Simplified Scheme:

![Class model](docs/images/ApiUjorm2.svg "Class model")


## Examples of use

```java
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
```

```java
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
```

```java
    /** Criterions */
    public void doItemCondition() {
        MetaItem<Item> mItem = MetaItem.of();

        Criterion<Item> itemCrn1 = mItem.forAll();
        List<Item> items = itemCrn1.select(findItemsService());

        Criterion<Item> crn1 = mItem.keyOrder().keyId().forEq(10);
        Criterion<Item> crn2 = mItem.keyOrder().keyCreated().forLe(LocalDateTime.now());
        Criterion<Item> crn3 = crn1.and(crn2);
        List<Item> result = crn3.select(findItemsService());
    }
```

The API (compile-ready draft) is stored in a branch
[Ujo2](https://github.com/pponec/ujorm/blob/Ujorm2/project-m2/ujo2-core/src/test/java/org/ujorm/service/MySampleService.java). 


## An implementation of the design

Although this proposal can be interesting, I do not plan to implement it at this time.

--

Writen by Pavel Ponec

author of the Ujorm framework

on: 2019-03-22