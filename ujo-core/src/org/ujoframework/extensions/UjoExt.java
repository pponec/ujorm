/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.extensions;

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * This is an <strong>extended Ujo</strong> interface designed for a more conventional property access evaluated by developers.
 * Most of the functions have been a similar reason like methods in the UjoProperty class. 
 * The new solution allows to the developers to chain more properties according to 
 * a model of a some new popular languages.
 * All methods are type safe likewise the usage of the Ujo interface - exclude two methods with an unlimited count of properties: setVal(...) and getVal(...).
 * 
 *<br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjoExt {
 *
 *  <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> MapProperty&lt;Person, String &gt; NAME = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Name</span><span class="java-string-literal">&quot;</span>, String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> MapProperty&lt;Person, Double &gt; CASH = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Cash</span><span class="java-string-literal">&quot;</span>, Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> MapProperty&lt;Person, Person&gt; CHILD = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Child</span><span class="java-string-literal">&quot;</span>, Person.<span class="java-keywords">class</span>);
 *    
 *  <span class="java-keywords">public</span> <span class="java-keywords">void</span> init() {
 *    set(NAME, <span class="java-string-literal">&quot;</span><span class="java-string-literal">George</span><span class="java-string-literal">&quot;</span>);
 *    set(CHILD, <span class="java-keywords">new</span> Person());
 *    set(CHILD, NAME, <span class="java-string-literal">&quot;</span><span class="java-string-literal">Jane</span><span class="java-string-literal">&quot;</span>);
 *    set(CHILD, CASH, 200d);
 *        
 *    String name = get(CHILD, NAME);
 *    <span class="java-keywords">double</span> cash = get(CHILD, CASH);
 *  }
 *}</pre>
 * @author Paul Ponec
 * @since UJO release 0.80 
 */
public interface UjoExt<UJO_IMPL extends UjoExt> extends Ujo {
    
    /** Getter based on one UjoProperty */
    public <UJO extends UJO_IMPL, VALUE> VALUE get
        ( UjoProperty<UJO, VALUE> property);

    /** Getter based on two properties */
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, VALUE> VALUE get
        ( UjoProperty<UJO1, UJO2 > property1
        , UjoProperty<UJO2, VALUE> property2);

    /** Getter based on three properties */
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> VALUE get
        ( UjoProperty<UJO1, UJO2 > property1
        , UjoProperty<UJO2, UJO3 > property2
        , UjoProperty<UJO3, VALUE> property3 );

    /** Setter  based on UjoProperty. Type of value is checked in the runtime. */
    public <UJO extends UJO_IMPL, VALUE> Ujo set
        ( UjoProperty<UJO, VALUE> property
        , VALUE value);

    /** Setter  based on two properties. Type of value is checked in the runtime. */
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, VALUE> void set
        ( UjoProperty<UJO1, UJO2 > property1
        , UjoProperty<UJO2, VALUE> property2
        , VALUE value);

    /** Setter  based on three properties. Type of value is checked in the runtime. */
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> void set
        ( UjoProperty<UJO1, UJO2 > property1
        , UjoProperty<UJO2, UJO3 > property2
        , UjoProperty<UJO3, VALUE> property3
        , VALUE value);


    // ------ LIST ----------
    
    /** Returns a count of Items. If the property is null, method returns 0. 
     * <br>Inside is called a method UjoPropertyList.getItemCount() .
     */
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> int getItemCount
        ( UjoPropertyList<UJO,LIST,ITEM> property);

    
    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method UjoPropertyList.addItem(...) .
     */
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> Ujo add
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , ITEM value);

    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method UjoPropertyList.setItem(...) .
     */
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> Ujo set
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , int index
        , ITEM value);
    
    /** Get Value
     * <br>Inside is called a method UjoPropertyList.getItem(...) .
     */
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> ITEM get
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , int index);
    
    /** Get Value */
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> ITEM remove
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , int index);

    /** Returns a not null List. If original list value is empty, the new List is created.
     * <br>Inside is called a method UjoPropertyList.getList() .
     */
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> LIST list
        ( UjoPropertyList<UJO,LIST,ITEM> property
        );


    /** Indicates whether a parameter value "equal to" property default value. */
    public <UJO extends UJO_IMPL, VALUE> boolean isDefault
        ( UjoProperty<UJO, VALUE> property);
    
    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     * <br>The method can be an alias for a method readValueString(...)
     *
     * @param property A Property
     * @return If property type is "container" then result is null.
     */
    public String getText(final UjoProperty property);
    
    
    
    /**
     * Set value from a String format by a NULL context. Property can't be an "container" type (Ujo, List, Object[]).
     * <br>The method can be an alias for a method writeValueString(...)
     * @param property Property
     * @param value String value
     */
    public void setText(final UjoProperty property, final String value);
        
}
