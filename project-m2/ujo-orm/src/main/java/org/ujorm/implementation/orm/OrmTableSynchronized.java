/*
 *  Copyright 2020-2026 Pavel Ponec
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.quick.QuickUjo;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;
import static org.ujorm.extensions.Property.UNDEFINED_INDEX;

/**
 * It is an thread safe implementation of the OrmUjo for the <strong>multi-thread use</strong>.
 * The special feature are:
 * <ul>
 *   <li>some critical method are synchronized</li>
 *   <li>Session is saved to the ThreadLocal instance so object in different thread must assign a new Session</li>
 * </ul>
 * See the {@link OrmTable} javadoc for basic information.
 * @author Pavel Ponec
 * @see OrmTable
 */
public abstract class OrmTableSynchronized<U extends OrmTableSynchronized> extends QuickUjo implements ExtendedOrmUjo<U> {

    /** ORM session */
    transient private ThreadLocal<Session> session;
    /** Set of changes */
    transient private BitSet changes = null;

    /** Read a session */
    @Override
    public Session readSession() {
        return session != null ? session.get() : null ;
    }

    /** Write a session */
    @Override
    public void writeSession(Session session) {
        if (this.session == null) {
            if (session == null) {
                return;
            }
            this.session = new ThreadLocal<>();
        }
        this.session.set(session);
    }

    /** A method for an internal use only. */
    @Override
    synchronized public void writeValue(Key key, Object value) {
        if (readSession() != null) {
            if (changes == null) {
                changes = new BitSet();
            }
            changes.flip(key.getIndex());
        }
        super.writeValue(key, value);
    }

    /** A method for an internal use only. */
    @Override
    synchronized public Object readValue(final Key key) {
        return super.readValue(key);
    }

    /**
     * Returns keys of changed values in a time when any <strong>session</strong> is assigned.
     * The method is used by a SQL UPDATE statement to update assigned values only.
     * Implementation tip: create a new key type of {@link Set<Key>}
     * and in the method writeValue assign the current Key always.
     * @param clear True value clears all the key changes.
     * @return Key array of the modified values.
     */
    @Override
    public synchronized Key[] readChangedProperties(boolean clear) {
        final KeyList<U> keys = readKeys();
        final ArrayList<Key> result = new ArrayList<>(keys.size());
        for (Key<U, ?> key : keys) {
            if (checkModificationFlag(key)) {
                result.add(key);
            }
        }
        if (clear) {
            clearModificationFlags();
        }
        return result.toArray(new Key[result.size()]);
    }

    /** Check the attribute modification flag */
    @Override
    public synchronized boolean checkModificationFlag(@NotNull final Key key) {
        return changes != null && changes.get(key.getIndex());
    }

    /** Type safe checking the modification flag */
    public synchronized final <UJO extends U, VALUE> boolean checkModificationFlagSafe(final @NotNull Key<UJO, VALUE> key) {
        return checkModificationFlag(key);
    }

    @Override
    public synchronized void clearModificationFlags() {
        if (changes != null) {
            changes.clear();
        }
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
        if (action.getType() == UjoAction.ACTION_TO_STRING) {
            return !(key instanceof RelationToMany);
        }
        return super.readAuthorization(action, key, value);
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
    synchronized public <UJO extends U> ForeignKey readFK(Key<UJO, ? extends OrmUjo> key) throws IllegalStateException {
        final Object value = super.readValue(key);
        if (value==null || value instanceof ForeignKey) {
            return (ForeignKey) value;
        }
        final Session session = readSession();
        if (session!=null) {
            final OrmUjo ujo = value instanceof OrmUjo
                    ? (OrmUjo) value
                    : this ;
            return session.readFK(ujo, key);
        }
        throw new NullPointerException("Can't get FK form the key '"+key+"' due the missing Session");
    }

    /** Clone the first level */
    @Override @NotNull
    public U cloneUjo() {
        return (U) clone(1, null);
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
     * <br>In case of OrmUjo the method is called by a Ujorm framework, so the newCamelFactory
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
        return new RelationToMany<> (name, type, UNDEFINED_INDEX, false);
    }

    /** A PropertyIterator Factory creates an new key and assign a next index.
     * @hidden
     */
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation(String name) {
        return new RelationToMany<> (name, null, UNDEFINED_INDEX, false);
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
        return Property.of(name, type, defaultValue, index, lock);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br>Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newKey
    ( VALUE value
    ) {
        return newKey(null, null, value, UNDEFINED_INDEX, false);
    }

    /** Returns a new instance of key where the default value is null.
     * <br>Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends QuickUjo,VALUE> Property<UJO,VALUE> newKey(Key<UJO,VALUE> p) {
        return Property.of(p.getName(), p.getType(), p.getDefault(), -1, false);
    }

    // ------------- DEPRECATED METHODS ---------------------

    /** A Property Factory creates new key and assigns a next key index.
     * <br>Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
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
     * <br>Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use the method newKey(...)
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty(String name) {
        return newProperty(name, null, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br>Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
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
     * <br>Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
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
     * <br>Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( VALUE value
    ) {
        return newProperty(null, null, value, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new key and assigns a next key index.
     * <br>Warning: Method does not lock the key so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty() {
        return newProperty(null, null, null, UNDEFINED_INDEX, false);
    }

}
