/* Powered by the Ujorm, don't modify it */

package org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated;

import java.math.BigDecimal;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UnsupportedKey;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.extensions.UjoMiddle;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.InternalUjo;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.Session;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.*;
import static org.ujorm.orm.InternalUjo.CONVERTER;

/**
 * The column mapping to DB table ITEM (a sample of usage).
 * Note, that the $Item object have got a reference to a $Order object.
 * @hidden
 * @Table=bo_item
 */
@Comment("Order item")
public final class $Item extends Item implements UjoMiddle<$Item>, ExtendedOrmUjo {
    private static final OrmKeyFactory<$Item> f = new OrmKeyFactory($Item.class, true);

    /** Unique key */
    @Column(pk = true)
    public static final Key<$Item,Long> ID = f.newKey();
    /** User key */
    public static final Key<$Item,Integer> USER_ID = f.newKey();
    /** Description of the $Item */
    public static final Key<$Item,String> NOTE = f.newKey();
    /** Price of the item */
    @Comment("Price of the item")
    @Column(length=8, precision=2)
    public static final Key<$Item,BigDecimal> PRICE = f.newKeyDefault(BigDecimal.ZERO);
    /** A reference to common $Order */
    @Comment("A reference to the Order")
    @Column(name="fk_order")
    public static final Key<$Item,$Order> ORDER = f.newKey();

    // Lock the Key factory
    static { f.lock(); }

    private final InternalUjo internal = new InternalUjo();

    /** Basic data */
    private final Item data;

    /** Constructor */
    public $Item(Item pojo) {
        data = pojo;
    }

    /** Constructor */
    public $Item() {
        this(null);
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key<?, ?> key, Object value) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends Ujo> KeyList<U> readKeys() {
        return (KeyList<U>) readKeyList();
    }

    @Override
    public KeyList<$Item> readKeyList() {
        return f.getKeys();
    }

    /** Read an ORM session where the session is an transient key. */
    public Session readSession() {
        return internal.readSession();
    }

    /** Write an ORM session. */
    public void writeSession(Session session) {
        internal.writeSession(session);
    }

    /**
     * Returns keys of changed values in a time when any <strong>session</strong> is assigned.
     * The method is used by a SQL UPDATE statement to update assigned values only.
     * Implementation tip: create a new key type of {@link Set<Key>}
     * and in the method writeValue assign the current Key always.
     * @param clear True value clears all the key changes.
     * @return Key array of the modified values.
     */
    public Key[] readChangedProperties(boolean clear) {
         return internal.readChangedProperties(clear);
    }

    /** Get an original foreign key for an internal use only.
     * The {@code non null} value means the undefined object properties of the current object.
     * @return An original foreign key can be {@code nullable} */
    public ForeignKey readInternalFK() {
        return internal.readInternalFK();
    }

    /** A method to a foreign key for an internal use only.
     * @param fk New key to assign can be {@code null} */
    public void writeInternalFK(ForeignKey fk) {
        internal.writeInternalFK(fk);
    }

    @Override
    public ForeignKey readFK(Key key) throws IllegalStateException {
        return internal.readFK(this, readValue(key), key);
    }

    @Override
    public <VALUE> VALUE get(Key<? super $Item, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> Ujo set(Key<? super $Item, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super $Item, VALUE> key) {
         return key.getList(this);
    }

    @Override
    public String getText(Key key) {
        return UjoManager.getInstance().getText(this, key, null);
    }

    @Override
    public void setText(Key key, String value) {
        UjoManager.getInstance().setText(this, key, value, null, null);
    }

    @Override
    public Object readValue(Key<?, ?> key) {
        if (this.data != null) {
            switch (key.getIndex()) {
                case 0: return data.getId();
                case 1: return data.getUserId();
                case 2: return data.getNote();
                case 3: return data.getPrice();
                case 4: return data.getOrder();
            }
        } else {
            switch (key.getIndex()) {
                case 0: return super.getId();
                case 1: return super.getUserId();
                case 2: return super.getNote();
                case 3: return super.getPrice();
                case 4: return super.getOrder();
            }
        }
        throw new UnsupportedKey(key);
    }

    @Override
    public void writeValue(Key<?, ?> key, Object value) {
       if (this.data != null) {
            switch (key.getIndex()) {
                case 0: data.setId((Long) value); return;
                case 1: data.setUserId((Integer) value); return;
                case 2: data.setNote((String) value); return;
                case 3: data.setPrice((BigDecimal) value); return;
                case 4: data.setOrder((Order)value); return;
            }
        } else {
            switch (key.getIndex()) {
                case 0: super.setId((Long) value); return;
                case 1: super.setUserId((Integer) value); return;
                case 2: super.setNote((String) value); return;
                case 3: super.setPrice((BigDecimal) value); return;
                case 4: super.setOrder((Order)value); return;
            }
        }
        throw new UnsupportedKey(key);
    }


    // --- Getters and Setters ---

    /** Unique key */
    public Long getId() {
        return ID.of(this);
    }

    /** Unique key */
    public void setId(Long id) {
        ID.setValue(this, id);
    }

    /** User key */
    public Integer getUserId() {
        return USER_ID.of(this);
    }

    /** User key */
    public void setUserId(Integer userId) {
        USER_ID.setValue(this, userId);
    }

    /** Description of the $Item */
    public String getNote() {
        return NOTE.of(this);
    }

    /** Description of the $Item */
    public void setNote(String note) {
        NOTE.setValue(this, note);
    }

    /** Price of the item */
    public BigDecimal getPrice() {
        return PRICE.of(this);
    }

    /** Price of the item */
    public void setPrice(BigDecimal price) {
        PRICE.setValue(this, price);
    }

    /** A reference to common $Order */
    public Order getOrder() {
        return ORDER.of(this);
    }

    /** A reference to common $Order */
    public void setOrder(Order order) {
        ORDER.setValue(this, ($Order) CONVERTER.marshal(order));
    }

}
