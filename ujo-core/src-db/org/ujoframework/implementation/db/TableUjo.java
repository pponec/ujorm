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

package org.ujoframework.implementation.db;

import org.ujoframework.core.orm.DbHandler;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.beans.EventRegistrar;
import org.ujoframework.beans.UjoPropertyChangeSupport;
import org.ujoframework.beans.UjoPropertyChangeListener;
import org.ujoframework.implementation.map.MapUjo;

/**
 * A simple implementation of an ORM solution. Why new ORM mapping?
 * <ul>
 *    <li>type safe query language!</li>
 *    <li>no more a LazyInitialization exception :-)</li>
 *    <li>no confusing proxy business objects</li>
 *    <li>no more list properties, a new object called <a href="../../core/UjoIterator.html">UjoIterator</a>  is designed for a collection. The UjoIterator provides a toList() method however</li>
 *    <li>very small framework without more library dependences</li>
 * </ul>
 * Some other features:
 * <ul>
 *    <li>the API was inspired by a Cayenne and Hibernate ORM frameworks</li>
 *    <li>lazy initialization of properties and lazy initialization items of a collection is supported</li>
 *    <li>all persistent objects are based on the Ujo interface, namely on a TableUjo implementation</li>
 *    <li>default ORM mapping is based on the UjoProperty names however there is possible overwrite the mapping by annotations and the annoatations can be owerwrited by a XML files </li>
 *    <li>JDBC query parameters are passed by a question notation to the PreparedStatement for a better security</li>
 *    <li>missing cache is siutable for a large transactions and a selecting uncommited changes</li>
 * </ul>
 * The sample of use:
 * <pre class="pre">
 *  <span class="comment">&#47;**</span> <span class="comment">Using</span> <span class="comment">INSERT</span> <span class="comment">*&#47;</span>
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> useCreateItem() {
 *
 *      Session session = DbHandler.getInstance().getSession();
 *
 *      BoOrder order = <span class="keyword-directive">new</span> BoOrder();
 *      BoOrder.DATE.setValue(order, <span class="keyword-directive">new</span> Date());
 *      BoOrder.DESCR.setValue(order, <span class="character">&quot;</span><span class="character">My first order</span><span class="character">&quot;</span>);
 *
 *      BoItem item = <span class="keyword-directive">new</span> BoItem();
 *      BoItem.DESCR.setValue(item, <span class="character">&quot;</span><span class="character">yellow table</span><span class="character">&quot;</span>);
 *      BoItem.ORDER.setValue(item, order);
 *
 *
 *      session.save(order);
 *      session.save(item);
 *
 *      <span class="keyword-directive">if</span> (<span class="keyword-directive">true</span>) {
 *         session.commit();
 *      } <span class="keyword-directive">else</span> {
 *         session.rollback();
 *      }
 *  }
 *
 *  <span class="comment">&#47;**</span> <span class="comment">Using</span> <span class="comment">SELECT</span> <span class="comment">by</span> <span class="comment">a</span> <span class="comment">object</span> <span class="comment">relations</span> <span class="comment">*&#47;</span>
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> useRelation() {
 *
 *      Session session = DbHandler.getInstance().getSession();
 *      BoDatabase db = session.getDatabase();
 *
 *      UjoIterator&lt;BoOrder&gt; orders  = BoDatabase.ORDERS.of(db);
 *      <span class="keyword-directive">for</span> (BoOrder order : orders) {
 *          Long id = BoOrder.ID.of(order);
 *          String descr = BoOrder.DESCR.of(order);
 *          System.out.println(<span class="character">&quot;</span><span class="character">Order id: </span><span class="character">&quot;</span> + id + <span class="character">&quot;</span><span class="character"> descr: </span><span class="character">&quot;</span> + descr);
 *
 *          <span class="keyword-directive">for</span> (BoItem item : BoOrder.ITEMS.of(order)) {
 *              Long itemId = BoItem.ID.of(item);
 *              String itemDescr = BoItem.DESCR.of(item);
 *              System.out.println(<span class="character">&quot;</span><span class="character"> Item id: </span><span class="character">&quot;</span> + itemId + <span class="character">&quot;</span><span class="character"> descr: </span><span class="character">&quot;</span> + itemDescr);
 *          }
 *      }
 *  }
 *
 *  <span class="comment">&#47;**</span> <span class="comment">Using</span> <span class="comment">SELECT</span> <span class="comment">by</span> <span class="comment">QUERY</span> <span class="comment">*&#47;</span>
 *  <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> useSelection() {
 *
 *      Session session = DbHandler.getInstance().getSession();
 *
 *      Expression&lt;BoOrder&gt; exp1 = Expression.newInstance(BoOrder.DESCR, <span class="character">&quot;</span><span class="character">test order</span><span class="character">&quot;</span>);
 *      Expression&lt;BoOrder&gt; exp2 = Expression.newInstance(BoOrder.DATE, Operator.LE, <span class="keyword-directive">new</span> Date());
 *      Expression&lt;BoOrder&gt; expr = exp1.and(exp2);
 *
 *      Query&lt;BoOrder&gt; query = session.createQuery(BoOrder.<span class="keyword-directive">class</span>, expr);
 *      query.sizeRequired(<span class="keyword-directive">true</span>); <span class="comment">// need a count of iterator items, a default value is false</span>
 *      query.readOnly(<span class="keyword-directive">false</span>);
 *
 *      <span class="keyword-directive">for</span> (BoOrder order : session.iterate( query ) ) {
 *          Long id = BoOrder.ID.of(order);
 *          String descr = BoOrder.DESCR.of(order);
 *          System.out.println(<span class="character">&quot;</span><span class="character">Order id: </span><span class="character">&quot;</span> + id + <span class="character">&quot;</span><span class="character"> descr: </span><span class="character">&quot;</span> + descr);
 *      }
 *  }</pre>
 *
 * <strong>Note</strong>: the API desing a very early prototype, methods are not implemented yet.
 *
 * @author Ponec
 * @see org.ujoframework.implementation.db.UjoRelative
 * @see org.ujoframework.core.UjoIterator
 */
public class TableUjo<UJO extends Ujo> extends MapUjo implements EventRegistrar<UJO> {
    
    final private UjoPropertyChangeSupport eventRegistrar = new UjoPropertyChangeSupport(this, null);
    final private DbHandler handler;

    public TableUjo() {
        handler = DbHandler.getInstance();
        //handler.registerPropertis(getClass(), readProperties());
    }


    /** A method for an internal use. */
    @Override
    public void writeValue(UjoProperty property, Object value) {
        Object oldValue = readValue(property);
        eventRegistrar.firePropertyChange(property, oldValue, value, true);
        super.writeValue(property, value);
        eventRegistrar.firePropertyChange(property, oldValue, value, false);
    }

    /** A method for an internal use. */
    @Override
    public Object readValue(UjoProperty property) {
        Object result = super.readValue(property);
        if (property instanceof UjoRelative
        &&  handler.isPersistent(property)
        ){
            // Don't save the result!
            result = handler.getSession().iterate(property);
        }
        return result;
    }

    /** Add property Listener */
    public boolean addPropertyChangeListener
        ( UjoProperty<UJO,?> property
        , Boolean before
        , UjoPropertyChangeListener listener
        ) {
        return eventRegistrar.addPropertyChangeListener(property, before, listener);
    }

    /** Remove property Listener */
    public boolean removePropertyChangeListener
        ( UjoProperty<UJO,?> property
        , Boolean before
        , UjoPropertyChangeListener listener
        ) {
         return eventRegistrar.removePropertyChangeListener(property, before, listener);
    }


    // --------- STATIC METHODS -------------------

    /** A PropertyIterator Factory
     * @hidden
     */
    protected static <UJO extends TableUjo, ITEM extends TableUjo> UjoRelative<UJO,ITEM> newRelation(String name, Class<ITEM> type) {
        return new UjoRelative<UJO,ITEM> (name, type);
    }

}
