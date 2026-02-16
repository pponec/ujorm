/*
 * Copyright 2007-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * This interface is a descriptor of the {@link Ujo} attribute. The Key contains only meta-data
 * and therefore the Property implementation never contains business data. 
 * Each instance of the Key must be located in the {@code public static final} field of some Ujo implementation.
 * The Key can't have a serializable feature never, because its instance is the unique for a related java field.
 * An appropriate solution solution for serialization is to use a decorator class KeyRing.
 * <br>See a <a href="package-summary.html#UJO">general information</a> about current framework or see some implementations.
 *
 * @author Pavel Ponec
 * @see Ujo
 * @opt attributes
 * @opt operations
 */
@Unmodifiable
@SuppressWarnings("deprecation")
public interface Key <UJO extends Object,VALUE> extends CharSequence, Comparable<Key> {

    /** Returns a name of the Key. */
    @NotNull String getName();

    /** Returns a name of the Key including  a simple class name (without package)
     * separated by the dot (.) character. */
    @NotNull String getFullName();

    /** Returns a class of the current key. */
    @NotNull Class<VALUE> getType();

    /** Returns a class of the domain Ujo object. */
    @NotNull Class<UJO> getDomainType();

    /** Returns a container of the Key field. */
    // public Class<?> getContainerType(); // TODO (?)

    /**
     * It is a basic method for setting an appropriate type safe value to an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(Key, Object)}
     * always.
     * @param ujo Related Ujo object
     * @param value A value to assign.
     * @see Ujo#writeValue(Key, Object)
     */
    void setValue(@NotNull UJO ujo, @Nullable VALUE value);

    /**
     * TODO: Is it really the good idea to extend the interface with this method ?
     * It is a basic method for setting an appropriate type safe value to an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(Key, Object)}
     * always.
     * @param ujo Related Ujo object
     * @param value A value to assign.
     * @param createRelations create related UJO objects in case of the composite key
     * @throws ValidationException can be throwed from an assigned input validator{@link Validator};
     * @see Ujo#writeValue(Key, Object)
     */
//    public void setValue(UJO ujo, VALUE value, boolean createRelations) throws ValidationException;

    /**
     * A shortcut for the method {@link #of(UJO)}.
     */
    VALUE getValue(@NotNull UJO ujo);

    /**
     * It is a basic method for getting an appropriate type safe value from an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(Key, Object)}
     * always.
     * <br>Note: this method replaces the value of <strong>null</strong> by default
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see Ujo#readValue(Key)
     * @see #getValue(UJO)
     */
    VALUE of(@NotNull UJO ujo);


//    /**
//     * Similar function like getValue(UJO), however in case a null parameter is used so the result value is null and no NullPointerExeption is throwed.
//     * @param ujo If a null parameter is used then the null value is returned.
//     * @return Returns a type safe value from the ujo object.
//     * @see #getValue(Ujo)
//     */
//    public VALUE takeFrom(UJO ujo);

    /** Returns a key index or value -1 if the key index is not defined.
     * <br>The index is reasonable for an implementation an <code>ArrayUjo</code> class and the value is used is used
     * <br>for a sorting of Keys in a method <code>UjoManager.readProperties(Class type)</code> .
     */
    int getIndex();

    /** Method returns a default value for substitution of the <code>null</code> value for the current key.
     * The feature is purposeful only if the default value is not <code>null</code> and a propert value is <code>null</code> .
     * @see Ujo#readValue(Key)
     */
    @Nullable VALUE getDefault();


    /** Indicates whether a parameter value of the ujo "equal to" this key default value. */
    boolean isDefault(@NotNull UJO ujo);

    /**
     * Returns the {@code true}:
     * <ul>
     *   <li>For adirect Key: if argument is the same.</li>
     *   <li>For a composite Key: if all items are the same.</li>
     * </ul>
     * Note: Any Alias names are ignored, there is necessary to use another comparator for it.
     * @param key A checked {@link CompositeKey} implementation
     */
    @Override
    boolean equals(final Object key);

    /**
     * Returns true, if the key value equals to a parameter value. The key value can be null.
     *
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    boolean equals(@NotNull UJO ujo, @Nullable VALUE value);

    /**
     * Returns true, if the key name equals to the parameter value.
     * @param name The name of a key
     */
    boolean equalsName(@Nullable CharSequence name);

    /**
     * The composite key is an instance of CompositeKey.
     * It this key is the a direct key of a related UJO class then this method returns the TRUE value.
     * All composite keys are excluded from from list {@link Ujo#readKeys()} by default
     * <br>
     * Note:
     * and these keys should not be sent to methods Ujo.writeValue() and Ujo.readValue().
     * @see CompositeKey
     * @since 1.36
     */
    boolean isComposite();

    /** Returns true if the key type is a type or subtype of the parameter class. */
    boolean isTypeOf(@NotNull Class type);

    /** Returns true if the domain type is a type or subtype of the parameter class. */
    boolean isDomainOf(@NotNull Class type);

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    void copy(@NotNull UJO from, @NotNull UJO to);

    /** Compare to another Key object by the index and name of the Key.
     * @since 1.20
     */
    @Override
    int compareTo(@NotNull Key p);

    /** Returns the name of the Key without domain class.<br>
     * If an implementation provides the attribute called 'alias', so the alias name name
     * is showed after the name separated by the slash pattern along the pattern: {@code RELATION[aliasName] }.
    @Override
    public String toString();

    /** Returns the full name of the Key including a simple domain class and aliases.
     * <br>Example: Person.ID */
    @NotNull String toStringFull();

    /**
     * Returns the full name of the Key including all attributes.
     * <br>Example: Person.id {index=0, ascending=false, ...}
     * @param extended arguments false calls the method {@link #getFullName()} only.
     * @return the full name of the Key including all attributes.
     */
    String toStringFull(boolean extended);

}
