/* Powered by the Ujorm, don't modify it */

package org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated;

import java.util.List;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UnsupportedKey;
import org.ujorm.extensions.UjoMiddle;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.InternalUjo;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.Session;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.View;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.ViewOrder;

/**
 * The column mapping to FROM view.
 * @hidden
 */
@View(select="SELECT * FROM ( "
    + "SELECT ord_order_alias.id"
    +         ", count(*) AS item_count"
    + " FROM ${SCHEMA}.ord_order ord_order_alias"
    + " LEFT JOIN ${SCHEMA}.ord_item ord_item_alias"
    + " ON ord_order_alias.id = ord_item_alias.fk_order"
    + " WHERE ord_item_alias.id>=? "
    + " GROUP BY ord_order_alias.id"
    + " ORDER BY ord_order_alias.id"
    + ") testView WHERE true"
    , alias="testView"
    )

//  /* MSSQL query */
//  @View(SELECT * FROM ( "
//  + " SELECT ord_order_alias.id, count(*) AS item_count"
//  + " FROM db1.dbo.ord_order ord_order_alias"
//  +     ", db1.dbo.ord_item  ord_item_alias"
//  + " WHERE ord_order_alias.id>=?
//  +   " AND ord_order_alias.id = ord_item_alias.fk_order"
//  + " GROUP BY ord_order_alias.id"
//  + " ORDER BY ord_order_alias.id")
//  + ") testView WHERE true"
//  , alias="testView"
//  )

 public class $ViewOrder extends ViewOrder implements UjoMiddle<$ViewOrder>, ExtendedOrmUjo {
    private static final OrmKeyFactory<$ViewOrder> f = new OrmKeyFactory($ViewOrder.class, true);

    /** Unique key */
    @Column(pk=true, name="id")
    public static final Key<$ViewOrder, Long> ID = f.newKey();
    /** ItemCount */
    @Column(name="item_count")
    public static final Key<$ViewOrder,Integer> ITEM_COUNT = f.newKeyDefault(0);

   // Lock the Key factory
    static { f.lock(); }

    private final InternalUjo internal = new InternalUjo();

    /** Basic data */
    private final ViewOrder data;

    /** Constructor */
    public $ViewOrder(ViewOrder pojo) {
        data = pojo;
    }

    /** Constructor */
    public $ViewOrder() {
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
    public KeyList<$ViewOrder> readKeyList() {
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
    public <VALUE> VALUE get(Key<? super $ViewOrder, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> Ujo set(Key<? super $ViewOrder, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super $ViewOrder, VALUE> key) {
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
                case 1: return data.getItemCount();
            }
        } else {
            switch (key.getIndex()) {
                case 0: return super.getId();
                case 1: return super.getItemCount();
            }
        }
        throw new UnsupportedKey(key);
    }

    @Override
    public void writeValue(Key<?, ?> key, Object value) {
       if (this.data != null) {
            switch (key.getIndex()) {
                case 0: data.setId((Long) value); return;
                case 1: data.setItemCount((Integer) value); return;
            }
        } else {
            switch (key.getIndex()) {
                case 0: super.setId((Long) value); return;
                case 1: super.setItemCount((Integer) value); return;
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

    /** ItemCount */
    public Integer getItemCount() {
        return ITEM_COUNT.of(this);
    }

    /** ItemCount */
    public void setItemCount(Integer itemCount) {
        ITEM_COUNT.setValue(this, itemCount);
    }


}
