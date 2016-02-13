/*
 *  Copyright 2009-2014 Pavel Ponec
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

package org.ujorm.implementation.orm;

import java.util.HashSet;
import java.util.Set;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.quick.QuickUjo;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;
import static org.ujorm.extensions.Property.UNDEFINED_INDEX;

/**
 * This abstract implementation of the OrmUjo interface is situable
 * for implementation the persistent entities.
 * <br>Instances of the OrmTable can be serializable, but you need to know that only
 * business data will be transferred, the session and property changes will not be passed.
 * <br>The sample of use:
 * <pre class="pre">
 *  <span class="comment">&#47;** Using INSERT *&#47;</span>
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> useCreateItem() {
 *
 *      Order order = <span class="keyword-directive">new</span> Order();
 *      order.set(Order.DATE, <span class="keyword-directive">new</span> Date());
 *      order.set(Order.DESCR, <span class="character">&quot;John's order&quot;</span>);
 *
 *      Item item = <span class="keyword-directive">new</span> Item();
 *      item.set(Item.ORDER, order);
 *      item.set(Item.DESCR, <span class="character">&quot;Yellow table&quot;</span>);
 *
 *      Session session = OrmHandler.getInstance().getSession();
 *      session.save(order);
 *      session.save(item);
 *      session.commit();
 *  }</pre>
 *
 * @author Pavel Ponec
 * @param <U> The current BO type
 * @see org.ujorm.implementation.orm.RelationToMany
 * @see org.ujorm.core.UjoIterator
 */
public abstract class OrmTable<U extends OrmTable> extends QuickUjo implements ExtendedOrmUjo<U> {

    /** An empty array of the UJO keys */
    @PackagePrivate
    static final Key[] EMPTY = new Key[0];
    /** Foreign key where a non null value is request to a lazy loading */
    private ForeignKey foreignKey;
    /** ORM session */
    transient private Session session;
    /** Set of changes */
    transient private Set<Key> changes = null;

    public OrmTable() {
    }

    /** Read a session */
    @Override
    public Session readSession() {
        return session;
    }

    /** Write a session */
    @Override
    public void writeSession(Session session) {
        this.session = session;
    }

    /** A method for an internal use only. */
    @Override
    public void writeValue(Key<?,?> key, Object value) {
        if (session!=null) {
            if (changes==null) {
                changes = new HashSet<Key>(8);
            }
            changes.add(key);
        }
        super.writeValue(key, value);
    }

    /**
     * Returns keys of changed values in a time when any <strong>session</strong> is assigned.
     * The method is used by a SQL UPDATE statement to update assigned values only.
     * Implementation tip: create a new property type of {@link Set<Key>}
     * and in the method writeValue assign the current Key always.
     * @param clear True value clears all the key changes.
     * @return Key array of the modified values.
     */
    @Override
    public Key[] readChangedProperties(boolean clear) {
        final Key[] result
            = (changes==null || changes.isEmpty())
            ? EMPTY
            : changes.toArray(new Key[changes.size()])
            ;
        if (clear) {
            changes = null;
        }
        return result;
    }

    /** Getter based on Key implemented by a pattern UjoExt */
    @SuppressWarnings("unchecked")
    public final <UJO extends U, VALUE> VALUE get(final Key<UJO, VALUE> key) {
        final VALUE result = key.of((UJO)this);
        return result;
    }

    /** Setter  based on Key. Type of value is checked in the runtime.
     * The method was implemented by a pattern UjoExt.
     */
    @SuppressWarnings({"unchecked"})
    public final <UJO extends U, VALUE> U set
        ( final Key<UJO, VALUE> key
        , final VALUE value
        ) {
        UjoManager.assertAssign(key, value);
        key.setValue((UJO)this, value);
        return (U) this;
    }

    /** Test an authorization of the action. */
    @Override
    public boolean readAuthorization(UjoAction action, Key key, Object value) {
        switch (action.getType()) {
            case UjoAction.ACTION_TO_STRING:
                return !(key instanceof RelationToMany);
            default:
                return super.readAuthorization(action, key, value);
        }
    }

    /** Read the original foreign key, if any
     * @return Return an original key */
    @Override
    public ForeignKey readInternalFK() {
        return foreignKey;
    }

    /** Write the original foreign key or the {@code null}
     * @param fk New foreign key to assign */
    @Override
    public void writeInternalFK(final ForeignKey fk) {
        this.foreignKey = fk;
    }

    /** Read the foreign key.
     * This is useful to obtain the foreign key value without (lazy) loading the entire object.
     * If the lazy object is loaded, the method will need the Session to build the ForeignKey instance.
     * <br>NOTE: The method is designed for developers only, the Ujorm doesn't call it newer.
     * @param key Must be direct key only ({@link Key#isDirect()}==true)
     * @return If no related object is available, then the result has the NULL value.
     * @throws IllegalStateException Method throws an exception for a wrong key type.
     * @throws NullPointerException Method throws an exception if a Session is missing after a lazy initialization of the key.
     */
    @Override
    public <UJO extends U> ForeignKey readFK(Key<UJO, ? extends OrmUjo> key) throws IllegalStateException {
        final Object value = super.readValue(key);
        if (value==null || value instanceof ForeignKey) {
            return (ForeignKey) value;
        }
//      if (key instanceof RelationToOne) {
//          // TODO: fix the case the key is a relation:
//          final Key key = ((RelationToOne)key).getRelatedKey();
//          return value instanceof ExtendedOrmUjo
//                  ? ((ExtendedOrmUjo)value).readFK(key)
//                  : new ForeignKey(key.of((Ujo)value));
//      }
// Effectiva: toto se volá cyklicky a navíc se předává špatná key (z původního objektu místo z cizího), pak se vrací nesmysly
//      if (value instanceof ExtendedOrmUjo) {
//          return ((ExtendedOrmUjo) value).readFK(key);
//      }
        if (session!=null) {
            final OrmUjo ujo = value instanceof OrmUjo
                    ? (OrmUjo) value
                    : this ;
            return session.readFK(ujo, key);
        }
        throw new NullPointerException("Can't get FK by the key '"
                + key.getFullName()
                + "' due a missing Session");
    }

    // ===== STATIC METHODS: Key Facotory =====

    /** Create a factory with a camel-case Key name generator.
     * <br>Note: after declarations of all properties is recommend to call method {@code KeyFactory.close()};
     */
    protected static <UJO extends Ujo, FACTORY extends KeyFactory<UJO>> FACTORY newCamelFactory(Class<? extends UJO> ujoClass) {
        return (FACTORY) new OrmKeyFactory(ujoClass, true);
    }

    /** Create a base factory Key name generator where key name is the same as its field name.
     * <br>Note: after declarations of all properties is recommend to call method {@code KeyFactory.close()};
     */
    protected static <UJO extends Ujo, FACTORY extends KeyFactory<UJO>> FACTORY newFactory(Class<? extends UJO> ujoClass) {
        return (FACTORY) new OrmKeyFactory(ujoClass, false);
    }

    /** A PropertyIterator Factory creates an new key and assign a next index.
     * @hidden
     * @deprecated use the {@link #newRelation(java.lang.String)} instead of this.
     */
    @Deprecated
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation(String name, Class<ITEM> type) {
        return new RelationToMany<UJO,ITEM> (name, type, UNDEFINED_INDEX, false);
    }

    /** A PropertyIterator Factory creates an new key and assign a next index.
     * @hidden
     */
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation(String name) {
        return new RelationToMany<UJO,ITEM> (name, null, UNDEFINED_INDEX, false);
    }

    /** A PropertyIterator Factory creates an new key and assign a next index.
     * @hidden
     * @deprecated use the {@link #newRelation()} instead of this.
     */
    @Deprecated
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation(Class<ITEM> type) {
        return newRelation(null, type);
    }

    /** A PropertyIterator Factory creates an new key and assign a next index.
     * @hidden
     */
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation() {
        return newRelation(null, null);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey() {
        return new OrmProperty(UNDEFINED_INDEX);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey(String name) {
        return new OrmProperty(UNDEFINED_INDEX, name, null, null);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey(String name, VALUE defaultValue) {
        return new OrmProperty(UNDEFINED_INDEX, name, defaultValue, null);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKeyDefault(VALUE defaultValue) {
        return new OrmProperty(UNDEFINED_INDEX, null, defaultValue, null);
    }

    // --------- STATIC METHODS -------------------


    /** A Property Factory creates new key and assigns a next key index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey
    ( String name
    , Class<VALUE> type
    , VALUE defaultValue
    , int index
    , boolean lock
    ) {
        return (Property<UJO, VALUE>) new OrmProperty(index, name, defaultValue, null);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newKey
    ( VALUE value
    ) {
        return newKey(null, null, value, UNDEFINED_INDEX, false);
    }

    /** Returns a new instance of key where the default value is null.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends QuickUjo,VALUE> Property<UJO,VALUE> newKey(Key p) {
        return (Property<UJO, VALUE>) new OrmProperty(UNDEFINED_INDEX, p.getName(), p.getDefault(), null);
    }

    // ------------- DEPRECATED METHODS ---------------------

    /** A Property Factory creates new key and assigns a next key index.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use rather a method {@link QuickUjo#newProperty(java.lang.String)} instead of this.
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( String name
    , Class<VALUE> type
    ) {
        return newProperty(name, type, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use the method newKey(...)
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty(String name) {
        return newProperty(name, null, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use the method newKey(...)
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( String name
    , VALUE value
    ) {
        return newProperty(name, null, value, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use rather a method {@link QuickUjo#newProperty()} instead of this,
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( Class<VALUE> type
    ) {
        return newProperty(null, type, null, UNDEFINED_INDEX, false);
    }

    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( String name
    , Class<VALUE> type
    , VALUE defaultValue
    , int index
    , boolean lock
    ) {
        return new OrmProperty(index, name, defaultValue, null);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( VALUE value
    ) {
        return newProperty(null, null, value, UNDEFINED_INDEX, false);
    }

    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( Class<VALUE> type
      , VALUE value) {
        return newProperty(null, null, value, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br />Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty() {
        return newProperty(null, null, null, UNDEFINED_INDEX, false);
    }

}
