/*
 *  Copyright 2007-2016 Pavel Ponec
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

import java.io.Serializable;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;

/**
 * This is a very fast abstract implementation of <code>Ujo</code>.
 * For implementation define only a "public static final Key" constants and a "readPropertyCount()" method in a child class.
 * The code syntax is Java 1.5 complied.
 * The class can be Serializable.
 * <br>All keys must be objects (no primitive types) in the current version of Ujorm.
 * <br>Features: very good performance, an order of keys from "<code>readKeys()</code>" method is guaranteed and independed on a Java implementation.
 * <h3>Sample of usage</h3>
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
 *        <span class="java-keywords">return</span> fa.<span class="java-layer-method">getKeys</span>();
 *    }
 * }
 * </pre>
 *
 * @see Property
 * @author Pavel Ponec
 * @composed 1 - * Property
 */
//@deprecated Use the class {@link  QuickUKjo} rather or a better class {@link KeyFactory} to create new Keys.
//@Deprecated
public abstract class AbstractUjo extends SuperAbstractUjo implements Serializable {

    /** Object data. Unauthorized writing is not allowed. */
    private final Object[] data;

    /** Constructor */
    public AbstractUjo() {
        this.data = new Object[readKeys().size()];
    }

    public AbstractUjo(Object[] data) {
        this.data = data;
    }

    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method
     * {@link Key#setValue(Ujo,Object)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators.
     * <br>NOTE: If the Key is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see Key#setValue(Ujo,Object)
     */

    @Override
    public void writeValue(final Key<?,?> key, final Object value) {
        assert UjoManager.assertDirectAssign(key, value, this);
        data[key.getIndex()] = value;
    }


    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     * {@link Key#of(Ujo)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors.
     * <br>NOTE: If key is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see Key#of(Ujo)
     */
    @Override
    public Object readValue(final Key<?,?> key) {
        assert !key.isComposite() : "Property must be direct only.";
        return data[key.getIndex()];
    }

    // ===== STATIC METHODS =====

    /** Create a factory with a camel-case Key name generator with the off validator.
     * <br>Note: after declarations of all properties is recommend to call method {@code KeyFactory.close()};
     * <br>In case of OrmUjo the method is called by a Ujorm framework
     */
    @SuppressWarnings("unchecked")
    protected static <UJO extends Ujo, FACTORY extends KeyFactory<UJO>> FACTORY newNoCheckFactory(Class<? extends UJO> ujoClass) {
        return (FACTORY) KeyFactory.NoCheckBuilder.get(ujoClass);
    }

    /** Create a factory with a camel-case Key name generator.
     * <br>Note: after declarations of all properties is recommend to call method {@code KeyFactory.close()};
     * <br>In case of OrmUjo the method is called by a Ujorm framework
     */
    @SuppressWarnings("unchecked")
    protected static <UJO extends Ujo, FACTORY extends KeyFactory<UJO>> FACTORY newCamelFactory(Class<? extends UJO> ujoClass) {
        return (FACTORY) KeyFactory.CamelBuilder.get(ujoClass);
    }

    /** Create a factory with a camel-case Key name generator.
     * <br>Note: after declarations of all properties is recommend to call method {@code KeyFactory.close()};
     * <br>In case of OrmUjo the method is called by a Ujorm framework
     */
    @SuppressWarnings("unchecked")
    protected static <UJO extends Ujo, FACTORY extends KeyFactory<UJO>> FACTORY newSnakeCaseFactory(Class<? extends UJO> ujoClass) {
        return (FACTORY) KeyFactory.SnakeCaseBuilder.get(ujoClass);
    }

    /** Create a base factory Key name generator where key name is the same as its field name.
     * <br>Note: after declarations of all properties is recommend to call method {@code KeyFactory.close()};
     * <br>In case of OrmUjo the method is called by a Ujorm framework
     */
    @SuppressWarnings("unchecked")
    protected static <UJO extends Ujo, FACTORY extends KeyFactory<UJO>> FACTORY newFactory(Class<? extends UJO> ujoClass) {
        return (FACTORY) (KeyFactory) KeyFactory.Builder.get(ujoClass);
    }
}
