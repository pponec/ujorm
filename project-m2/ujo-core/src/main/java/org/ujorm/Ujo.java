/*
 *  Copyright 2007-2010 Pavel Ponec
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

package org.ujorm;

/**
 * <a href="http://ujorm.org/" class="undecor"><img style="width: 32px; height: 32px;" alt="UJO Icons" src="ujo32.png"> 
 * UJO</a> means a <i>Unified Java Object</i> and its implementations provides a similar service like a JavaBeans class.
 * Ujo is a basic inteface of the <strong>Ujorm</strong> together with an interface <code>Key</code> .
 * <p>Basic two methods are <code>writeProperty(..)</code> and <code>readProperty(..)</code> for a manipulation with a value;
 * next method <code>readAuthorization(..)</code> recommends an authorizaton for a required action, selected Property and context;
 * the last method returns all keys of current UJO object.</p>
 * See a <a href="package-summary.html#UJO">general prologue</a> for more information or see some implementations.
 *
 *<p>The fastest way to use the interface is to extend an abstract parrent:</p>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.core.*;
 * <span class="java-keywords">public class</span> Person <span class="java-keywords">extends</span> AbstractUjo {
 *    <span class="java-keywords">private static final</span> KeyFactory fa = <span class="java-layer-method">newFactory</span>(Person.<span class="java-keywords">class</span>);
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,String &gt; NAME  = fa.<span class="java-layer-method">newKey</span>();
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Boolean&gt; MALE  = fa.<span class="java-layer-method">newKey</span>();
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Date   &gt; BIRTH = fa.<span class="java-layer-method">newKey</span>();
 *
 *    <span class="java-annotation">@</span>Override
 *    <span class="java-keywords">public</span> <span class="java-keywords">KeyList</span>&lt;?&gt; <span class="java-layer-method">readKeys(</span>() {
 *        <span class="java-keywords">return</span> fa.<span class="java-layer-method">getKeyList</span>();
 *    }
 * }
 * </pre>
 * 
 * @author Pavel Ponec
 * @see Key
 * @assoc - - - Key
 * @composed - - 1 KeyList
 * @opt attributes
 * @opt operations
 */
public interface Ujo {
    
    
    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     * {@link Key#getValue(org.ujorm.Ujo)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br />NOTE: A reaction on an incorrect property depends on the implementation.
     *
     * @param property Property must be a direct type only!
     * @return Property value
     * @see Key#getValue(org.ujorm.Ujo)
     * @see Key#isDirect()
     */
    public Object readValue(Key property);
    
    
    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * {@link Key#setValue(Ujo,Object)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: A reaction on an incorrect property depends on the implementation.
     *
     * @param property Property must be a direct type only!
     * @see Key#setValue(Ujo,Object)
     * @see Key#isDirect()
     */
    public void writeValue(Key property, Object value);
    
    /** Returns all direct keys.
     * There is recommended to be a "name" of each property is unique (but it is NOT a necessary condition).
     * Two attributes with the same "name" must be demarked by a different annotation {@link XmlElementBody} for a XML export.
     *
     * <br>An index property in the array UJO must be unique a continuous, an order of property array depends on an implementation of UJO object.
     * @see Key#isDirect()
     */
    public KeyList<?> readKeys();


    /** Returns all direct keys.
     * There is recommended to be a "name" of each property is unique (but it is NOT a necessary condition).
     * Two attributes with the same "name" must be demarked by a different annotation {@link XmlElementBody} for a XML export.
     *
     * <br>An index property in the array UJO must be unique a continuous, an order of property array depends on an implementation of UJO object.
     * @see Key#isDirect()
     * @deprecated Use the method {@link #readKeys()} instead of. The defauult implementation is: {@code new UjoPropertyList(readKeys())}
     */
    @Deprecated
    public UjoPropertyList readProperties();


    
    /**
     * Get an authorization of the property for different actions.
     * <br>There is recommended to return a true value for all actions by a default.
     * <br>Note: An implemetace may return the original property array so it is possible to change some original property in the array from an extefnal code.
     *
     * @param action Type of request. See constant(s) UjoAction.ACTION_* for more information. 
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY.
     * @param property A property of the Ujo
     * @param value A property value
     * @return Returns a TRUE value in case the property is authorized successfully.
     * @see UjoAction
     */
    public boolean readAuthorization(UjoAction action, Key property, Object value);
    
}
