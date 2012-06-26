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
 * Ujo is a basic inteface of the <strong>Ujorm</strong> together with an interface <code>UjoProperty</code> .
 * <p>Basic two methods are <code>writeProperty(..)</code> and <code>readProperty(..)</code> for a manipulation with a value;
 * next method <code>readAuthorization(..)</code> recommends an authorizaton for a required action, selected Property and context;
 * the last method returns all properties of current UJO object.</p>
 * See a <a href="package-summary.html#UJO">general prologue</a> for more information or see some implementations.
 *
 *<p>The fastest way to use the interface is to extend an abstract parrent:</p>
 *<pre class="pre"><span class="java-keywords">import</span> org.ujorm.implementation.map.*;
 *<span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjo {
 *    
 *  <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person, String &gt; NAME = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">"Name"</span>, String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person, Boolean&gt; MALE = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">"Male"</span>, Boolean.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person, Double &gt; CASH = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">"Cash"</span>, <span class="java-numeric-literals">0d</span>);
 *    
 *  <span class="java-keywords">public</span> <span class="java-keywords">void</span> <span class="java-layer-method">addCash</span>(<span class="java-keywords">double</span> cash) {
 *    <span class="java-keywords">double</span> newCash = CASH.<span class="java-layer-method">of</span>(<span class="java-keywords">this</span>) + cash;
 *    CASH.<span class="java-layer-method">setValue</span>(<span class="java-keywords">this</span>, newCash);
 *  }
 *}</pre>
 * 
 * @author Pavel Ponec
 * @see UjoProperty
 * @assoc - - - UjoProperty
 * @composed - - 1 UjoPropertyList
 * @composed - - 1 UjoAction
 * @opt attributes
 * @opt operations
 */
public interface Ujo {
    
    
    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     * {@link UjoProperty#getValue(org.ujorm.Ujo)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br />NOTE: A reaction on an incorrect property depends on the implementation.
     *
     * @param property Property must be a direct type only!
     * @return Property value
     * @see UjoProperty#getValue(org.ujorm.Ujo)
     * @see UjoProperty#isDirect()
     */
    public Object readValue(UjoProperty property);
    
    
    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * {@link UjoProperty#setValue(Ujo,Object)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: A reaction on an incorrect property depends on the implementation.
     *
     * @param property Property must be a direct type only!
     * @see UjoProperty#setValue(Ujo,Object)
     * @see UjoProperty#isDirect()
     */
    public void writeValue(UjoProperty property, Object value);
    
    /** Returns all direct properties.
     * There is recommended to be a "name" of each property is unique (but it is NOT a necessary condition).
     * Two attributes with the same "name" must be demarked by a different annotation {@link XmlElementBody} for a XML export.
     *
     * <br>An index property in the array UJO must be unique a continuous, an order of property array depends on an implementation of UJO object.
     * @see UjoProperty#isDirect()
     */
    public UjoPropertyList<?> readProperties();
    
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
    public boolean readAuthorization(UjoAction action, UjoProperty property, Object value);
    
}
