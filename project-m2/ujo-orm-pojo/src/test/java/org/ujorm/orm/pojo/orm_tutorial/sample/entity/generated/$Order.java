/* Powered by the Ujorm, don't modify it */

package org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated;

import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.UjoIterator;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UnsupportedKey;
import org.ujorm.core.annot.Transient;
import org.ujorm.extensions.UjoMiddle;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.DbType;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.InternalUjo;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.Session;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Customer;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Item;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Order;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Order.State;
import static org.ujorm.orm.InternalUjo.CONVERTER;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the $Order object has got an collection of Items.
 * @hidden
 */
@Comment("Order table for registering the 'order items'")
@Table(name = "ord_order")
public final class $Order extends Order implements UjoMiddle<$Order>, ExtendedOrmUjo {
    private static final OrmKeyFactory<$Order> f = new OrmKeyFactory($Order.class, true);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<$Order, Long> ID = f.newKey();
    /** $Order STATE, default is ACTIVE */
    @Comment("Order state, default value is ACTIVE")
    public static final Key<$Order, State> STATE = f.newKeyDefault(State.ACTIVE);
    /** User key */
    public static final Key<$Order, Integer> USER_ID = f.newKey();
    /** Description of the $Order */
    @Comment("Description of the Order")
    @Column(type = DbType.VARCHAR, name = "NOTE", mandatory = true)
    public static final Key<$Order, String> NOTE = f.newKey();
    /** Date of creation */
    public static final Key<$Order, Date> CREATED = f.newKey();
    /** Text file */
    @Transient
    public static final Key<$Order, Clob> TEXT_FILE = f.newKey();
    /** Binary file */
    @Transient
    public static final Key<$Order, Blob> BINARY_FILE = f.newKey();
    /** Reference to Items */
    public static final RelationToMany<$Order, $Item> ITEMS = f.newRelation();
    /** $Customer */
    @Column(name="fk_customer") public static final Key<$Order, $Customer> CUSTOMER = f.newKey();
    @Column(mandatory=true) public static final Key<$Order, Integer> NEW_COLUMN = f.newKeyDefault(777);

    // Lock the factory:
    static {  f.lock(); }

    private final InternalUjo internal = new InternalUjo();

    /** Basic data */
    private final Order data;

    /** Constructor */
    public $Order(Order pojo) {
        data = pojo;
    }

    /** Constructor */
    public $Order() {
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
    public KeyList<$Order> readKeyList() {
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
    public <VALUE> VALUE get(Key<? super $Order, VALUE> key) {
        return key.of(this);
    }

    @Override
    public <VALUE> Ujo set(Key<? super $Order, VALUE> key, VALUE value) {
        key.setValue(this, value);
        return this;
    }

    @Override
    public <VALUE> List<VALUE> getList(ListKey<? super $Order, VALUE> key) {
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
        final Object result = _readValue(key);
        if (result == null) {
            return result;
        }
        if (key.isTypeOf(Ujo.class) && !(result instanceof Ujo)) {
            return CONVERTER.marshal(result);
        } else {
            return result;
        }
    }

    protected Object _readValue(Key<?, ?> key) {
        if (this.data != null) {
            switch (key.getIndex()) {
                case 0: return data.getId();
                case 1: return data.getState();
                case 2: return data.getUserId();
                case 3: return data.getNote();
                case 4: return data.getCreated();
                case 5: return data.getTextFile();
                case 6: return data.getBinaryFile();
                case 7: return data.getItems();
                case 8: return data.getCustomer();
                case 9: return data.getNewColumn();
            }
        } else {
            switch (key.getIndex()) {
                case 0: return super.getId();
                case 1: return super.getState();
                case 2: return super.getUserId();
                case 3: return super.getNote();
                case 4: return super.getCreated();
                case 5: return super.getTextFile();
                case 6: return super.getBinaryFile();
                case 7: return super.getItems();
                case 8: return super.getCustomer();
                case 9: return super.getNewColumn();
            }
        }
        throw new UnsupportedKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeValue(Key<?, ?> key, Object value) {
       if (this.data != null) {
            switch (key.getIndex()) {
                case 0: data.setId((Long) value); return;
                case 1: data.setState((State) value); return;
                case 2: data.setUserId((Integer) value); return;
                case 3: data.setNote((String) value); return;
                case 4: data.setCreated((Date) value); return;
                case 5: data.setTextFile((Clob) value); return;
                case 6: data.setBinaryFile((Blob) value); return;
                case 7: data.setItems((List<Item>) value); return;
                case 8: data.setCustomer((Customer) value); return;
                case 9: data.setNewColumn((Integer) value); return;
            }
        } else {
            switch (key.getIndex()) {
                case 0: super.setId((Long) value); return;
                case 1: super.setState((State) value); return;
                case 2: super.setUserId((Integer) value); return;
                case 3: super.setNote((String) value); return;
                case 4: super.setCreated((Date) value); return;
                case 5: super.setTextFile((Clob) value); return;
                case 6: super.setBinaryFile((Blob) value); return;
                case 7: super.setItems((List<Item>) value); return;
                case 8: super.setCustomer((Customer) value); return;
                case 9: super.setNewColumn((Integer) value); return;
            }
        }
        throw new UnsupportedKey(key);
    }

    // --- Getters and Setters ---

    /** The Primary Key */
    public Long getId() {
        return ID.of(this);
    }

    /** The Primary Key */
    public void setId(Long id) {
        ID.setValue(this, id);
    }

    /** $Order STATE, default is ACTIVE */
    public State getState() {
        return STATE.of(this);
    }

    /** $Order STATE, default is ACTIVE */
    public void setState(State state) {
        STATE.setValue(this, state);
    }

    /** User key */
    public Integer getUserId() {
        return USER_ID.of(this);
    }

    /** User key */
    public void setUserId(Integer userId) {
        USER_ID.setValue(this, userId);
    }

    /** Description of the $Order */
    public String getNote() {
        return NOTE.of(this);
    }

    /** Description of the $Order */
    public void setNote(String note) {
        NOTE.setValue(this, note);
    }

    /** Date of creation */
    public Date getCreated() {
        return CREATED.of(this);
    }

    /** Date of creation */
    public void setCreated(Date created) {
        CREATED.setValue(this, created);
    }

    /** Text file */
    public Clob getTextFile() {
        return TEXT_FILE.of(this);
    }

    /** Text file */
    public void setTextFile(Clob textFile) {
        TEXT_FILE.setValue(this, textFile);
    }

    /** Binary file */
    public Blob getBinaryFile() {
        return BINARY_FILE.of(this);
    }

    /** Binary file */
    public void setBinaryFile(Blob binaryFile) {
        BINARY_FILE.setValue(this, binaryFile);
    }

    public List<Item> getItems() {
        final ArrayList<Item> result = new ArrayList<Item>();
        for ($Item item : ITEMS.getValue(this)) {
            result.add(item);
        }
        return result;
    }

    /** Binary file */
    public void setItems(List<Item> items) {
        final List<$Item> uItems = new ArrayList<$Item>(items.size());
        for (Item item : items) {
            uItems.add(($Item) CONVERTER.marshal(item));
        }
        ITEMS.setValue(this, UjoIterator.of(uItems));
    }

    /** $Customer */
    public $Customer getCustomer() {
        return CUSTOMER.of(this);
    }

    /** $Customer */
    public void setCustomer($Customer customer) {
        CUSTOMER.setValue(this, customer);
    }

    public Integer getNewColumn() {
        return NEW_COLUMN.of(this);
    }

    public void setNewColumn(Integer newColumn) {
        NEW_COLUMN.setValue(this, newColumn);
    }

}
