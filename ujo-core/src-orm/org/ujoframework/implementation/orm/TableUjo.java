/*
 *  Copyright 2009 Paul Ponec
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
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.UniqueKey;
import org.ujoframework.orm.Session;

/**
 * A simple implementation of an ORM solution. Why a new ORM mapping?
 * <ul>
 *    <li>framework has a type safe query language which allows the java compiler find a syntax error similar like a 4GL language</li>
 *    <li>never more a LazyInitialization exception though a lazy initialization is supported</li>
 *    <li>no confusing proxy business objects</li>
 *    <li>no list properties are supported but a special object called <a href="../../core/UjoIterator.html">UjoIterator</a> is designed for a collection. The UjoIterator provides a toList() method however</li>
 *    <li>easy to configure the ORM model by java source code, optionaly by annotations or a XML file</li>
 *    <li>small framework without more library dependences</li>
 * </ul>
 * Some other features:
 * <ul>
 *    <li>all persistent objects are based on the Ujo interface, namely on a TableUjo implementation</li>
 *    <li>resources for ORM mapping can be a database table, view, or your own SQL SELECT</li>
 *    <li>default ORM mapping is based on the UjoProperty names however there is possible overwrite the mapping by annotations and the annoatations can be owerwrited by a XML files </li>
 *    <li>JDBC query parameters are passed by a question notation to the PreparedStatement for a hight security</li>
 *    <li>restricted cache is suitable for a large transactions and a selecting uncommited changes</li>
 *    <li>the API was inspired by a Cayenne and Hibernate ORM frameworks</li>
 * </ul>
 * The sample of use:
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
 *  }
 *
 *
 *  <span class="comment">&#47;** Using SELECT by QUERY *&#47;</span>
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> useSelection() {
 *
 *      Criterion&lt;Order&gt; crn1 = Criterion.newInstance(Order.DESCR, <span class="character">&quot;John's order&quot;</span>);
 *      Criterion&lt;Order&gt; crn2 = Criterion.newInstance(Order.DATE, Operator.LE, <span class="keyword-directive">new</span> Date());
 *      Criterion&lt;Order&gt; crit = crn1.and(crn2);
 *
 *      Session session = OrmHandler.getInstance().getSession();
 *      UjoIterator&lt;Order&gt; orders = session.createQuery(crit).iterate();
 *
 *      <span class="keyword-directive">for</span> (Order order : orders ) {
 *          Long id = order.get(Order.ID);
 *          String descr = order.get(Order.DESCR);
 *          System.out.println(<span class="character">&quot;</span><span class="character">Order id: </span><span class="character">&quot;</span> + id + <span class="character">&quot;</span><span class="character"> descr: </span><span class="character">&quot;</span> + descr);
 *      }
 *  }
 *
 *  <span class="comment">&#47;** Using SELECT by an object relation(s) *&#47;</span>
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> useRelation() {
 *
 *      Session session = OrmHandler.getInstance().getSession();
 *      BoDatabase db = session.getDatabase();
 *      UjoIterator&lt;Order&gt; orders = db.get(BoDatabase.ORDERS);
 *
 *      <span class="keyword-directive">for</span> (Order order : orders) {
 *          Long id = order.get(Order.ID);
 *          String descr = order.get(Order.DESCR);
 *          System.out.println(<span class="character">&quot;</span><span class="character">Order id: </span><span class="character">&quot;</span> + id + <span class="character">&quot;</span><span class="character"> descr: </span><span class="character">&quot;</span> + descr);
 *
 *          <span class="keyword-directive">for</span> (Item item : order.get(Order.ITEMS)) {
 *              Long itemId = item.get(Item.ID);
 *              String itemDescr = item.get(Item.DESCR);
 *              System.out.println(<span class="character">&quot;</span><span class="character"> Item id: </span><span class="character">&quot;</span> + itemId + <span class="character">&quot;</span><span class="character"> descr: </span><span class="character">&quot;</span> + itemDescr);
 *          }
 *      }
 *  }</pre>
 *
 * @author Pavel Ponec
 * @see org.ujoframework.implementation.orm.RelationToMany
 * @see org.ujoframework.core.UjoIterator
 */
public class TableUjo<UJO_IMPL extends Ujo> extends MapUjo implements OrmUjo {
    
    /** Orm session */
    private Session session;
    /** Set of changes */
    private Set<UjoProperty> changes = null;

    public TableUjo() {
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

        if (result instanceof UniqueKey) {
            result = session.loadInternal(property, ((UniqueKey)result).getValue(), true);
            super.writeValue(property, result);
        }
        else
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
            : UjoManager.EMPTY_PROPERTIES
            ;
        if (clear) {
            changes = null;
        }
        return result;
    }


    /** Getter based on UjoProperty implemeted by a pattern UjoExt */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> VALUE get(final UjoProperty<UJO, VALUE> property) {
        final Object result = ((UjoProperty) property).of(this);
        return (VALUE) result;
    }

    /** Setter  based on UjoProperty. Type of value is checked in the runtime.
     * The method was implemented by a pattern UjoExt.
     */
    @SuppressWarnings({"unchecked"})
    public <UJO extends UJO_IMPL, VALUE> UJO_IMPL set
        ( final UjoProperty<UJO, VALUE> property
        , final VALUE value
        ) {
        readUjoManager().assertAssign(property, value);
        ((UjoProperty)property).setValue(this, value);
        return (UJO_IMPL) this;
    }

    /** Read a session */
    public Session readSession() {
        return session;
    }

    /** Write a session */
    public void writeSession(Session session) {
        this.session = session;
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

    // --------- STATIC METHODS -------------------

    /** A PropertyIterator Factory creates an new property and assign a next index.
     * @hidden
     */
    protected static <UJO extends TableUjo, ITEM extends TableUjo> RelationToMany<UJO,ITEM> newRelation(String name, Class<ITEM> type) {
        return new RelationToMany<UJO,ITEM> (name, type, _nextPropertyIndex());
    }

}
