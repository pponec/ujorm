/*
 *  Copyright 2007-2014 Pavel Ponec
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.annot.Immutable;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.validator.ValidationException;

/**
 * A <strong>PathProperty</strong> class is an composite of a Key objects.
 * The PathProperty class can be used wherever is used Key - with a one important <strong>exception</strong>:
 * do not send the PathProperty object to methods Ujo.readValue(...) and Ujo.writeValue(...) !!!
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the Key is not included
 * in the list returned by Ujo.readProperties().
 *
 * @author Pavel Ponec
 * @since 0.81
 */
@Immutable
@SuppressWarnings("deprecation")
public class PathProperty<UJO extends Ujo, VALUE> implements CompositeKey<UJO, VALUE> {
    /** No alias is used */
    protected static final String[] NO_ALIAS = null;

    /** Array of <strong>direct</strong> keys */
    private final Key[] keys;
    /** Array of <strong>aliases</strong> keys */
    private final String[] aliases;
    /** Is key ascending / descending */
    private final boolean ascending;
    private String name;

    public PathProperty(String lastSpaceName, List<Key> keys) {
        this(lastSpaceName, keys.toArray(new Key[keys.size()]));
    }

    /** The main constructor. It is recommended to use the factory method
     * {@link #of(org.ujorm.Key, org.ujorm.Key) of(..)}
     * for better performance in some cases.
     * @see #of(org.ujorm.Key, org.ujorm.Key) of(..)
     */
    public PathProperty(String lastSpaceName, Key... keys) {
        this(null, lastSpaceName, keys);
    }

    /** Main constructor */
    @SuppressWarnings("unchecked")
    public PathProperty(Boolean ascending, String lastSpaceName, Key... keys) {
        final ArrayList<Key> list = new ArrayList<Key>(keys.length + 3);
        boolean alias = lastSpaceName != null;
        for (Key key : keys) {
            if (key.isComposite()) {
                final CompositeKey cKey = ((CompositeKey)key);
                cKey.exportKeys(list);
                alias = alias || cKey.hasAlias();
            } else {
                list.add(key);
            }
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Argument must not be empty");
        }
        this.ascending = ascending!=null ? ascending : keys[keys.length-1].isAscending();
        this.keys = list.toArray(new Key[list.size()]);
        this.aliases = alias
                ? new String[list.size()]
                : NO_ALIAS;

        if (alias) {
            int i = 0;
            for (Key key : keys) {
                if (key.isComposite()) {
                    final CompositeKey cKey = ((CompositeKey)key);
                    list.clear();
                    cKey.exportKeys(list);
                    for (int j = 0, max = list.size(); j < max; j++) {
                        this.aliases[i++] = cKey.getAlias(j);
                    }
                } else {
                    i++;
                }
            }
            if (lastSpaceName != null) {
               this.aliases[this.aliases.length - 1] = lastSpaceName;
            }
        }
    }

    /** A constructor for a better performance. For an internal use only. */
    private PathProperty(final Key[] keys, final String[] spaces, final boolean ascending) {
        this.aliases = spaces;
        this.keys = keys;
        this.ascending = ascending;

        checkAttributes();
    }

    /** A constructor for a better performance */
    public PathProperty(final Key<?,?> keys, final String[] spaces, final boolean ascending) {
        this.aliases = spaces;
        this.keys = createKeyArray(keys);
        this.ascending = ascending;

        checkAttributes();
    }

    /** Create a key array from the argument */
    private static Key[] createKeyArray(final Key keys) {
        if (keys instanceof PathProperty) {
            return ((PathProperty)keys).keys;
        } else if (keys instanceof CompositeKey) {
            final CompositeKey cKey = (CompositeKey) keys;
            final Key[] result = new Key[cKey.getCompositeCount()];
            for (int i = cKey.length() - 1; i >= 0 ; i--) {
                result[i] = cKey.getDirectKey(i);
            }
            return result;
        } else {
            return new Key[] {keys};
        }
    }

    /** Check arguments */
    private void checkAttributes() throws IllegalStateException {
        if (aliases != null && aliases.length != keys.length) {
            throw new IllegalStateException
                    ( "The spaces have hot a bad count: "
                    + (aliases != null ? aliases.length : -1));
        }
    }

    /** Get the last key of the current object. The result may not be the direct key. */
    @SuppressWarnings("unchecked")
    public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getLastPartialProperty() {
        return keys[keys.length - 1];
    }

    /** Get the first key of the current object. The result is direct key always. */
    @SuppressWarnings("unchecked")
    @Override
    final public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getLastKey() {
        Key result = keys[keys.length - 1];
        return result.isComposite()
            ? ((CompositeKey)result).getLastKey()
            : result
            ;
    }

    /** Get the first key of the current object. The result is direct key always. */
    @SuppressWarnings("unchecked")
    @Override
    final public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getFirstKey() {
        Key result = keys[0];
        return result.isComposite()
            ? ((CompositeKey)result).getFirstKey()
            : result
            ;
    }

    /** Full key names with no alias */
    final public String getName() {
        if (name == null) {
            name = getName(false);
        }
        return name;
    }

    /** Full key name with alias names, if any.
     * @param alias A request to display an alias by the template: {@code PARENT[aliasName] }
     * @return alias name
     */
    private String getName(boolean alias) {
        final StringBuilder result = new StringBuilder(32);
        for (int i = 0, max = keys.length; i < max; i++) {
            final Key p = keys[i];
            if (result.length() > 0) {
                result.append('.');
            }
            result.append(p.getName());
            if (alias) {
                final String aliasName = getAlias(i);
                if (aliasName != null) {
                    result.append('[');
                    result.append(aliasName);
                    result.append(']');
                }
            }
        }
        return result.toString();
    }

    /** Property type */
    @Override
    public Class<VALUE> getType() {
        return getLastPartialProperty().getType();
    }

    /** Property domain type */
    @Override
    @SuppressWarnings("unchecked")
    public Class<UJO> getDomainType() {
        return keys[0].getDomainType();
    }

    /** Get a penultimate value of a composite key.
     * If any value (not getLastPartialProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Ujo getSemiValue(final UJO ujo, final boolean create) {
        if (ujo == null) {
            return ujo;
        }
        Ujo result = ujo;
        for (int i = 0, max = keys.length - 1; i < max; i++) {
            Ujo value = (Ujo) keys[i].of(result);
            if (value == null) {
                if (create) {
                    try {
                        value = (Ujo) keys[i].getType().newInstance();
                        result.writeValue(keys[i], value);
                    } catch (Throwable e) {
                        throw new IllegalStateException("Can't create new instance for the key: " + keys[i].toStringFull(), e);
                    }
                } else {
                    return value;
                }
            }
            result = value;
        }
        return result;
    }

    /**
     * An alias for the method of(Ujo) .
     * @see #of(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    final public VALUE getValue(final UJO ujo) throws ValidationException {
        return of(ujo);
    }

    /** The method writes a {@code value} to the domain object
     * and creates all missing relations.
     * @see CompositeKey#setValue(org.ujorm.Ujo, java.lang.Object, boolean)
     * @since 1.45
     */
    @Override
    final public void setValue(final UJO ujo, final VALUE value) throws ValidationException {
        setValue(ujo, value, true);
    }

    @Override
    final public void setValue(final UJO ujo, final VALUE value, boolean createRelations) throws ValidationException {
        final Ujo u = getSemiValue(ujo, createRelations);
        getLastPartialProperty().setValue(u, value);
    }

    /** Get a value from an Ujo object by a chain of keys.
     * If a value  (not getLastPartialProperty) is null, then the result is null.
     */
    @Override
    final public VALUE of(final UJO ujo) {
        final Ujo u = getSemiValue(ujo, false);
        return  u!=null ? getLastPartialProperty().of(u) : null ;
    }

    @Override
    final public int getIndex() {
        return -1;
    }

    /** Returns a default value */
    @Override
    public VALUE getDefault() {
        return getLastPartialProperty().getDefault();
    }

    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    @Override
    public boolean isDefault(UJO ujo) {
        VALUE value = of(ujo);
        VALUE defaultValue = getDefault();
        final boolean result
        =  value==defaultValue
        || (defaultValue!=null && defaultValue.equals(value))
        ;
        return result;
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(final UJO from, final UJO to) {
        final Ujo from2 = getSemiValue(from, false);
        final Ujo to2 = getSemiValue(to, false);
        getLastPartialProperty().copy(from2, to2);
    }

    /** Returns true if the key type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    final public boolean isTypeOf(final Class type) {
        return getLastKey().isTypeOf(type);
    }

    /**
     * Returns true, if the key value equals to a parameter value. The key value can be null.
     *
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    @Override
    public boolean equals(final UJO ujo, final VALUE value) {
        Object myValue = of(ujo);
        if (myValue==value) { return true; }

        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    /**
     * Returns true, if the key name equals to the parameter value.
     */
    @Override
    public boolean equalsName(final CharSequence name) {
        return name!=null && name.toString().equals(getName());
    }

    /**
     * Returns true, if the key value equals to a parameter value. The key value can be null.
     *
     * @param key A basic CujoProperty.
     * @param value Null value is supported.
     */
    @Override
    public boolean equals(final Object key) {
        return key instanceof Key
            && key.toString().equals(toString())
            && getType().equals(((Key)key).getType())
            ;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public String toString() {
        return getName(true);
    }

    @Override
    public String toStringFull() {
        return getDomainType().getSimpleName() + '.' +  getName(true);
    }

    /**
     * Returns the full name of the Key including all atributes.
     * <br />Example: Person.id {index=0, ascending=false, ...}
     * @param extended argumenta false calls the method {@link #toStringFull()} only.
     * @return the full name of the Key including all atributes.
     */
    @Override
    public String toStringFull(boolean extended) {
        return  extended
                ? toStringFull() + Property.printAttributes(this)
                : toStringFull() ;
    }

    /** Length of the Name */
    @Override
    public int length() {
        return getName().length();
    }

    /** A char from Name */
    @Override
    public char charAt(int index) {
        return getName().charAt(index);
    }

    /** Sub sequence from the Name */
    @Override
    public CharSequence subSequence(int start, int end) {
        return getName().subSequence(start, end);
    }

    /**
     * If the key is the direct key of the related UJO class then method returns the TRUE value.
     * The return value false means, that key is type of {@link CompositeKey}.
     * <br />
     * Note: The composite keys are excluded from from function Ujo.readProperties() by default
     * and these keys should not be sent to methods Ujo.writeValue() and Ujo.readValue().
     * @see CompositeKey
     * @since 0.81
     * @deprecated use rather a negation of the method {@link #isComposite() }
     */
    @Deprecated
    @Override
    public final boolean isDirect() {
        return ! isComposite();
    }

    /** @{@inheritDoc} */
    @Override
    public final boolean isComposite() {
        return true;
    }

    /** A flag for an ascending direction of order. For the result is significant only the last key.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    public boolean isAscending() {
        return ascending;
    }

    /** Create a new instance of the key with a descending direction of order.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    @SuppressWarnings({"unchecked","deprecation"})
    public Key<UJO,VALUE> descending() {
        return descending(true);
    }

    /** Create a new instance of the key with a descending direction of order.
     * @see org.ujorm.core.UjoComparator
     */
    @Override
    @SuppressWarnings({"unchecked","deprecation"})
    public Key<UJO,VALUE> descending(boolean descending) {
        return isAscending()==descending
                ? new PathProperty(this, aliases, !descending)
                : this
                ;
    }

    /** Export all <string>direct</strong> keys to the list from parameter. */
    @SuppressWarnings("unchecked")
    @Override
    public void exportKeys(final Collection<Key<?,?>> result) {
        for (Key p : keys) {
            result.add(p);
        }
    }

    /** Returns a {@code directKey} for the required level.
     * @param level Level of the composite key.
     * @see #getCompositeCount()
     */
    public Key<?,?> getDirectKey(int level) {
        return keys[level];
    }

    /** Get the last key validator or return the {@code null} value if no validator was assigned */
    public Validator<VALUE> getValidator() {
        return getLastKey().getValidator();
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> CompositeKey<UJO, T> add(final Key<? super VALUE, T> key) {
        return new PathProperty(DEFAULT_ALIAS, this, key);
    }

    /** Create new composite (indirect) instance.
     * @since 1.43
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> CompositeKey<UJO, T> add(final Key<? super VALUE, T> key, String alias) {
        return new PathProperty(alias, this, key);
    }

    /** ListKey, method does not support the name spaces */
    @SuppressWarnings("unchecked")
    public <T> ListKey<UJO, T> add(ListKey<? super VALUE, T> key) {
        Key[] props = new Key[keys.length+1];
        System.arraycopy(keys, 0, props, 0, keys.length);
        props[keys.length] = key;

        return new PathListProperty(DEFAULT_ALIAS, props);
    }

    /** Create new composite (indirect) instance with a required space
     * @since 1.43
     */
    @Override
    public CompositeKey<UJO, VALUE> alias(String alias) {
        return new PathProperty<UJO, VALUE>(alias, this);
    }

    /** Returns a {@code spaceName} for the required level.
     * Level no. 0 returns the {@link null} value always.
     */
    public final String getAlias(int level) {
        return this.aliases == NO_ALIAS
            ? DEFAULT_ALIAS
            : this.aliases[level];
    }

    /** Returns the {@code true} if the composite key contains any name space */
    public boolean hasAlias() {
        return this.aliases != NO_ALIAS;
    }


    /** Compare to another Key object by the index and name of the key.
     * @since 1.20
     */
    public int compareTo(final Key p) {
        return getIndex()<p.getIndex() ? -1
             : getIndex()>p.getIndex() ?  1
             : getName().compareTo(p.getName())
             ;
    }

    /** Returns the full sequence of the direct keys */
    public Key[] getDirectKeySequence() {
        final Key[] result = new Key[this.keys.length];
        System.arraycopy(this.keys, 0, result, 0, result.length);
        return result;
    }

    /** Returns a count of inner key items of this composite key */
    public int getCompositeCount() {
        return this.keys.length;
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> where(Operator operator, VALUE value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> where(Operator operator, Key<?, VALUE> value) {
        return Criterion.where(this, operator, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereEq(VALUE value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereEq(Key<UJO, VALUE> value) {
        return Criterion.where(this, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereIn(Collection<VALUE> list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotIn(Collection<VALUE> list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereIn(VALUE... list) {
        return Criterion.whereIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotIn(VALUE... list) {
        return Criterion.whereNotIn(this, list);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNull() {
        return Criterion.whereNull(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNotNull() {
        return Criterion.whereNotNull(this);
    }

    /** {@inheritDoc} */
    public Criterion<UJO> whereFilled() {
        return Criterion.whereNotNull(this).and(Criterion.where(this, Operator.NOT_EQ, getEmptyValue()));
    }

    /** {@inheritDoc} */
    public Criterion<UJO> whereNotFilled(){
        return Criterion.whereNull(this).or(Criterion.where(this, getEmptyValue()));
    }

    /** Returns an empty value */
    private VALUE getEmptyValue() {
        final Class type = getType();
        if (CharSequence.class.isAssignableFrom(type)) {
            return (VALUE) "";
        }
        if (type.isArray()) {
            return (VALUE) Array.newInstance(type, 0);
        }
        if (List.class.isAssignableFrom(type)) {
            return (VALUE) Collections.EMPTY_LIST;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereNeq(VALUE value) {
        return Criterion.where(this, Operator.NOT_EQ, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereGt(VALUE value) {
        return Criterion.where(this, Operator.GT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereGe(VALUE value) {
        return Criterion.where(this, Operator.GE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereLt(VALUE value) {
        return Criterion.where(this, Operator.LT, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> whereLe(VALUE value) {
        return Criterion.where(this, Operator.LE, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forSql(String sqlCondition) {
        return Criterion.forSql(this, sqlCondition);
    }

    /** The method creates a new Criterion for a native condition (called Native Criterion) in SQL statejemt format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native Criterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlTemplate a SQL condition in the String format, the NULL value or empty string is not accepted
     * A substring {@code {0}} will be replaced for the current column name;
     * @param value a codition value
     * A substring {@code {1}} will be replaced for the current column name;
     * @see Operator#XSQL
     */

    public Criterion<UJO> forSql(String sqlTemplate, VALUE value) {
        return Criterion.forSql(this, sqlTemplate, value);
    }

    public Criterion<UJO> forSqlUnchecked(String sqlTemplate, Object value) {
        return Criterion.forSqlUnchecked(this, sqlTemplate, value);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forAll() {
        return Criterion.forAll(this);
    }

    /** {@inheritDoc} */
    @Override
    public Criterion<UJO> forNone() {
        return Criterion.forNone(this);
    }

    // ============= STATIC METHODS =============

    /** Create a new instance of key with a new sort attribute value.
     * @hidden
     */
    @SuppressWarnings("deprecation")
    public static <UJO extends Ujo, VALUE> Key<UJO, VALUE> sort(final Key<UJO, VALUE> key, final boolean ascending) {
        if (key.isAscending()==ascending) {
            return (Key<UJO, VALUE>) key;
        }
        return key.isComposite()
            ? new PathProperty<UJO, VALUE>(ascending, DEFAULT_ALIAS, key)
            : new PathProperty<UJO, VALUE>(new Key[]{key}, NO_ALIAS, ascending)
            ;
    }

    /** Create a new instance of key with a new sort attribute value.
     * This is an alias for the static method {@link #sort(org.ujorm.Key, boolean) sort()}.
     * @hidden
     * @see #sort(org.ujorm.Key, boolean) sort(..)
     */
    public static <UJO extends Ujo, VALUE> Key<UJO, VALUE> of(final Key<UJO, VALUE> key, final boolean ascending) {
        return sort(key, ascending);
    }

    /** Quick instance for the direct key.
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> of(final Key<UJO, VALUE> key) {
        return key.isComposite()
            ? new PathProperty<UJO, VALUE>(key.isAscending(), CompositeKey.DEFAULT_ALIAS, key)
            : new PathProperty<UJO, VALUE>(new Key[]{key}, NO_ALIAS, key.isAscending())
            ;
    }

    /** Quick instance for the direct keys
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, VALUE> PathProperty<UJO1, VALUE> of
        ( final Key<UJO1, UJO2> key1
        , final Key<UJO2, VALUE> key2
        ) {
        return key1.isComposite() || key2.isComposite()
            ? new PathProperty<UJO1, VALUE>(key2.isAscending(), DEFAULT_ALIAS, key1, key2)
            : new PathProperty<UJO1, VALUE>(new Key[]{key1,key2}, NO_ALIAS, key2.isAscending())
            ;
    }

    /** Create new instance
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> PathProperty<UJO1, VALUE> of
        ( final Key<UJO1, UJO2> key1
        , final Key<UJO2, UJO3> key2
        , final Key<UJO3, VALUE> key3
        ) {
        return new PathProperty<UJO1, VALUE>(DEFAULT_ALIAS, key1, key2, key3);
    }

    /** Create new instance
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, UJO4 extends Ujo, VALUE> PathProperty<UJO1, VALUE> of
        ( final Key<UJO1, UJO2> key1
        , final Key<UJO2, UJO3> key2
        , final Key<UJO3, UJO4> key3
        , final Key<UJO4, VALUE> key4
        ) {
        return new PathProperty<UJO1, VALUE>(DEFAULT_ALIAS, key1, key2, key3, key4);
    }

    /** Create new instance
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> create(Key<UJO, ? extends Object>... keys) {
        return new PathProperty(DEFAULT_ALIAS, keys);
    }

    /** Create a new instance of key with a new sort attribute value.
     * This is an alias for the static method {@link #sort(org.ujorm.Key, boolean) sort()}.
     * @see #sort(org.ujorm.Key, boolean) sort(..)
     * @deprecated See the {@link #of(org.ujorm.Key, org.ujorm.Key)
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Key<UJO, VALUE> newInstance(final Key<UJO, VALUE> key, final boolean ascending) {
        return sort(key, ascending);
    }

    /** Quick instance for the direct key.
     * @deprecated See the {@link #of(org.ujorm.Key, org.ujorm.Key)
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> newInstance(final Key<UJO, VALUE> key) {
        return key.isComposite()
            ? new PathProperty<UJO, VALUE>(key.isAscending(), CompositeKey.DEFAULT_ALIAS, key)
            : new PathProperty<UJO, VALUE>(new Key[]{key}, NO_ALIAS, key.isAscending())
            ;
    }

    /** Quick instance for the direct properites
     * @deprecated See the {@link #of(org.ujorm.Key, org.ujorm.Key)
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final Key<UJO1, UJO2> key1
        , final Key<UJO2, VALUE> key2
        ) {
        return key1.isComposite() || key2.isComposite()
            ? new PathProperty<UJO1, VALUE>(key2.isAscending(), DEFAULT_ALIAS, key1, key2)
            : new PathProperty<UJO1, VALUE>(new Key[]{key1,key2}, NO_ALIAS, key2.isAscending())
            ;
    }

    /** Create new instance
     * @deprecated See the {@link #of(org.ujorm.Key, org.ujorm.Key, org.ujorm.Key)
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final Key<UJO1, UJO2> key1
        , final Key<UJO2, UJO3> key2
        , final Key<UJO3, VALUE> key3
        ) {
        return new PathProperty<UJO1, VALUE>(DEFAULT_ALIAS, key1, key2, key3);
    }

    /** Create new instance
     * @deprecated See the {@link #of(org.ujorm.Key, org.ujorm.Key, org.ujorm.Key, org.ujorm.Key) )
     * @hidden
     */
    public static <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, UJO4 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final Key<UJO1, UJO2> key1
        , final Key<UJO2, UJO3> key2
        , final Key<UJO3, UJO4> key3
        , final Key<UJO4, VALUE> key4
        ) {
        return new PathProperty<UJO1, VALUE>(DEFAULT_ALIAS, key1, key2, key3, key4);
    }

}
