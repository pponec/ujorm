/*
 *  Copyright 2009-2010 Pavel Ponec
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
import org.ujorm.core.UjoPropertyListImpl;
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
 * <ul>
 * See the {@link OrmTable} javadoc for basic information.
 * @author Pavel Ponec
 * @see OrmTable
 */
public class OrmTableSynchronized<UJO_IMPL extends Ujo> extends QuickUjo implements ExtendedOrmUjo<UJO_IMPL> {

    /** Orm session */
    transient private ThreadLocal<Session> session;
    /** Set of changes */
    transient private Set<Key> changes = null;

    public OrmTableSynchronized() {
    }

    /** Read a session */
    @Override
    public Session readSession() {
        return session!=null ? session.get() : null ;
    }

    /** Write a session */
    @Override
    public void writeSession(Session session) {
        if (this.session==null) {
            if (session==null) {
                return;
            }
            this.session = new ThreadLocal<Session>();
        }
        this.session.set(session);
    }

    /** A method for an internal use only. */
    @Override
    synchronized public void writeValue(Key property, Object value) {
        if (readSession()!=null) {
            if (changes==null) {
                changes = new HashSet<Key>(8);
            }
            changes.add(property);
        }
        super.writeValue(property, value);
    }

    /** A method for an internal use only. */
    @Override
    synchronized public Object readValue(final Key property) {
        return super.readValue(property);
    }

    /** Returns a changed keys. The method is not the thread save.
     * @param clear True value clears the property changes.
     */
    @Override
    synchronized public Key[] readChangedProperties(boolean clear) {
        final Key[] result
            = changes==null || changes.isEmpty()
            ? UjoPropertyListImpl.EMPTY
            : changes.toArray(new Key[changes.size()])
            ;
        if (clear) {
            changes = null;
        }
        return result;
    }


    /** Getter based on Key implemeted by a pattern UjoExt */
    @SuppressWarnings("unchecked")
    public final <UJO extends UJO_IMPL, VALUE> VALUE get(final Key<UJO, VALUE> property) {
        final VALUE result = property.of((UJO)this);
        return result;
    }

    /** Setter  based on Key. Type of value is checked in the runtime.
     * The method was implemented by a pattern UjoExt.
     */
    @SuppressWarnings({"unchecked"})
    public final <UJO extends UJO_IMPL, VALUE> UJO_IMPL set
        ( final Key<UJO, VALUE> property
        , final VALUE value
        ) {
        readUjoManager().assertAssign(property, value);
        property.setValue((UJO)this, value);
        return (UJO_IMPL) this;
    }

    /** Test an authorization of the action. */
    @Override
    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        switch (action.getType()) {
            case UjoAction.ACTION_TO_STRING:
                return !(property instanceof RelationToMany);
            default:
                return super.readAuthorization(action, property, value);
        }
    }

    /** Read the foreign key.
     * This is useful to obtain the foreign key value without (lazy) loading the entire object.
     * If the lazy object is loaded, the method will need the Session to build the ForeignKey instance.
     * <br>NOTE: The method is designed for developers only, the Ujorm doesn't call it newer.
     * @param property Must be direct property only ({@link Key#isDirect()}==true)
     * @return If no related object is available, then the result has the NULL value.
     * @throws IllegalStateException Method throws an exception for a wrong property type.
     * @throws NullPointerException Method throws an exception if a Session is missing after a lazy initialization of the property.
     */
    @Override
    synchronized public <UJO extends UJO_IMPL> ForeignKey readFK(Key<UJO, ? extends OrmUjo> property) throws IllegalStateException {
        final Object value = super.readValue(property);
        if (value==null || value instanceof ForeignKey) {
            return (ForeignKey) value;
        }
        if (value instanceof ExtendedOrmUjo) {
            return ((ExtendedOrmUjo) value).readFK(property);
        }
        final Session session = readSession();
        if (session!=null) {
            final OrmUjo ujo = value instanceof OrmUjo
                    ? (OrmUjo) value
                    : this ;
            return session.readFK(ujo, property);
        }
        throw new NullPointerException("Can't get FK form the property '"+property+"' due the missing Session");
    }

    // ===== STATIC METHODS: Key Facotory =====

    /** Create a factory with a cammel-case Key name generator.
     * <br>Note: after declarations of all properties is recommend to call method {@code KeyFactory.close()};
     */
    protected static <UJO extends Ujo, FACTORY extends KeyFactory<UJO>> FACTORY newCamelFactory(Class<? extends UJO> ujoClass) {
        return (FACTORY) new OrmKeyFactory(ujoClass, true);
    }

    /** Create a base factory Key name generator where property name is the same as its field name.
     * <br>Note: after declarations of all properties is recommend to call method {@code KeyFactory.close()};
     * <br>In case of OrmUjo the method is called by a Ujorm framework, so the newCamelFactory
     */
    protected static <UJO extends Ujo, FACTORY extends KeyFactory<UJO>> FACTORY newFactory(Class<? extends UJO> ujoClass) {
        return (FACTORY) new OrmKeyFactory(ujoClass, false);
    }

    /** A PropertyIterator Factory creates an new property and assign a next index.
     * @hidden
     * @deprecated use the {@link #newRelation(java.lang.String)} instead of this.
     */
    @Deprecated
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation(String name, Class<ITEM> type) {
        return new RelationToMany<UJO,ITEM> (name, type, UNDEFINED_INDEX, false);
    }

    /** A PropertyIterator Factory creates an new property and assign a next index.
     * @hidden
     */
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation(String name) {
        return new RelationToMany<UJO,ITEM> (name, null, UNDEFINED_INDEX, false);
    }

    /** A PropertyIterator Factory creates an new property and assign a next index.
     * @hidden
     * @deprecated use the {@link #newRelation()} instead of this.
     */
    @Deprecated
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation(Class<ITEM> type) {
        return newRelation(null, type);
    }

    /** A PropertyIterator Factory creates an new property and assign a next index.
     * @hidden
     */
    protected static <UJO extends ExtendedOrmUjo, ITEM extends ExtendedOrmUjo> RelationToMany<UJO,ITEM> newRelation() {
        return newRelation(null, null);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey() {
        return new OrmProperty(UNDEFINED_INDEX);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey(String name) {
        return new OrmProperty(UNDEFINED_INDEX, name, null, null);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey(String name, VALUE defaultValue) {
        return new OrmProperty(UNDEFINED_INDEX, name, defaultValue, null);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKeyDefault(VALUE defaultValue) {
        return new OrmProperty(UNDEFINED_INDEX, null, defaultValue, null);
    }

    // --------- STATIC METHODS -------------------


    /** A Property Factory creates new property and assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey
    ( String name
    , Class<VALUE> type
    , VALUE defaultValue
    , int index
    , boolean lock
    ) {
        return Property.newInstance(name, type, defaultValue, index, lock);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newKey
    ( VALUE value
    ) {
        return newKey(null, null, value, UNDEFINED_INDEX, false);
    }

    /** Returns a new instance of property where the default value is null.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends QuickUjo,VALUE> Property<UJO,VALUE> newKey(Key p) {
        return Property.newInstance(p.getName(), p.getType(), p.getDefault(), -1, false);
    }

    // ------------- DEPRECATED METHODS ---------------------

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
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

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use the method newKey(...)
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty(String name) {
        return newProperty(name, null, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
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

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
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

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( VALUE value
    ) {
        return newProperty(null, null, value, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty() {
        return newProperty(null, null, null, UNDEFINED_INDEX, false);
    }

}
