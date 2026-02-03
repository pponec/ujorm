/*
 *  Copyright 2007-2026 Pavel Ponec
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

package org.ujorm.extensions;

import java.util.List;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;

/**
 * This is an <strong>extended Ujo</strong> interface designed for a more conventional key access evaluated by developers.
 * Most of the functions have been a similar reason like methods in the Key class.
 * The new solution allows to the developers to chain more keys according to
 * a model of a some new popular languages.
 * All methods are type safe likewise the usage of the Ujo interface - exclude two methods with an unlimited count of keys: setVal(...) and getVal(...).
 *
 *<br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjo <span class="java-keywords">implements</span> UjoMiddle {
 *
 *  <span class="java-keywords">public static final</span> Key&lt;Person, String &gt; NAME = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Name</span><span class="java-string-literal">&quot;</span>, String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static final</span> Key&lt;Person, Double &gt; CASH = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Cash</span><span class="java-string-literal">&quot;</span>, Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static final</span> Key&lt;Person, Person&gt; CHILD = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Child</span><span class="java-string-literal">&quot;</span>, Person.<span class="java-keywords">class</span>);
 *
 *  <span class="java-keywords">public</span> <span class="java-keywords">void</span> init() {
 *    set(NAME, <span class="java-string-literal">&quot;George&quot;</span>);
 *    set(CHILD, <span class="java-keywords">new</span> Person());
 *    set(CHILD, NAME, <span class="java-string-literal">&quot;</span><span class="java-string-literal">Jane</span><span class="java-string-literal">&quot;</span>);
 *    set(CHILD, CASH, 200d);
 *
 *    String name = get(CHILD, NAME);
 *    <span class="java-keywords">double</span> cash = get(CHILD, CASH);
 *  }
 *}</pre>
 * @author Pavel Ponec
 * @since UJO release 0.80
 */
public interface UjoExt<UJO extends UjoExt> extends UjoMiddle<UJO> {

    /** Getter based on two keys */
    <UJO2 extends Ujo, VALUE> VALUE get
        ( Key<? super UJO, UJO2 > key1
        , Key<UJO2, VALUE> key2);

    /** Getter based on three keys */
    <UJO2 extends Ujo, UJO3 extends Ujo, VALUE> VALUE get
        ( Key<? super UJO, UJO2 > key1
        , Key<UJO2, UJO3 > key2
        , Key<UJO3, VALUE> key3 );

    /** Setter  based on two keys. Type of value is checked in the runtime. */
    <UJO2 extends Ujo, VALUE> void set
        ( Key<? super UJO, UJO2 > key1
        , Key<UJO2, VALUE> key2
        , VALUE value);

    /** Setter  based on three keys. Type of value is checked in the runtime. */
    <UJO2 extends Ujo, UJO3 extends Ujo, VALUE> void set
        ( Key<? super UJO, UJO2 > key1
        , Key<UJO2, UJO3 > key2
        , Key<UJO3, VALUE> key3
        , VALUE value);


    // ------ LIST ----------

    /** Returns a count of Items. If the key is null, method returns 0.
     * <br>Inside is called a method ListUjoPropertyCommon.getItemCount() .
     */
    <ITEM> int getItemCount
        ( ListKey<? super UJO,ITEM> key);


    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method ListUjoPropertyCommon.addItem(...) .
     */
    <ITEM> Ujo add
        ( ListKey<? super UJO,ITEM> key
        , ITEM value);

    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method ListUjoPropertyCommon.setItem(...) .
     */
    <ITEM> Ujo set
        ( ListKey<? super UJO,ITEM> key
        , int index
        , ITEM value);

    /** Get Value
     * <br>Inside is called a method ListUjoPropertyCommon.getItem(...) .
     */
    <ITEM> ITEM get
        ( ListKey<? super UJO,ITEM> key
        , int index);

    /** Get Value */
    <ITEM> ITEM remove
        ( ListKey<? super UJO,ITEM> key
        , int index);

    /** Returns a not null List. If original list value is empty, the new List is created.
     * <br>Inside is called a method ListUjoPropertyCommon.getList() .
     */
    <LIST extends List<ITEM>,ITEM> LIST list
        ( ListKey<? super UJO,ITEM> key
        );

    /** Indicates whether a parameter value "equal to" key default value. */
    <VALUE> boolean isDefault
        ( Key<? super UJO, VALUE> key);

}
