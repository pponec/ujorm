/*
 * Copyright 2007-2020 Pavel Ponec, https://github.com/pponec
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

package org.ujorm2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.ujorm2.core.KeyRing;
import org.ujorm2.validator.ValidationException;

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
 * @see KeyRing
 */
@Immutable
@SuppressWarnings("deprecation")
public interface Key<D, V> extends CharSequence, Comparable<Key>, CriterionProvider<D, V> {

    /** Returns a name of the Key. */
    @Nonnull
    public String getName();

    /** Returns a name of the Key including  a simple class name (without package)
     * separated by the dot (.) character. */
    @Nonnull
    public String getFullName();

    /** Returns a class of the domain Ujo object. */
    @Nonnull
    public Class<D> getDomainClass();

    /** Returns a class of the current key. */
    @Nonnull
    public Class<V> getValueClass();

    /** Returns a container of the Key field. */
    // public Class<?> getContainerType(); // TODO (?)

    /**
     * It is a basic method for setting an appropriate type safe value to an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(org.ujorm.Key, java.lang.Object)}
     * always.
     * @param domain Related domain object
     * @param value A value to assign.
     * @throws ValidationException can be throwed from an assigned input validator{@link Validator};
     * @see Ujo#writeValue(org.ujorm.Key, java.lang.Object)
     */
    public void setValue(@Nullable V value, @Nonnull D domain) throws ValidationException;

    /**
     * TODO: Is it really the good idea to extend the interface with this method ?
     * It is a basic method for setting an appropriate type safe value to an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(org.ujorm.Key, java.lang.Object)}
     * always.
     * @param ujo Related domain object
     * @param value A value to assign.
     * @param createRelations create related UJO objects in case of the composite key
     * @throws ValidationException can be throwed from an assigned input validator{@link Validator};
     * @see Ujo#writeValue(org.ujorm.Key, java.lang.Object)
     */
//    public void setValue(UJO ujo, VALUE value, boolean createRelations) throws ValidationException;

    /**
     * A shortcut for the method {@link #of(org.ujorm.Ujo)}.
     * @see #of(Ujo)
     */
    public V getValue(@Nonnull D domain);

    /**
     * It is a basic method for getting an appropriate type safe value from an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#writeValue(org.ujorm.Key, java.lang.Object)}
     * always.
     * <br>Note: this method replaces the value of <strong>null</strong> by default
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see Ujo#readValue(Key)
     * @see #getValue(org.ujorm.Ujo)
     */
    public V of(@Nonnull D ujo);


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
     * @see org.ujorm.implementation.array.ArrayUjo
     * @see org.ujorm.core.UjoManager#readProperties(Class)
     */
    public int getIndex();

    /** Method returns a default value for substitution of the <code>null</code> value for the current key.
     * The feature is purposeful only if the default value is not <code>null</code> and a propert value is <code>null</code> .
     * @see Ujo#readValue(Key)
     */
    @Nullable
    public V getDefaultValue();


    /** Indicates whether a parameter value of the ujo "equal to" this key default value. */
    public boolean isDefault(@Nonnull D ujo);

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
    public boolean equals(final Object key);

    /**
     * Returns true, if the key value equals to a parameter value. The key value can be null.
     *
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    public boolean equals(@Nonnull D ujo, @Nullable V value);

    /**
     * Returns true, if the key name equals to the parameter value.
     * @param name The name of a key
     */
    public boolean equalsName(@Nullable CharSequence name);

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
    public boolean isComposite();

    /** Returns true if the key type is a type or subtype of the parameter class. */
    public boolean isTypeOf(@Nonnull Class type);

    /** Returns true if the domain type is a type or subtype of the parameter class. */
    public boolean isDomainOf(@Nonnull Class type);

    /** A flag for an ascending direction of sorting. It is recommended that the default result was true.
     * @since 0.85
     * @see org.ujorm.core.UjoComparator
     */
    public boolean isAscending();

    /** Create new instance of an <strong>indirect</strong> Key with the descending direction of sorting.
     * @return returns a new instance of the indirect Key
     * @since 0.85
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    public Key<D,V> descending();

    /** Create new instance of an <strong>indirect</strong> Key with the descending direction of sorting.
     * @return returns a new instance of the indirect Key
     * @since 1.21
     * @see #isAscending()
     * @see org.ujorm.core.UjoComparator
     */
    public Key<D,V> descending(boolean descending);

    /** Get the ujorm key validator or return the {@code null} value if no validator was assigned */
    public Validator<V> getValidator();

    /** Create new composite (indirect) instance of the {@link  Key}.
     * @since 0.92
     */
    public <T> CompositeKey<D, T> join(@Nonnull Key<? super V, T> key);

    /** Create new composite (indirect) instance of the {@link  Key}.
     * @param key The relation key
     * @param alias This attribute is used to distinguish the same entities
     * in different spaces. Examples of use are different alias for a table in SQL queries.
     * <br>The attribute is not serializable in the current release.
     *
     * @return
     * @since 1.43
     * @see CompositeKey#getSpaceName(int)
     */
    public <T> CompositeKey<D, T> join(@Nonnull Key<? super V, T> key, String alias);

    /** Create new composite (indirect) instance of the {@link  Key}.
     * @since 1.36
     */
    public <T> ListKey<D, T> join(@Nonnull ListKey<? super V, T> key);

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    public void copy(@Nonnull D from, @Nonnull D to);

    /** Compare to another Key object by the index and name of the Key.
     * @since 1.20
     */
    @Override
    public int compareTo(@Nonnull Key p);

    /** Returns the name of the Key without domain class.<br>
     * If an implementation provides the attribute called 'alias', so the alias name name
     * is showed after the name separated by the slash pattern along the pattern: {@code RELATION[aliasName] }.
    @Override
    public String toString();

    /**
     * Returns the name of full name of the Key including all attributes.
     * @return the full name of the Key including all attributes.
     */
    @Override
    public String toString();

    /**
     * Returns the full name of the Key including all attributes.
     * @return the full name of the Key including all attributes.
     */
    public String toStringDetailed();

}
