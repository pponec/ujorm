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

package org.ujoframework.implementation.orm;

import java.util.HashSet;
import java.util.Set;
import org.ujoframework.UjoAction;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoPropertyListImpl;
import org.ujoframework.implementation.quick.QuickUjo;
import org.ujoframework.orm.ExtendedOrmUjo;
import org.ujoframework.orm.ForeignKey;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Session;

/**
 * This abstract implementation of the OrmUjo interface is situable
 * for implementation the persistent entities.
 * <br>Instances of the OrmTable are serializable, but you need to know that only 
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
 * @see org.ujoframework.implementation.orm.RelationToMany
 * @see org.ujoframework.core.UjoIterator
 */
public class OrmTable<UJO_IMPL extends Ujo> extends QuickUjo implements ExtendedOrmUjo<UJO_IMPL> {

    /** Orm session */
    transient private Session session;
    /** Set of changes */
    transient private Set<UjoProperty> changes = null;

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
    public void writeValue(UjoProperty property, Object value) {
        if (session!=null) {
            if (changes==null) {
                changes = new HashSet<UjoProperty>(8);
            }
            changes.add(property);
        }
        super.writeValue(property, value);
    }


    /** A method for an internal use only. */
    @Override
    public Object readValue(final UjoProperty property) {
        Object result = super.readValue(property);

        if (property.isTypeOf(OrmUjo.class)) {
            if (result instanceof ForeignKey) {
                result = session.loadInternal(property, ((ForeignKey)result).getValue(), true);
                super.writeValue(property, result);
            }
            else
            if (result!=null
            && session!=null
            && session!=((OrmUjo)result).readSession()
            ){
                // Write the current session to a related object:
                ((OrmUjo)result).writeSession(session);
            }
        } else
        if (property instanceof RelationToMany
        &&  session!=null
        &&  session.getHandler().isPersistent(property)
        ){
            result = session.iterateInternal( (RelationToMany) property, this);
            // Don't save the result!
        }
        return result;
    }

    /** Returns a changed properties. The method is not the thread save.
     * @param clear True value clears the property changes.
     */
    @Override
    public UjoProperty[] readChangedProperties(boolean clear) {
        final UjoProperty[] result
            = changes!=null
            ? changes.toArray(new UjoProperty[changes.size()])
            : UjoPropertyListImpl.EMPTY
            ;
        if (clear) {
            changes = null;
        }
        return result;
    }


    /** Getter based on UjoProperty implemeted by a pattern UjoExt */
    @SuppressWarnings("unchecked")
    public final <UJO extends UJO_IMPL, VALUE> VALUE get(final UjoProperty<UJO, VALUE> property) {
        final VALUE result = property.of((UJO)this);
        return result;
    }

    /** Setter  based on UjoProperty. Type of value is checked in the runtime.
     * The method was implemented by a pattern UjoExt.
     */
    @SuppressWarnings({"unchecked"})
    public final <UJO extends UJO_IMPL, VALUE> UJO_IMPL set
        ( final UjoProperty<UJO, VALUE> property
        , final VALUE value
        ) {
        readUjoManager().assertAssign(property, value);
        property.setValue((UJO)this, value);
        return (UJO_IMPL) this;
    }

    /** Test an authorization of the action. */
    @Override
    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
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
     * @return If no related object is available, then the result has the NULL value.
     * @throws IllegalStateException Method throws an exception for a wrong property type.
     * @throws NullPointerException Method throws an exception if a Session is missing after a lazy initialization of the property.
     */
    @Override
    public <UJO extends UJO_IMPL> ForeignKey readFK(UjoProperty<UJO, ? extends OrmUjo> property) throws IllegalStateException {
        Object value = super.readValue(property);
        if (value==null || value instanceof ForeignKey) {
            return (ForeignKey) value;
        } else if (session!=null) {
            return session.readFK(this, property);
        }
        throw new NullPointerException("Can't get FK form the property '"+property+"' due the missing Session");
    }

    // --------- STATIC METHODS -------------------

    /** A PropertyIterator Factory creates an new property and assign a next index.
     * @hidden
     */
    protected static <UJO extends OrmTable, ITEM extends OrmTable> RelationToMany<UJO,ITEM> newRelation(String name, Class<ITEM> type) {
        return new RelationToMany<UJO,ITEM> (name, type, -1, false);
    }

    /** A PropertyIterator Factory creates an new property and assign a next index.
     * @hidden
     */
    protected static <UJO extends OrmTable, ITEM extends OrmTable> RelationToMany<UJO,ITEM> newRelation(Class<ITEM> type) {
        return newRelation(null, type);
    }


}
