/*
 * Copyright 2007-2017 Pavel Ponec, https://github.com/pponec
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

package org.ujorm;

import java.util.Collection;
import org.ujorm.validator.ValidationException;

/**
 * A <strong>CompositeKey</strong> interface is a composite of more Key objects.
 * The CompositeKey class can be used wherever is used Key - with a one important <strong>exception</strong>:
 * do not send the CompositeKey object to methods Ujo.readValue(...) and Ujo.writeValue(...) directly!!!
 * <p>There is preferred two methods UjoManager.setValue(...) / UjoManager.getValue(...)
 * to write and read a value instead of this - or use some type safe solution by UjoExt or a method of Key.
 * <p>Note that method isDirect() returns a false in this class. For this reason, the key is not included
 * in the list returned by Ujo.readProperties().
 *
 * @author Pavel Ponec
 * @since 0.81
 */
@SuppressWarnings("deprecation")
public interface CompositeKey<UJO, VALUE> extends Key<UJO, VALUE>, Iterable<Key<?,?>> {
    /** Default name space have got the {@code null} value */
    public static final String DEFAULT_ALIAS = null;

    /** Get the a count of the <string>direct keys</strong>. */
    public int getKeyCount();

    /** Get required key */
    public <U> Key<U, VALUE> getKey(int i);

    /** Get the first key of the current object. The result is direct key always. */
    public <U> Key<U, VALUE> getLastKey();

    /** Export all <strong>direct</strong> keys to the list from parameter. */
    public void exportKeys(Collection<Key<?,?>> result);

    /** Returns a {@code directKey} for the required level.
     * @param level Level no. 0 returns the {@code null} value always.
     * @see #getKeyCount()
     */
    public Key<?,?> getDirectKey(int level);

    /** The method writes a {@code value} to the domain object
     * and creates all missing relations.
     * <br>The method calls a method
     * {@link _Object#writeValue(org.ujorm.Key, java.lang.Object)} always.
     * @param ujo Related Ujo object
     * @param value A value to assign.
     * @throws ValidationException can be throwed from an assigned input validator{@link Validator};
     * @see _Object#writeValue(org.ujorm.Key, java.lang.Object)
     */
    @Override
    public void set(final UJO ujo, final VALUE value) throws ValidationException;

    /**
     * It is a basic method for setting an appropriate type safe value to an Ujo object.
     * <br>The method calls a method
     * {@link _Object#writeValue(org.ujorm.Key, java.lang.Object)}
     * always.
     * @param ujo Related Ujo object
     * @param value A value to assign.
     * @param createRelations  Value {@code true} creates missing domain relations (but no based entity)
     * @throws ValidationException can be throwed from an assigned input validator{@link Validator};
     * @see _Object#writeValue(org.ujorm.Key, java.lang.Object)
     */
    public void setValue(final UJO ujo, final VALUE value, boolean createRelations) throws ValidationException;

    /** Get a penultimate value of this composite key.
     * If any value is {@code null}, then the result is {@code null}.
     * If the Composite key is a direct key than the ujo argument is send to the method result.
     * @param ujo base Ujo object
     * @param create create new instance of a related UJO object for an undefined ({@code null} case.
     * During the assigning the new relations are <strong>disabled</strong> all validators.
     */
    public Object getSemiValue(UJO ujo, boolean create);


    /** Returns a {@code spaceName} for the required level.
     * @param level Level no. 0 returns the {@code null} value always.
     * @return The value is used to distinguish the same entities
     * in different spaces. Examples of use are different alias for a table in SQL queries.
     * <br>The attribute is not serializable in the current Ujorm release.
     * @see #getKeyCount()
     */
    public String getAlias(int level);

    /** Returns the {@code true} if the composite key contains any alias name */
    public boolean hasAlias();

    /**
     * Returns the {@code true}, if the values
     * {@link CompositeKey#getName() } and
     * {@link CompositeKey#getDomainType()}
     * of an another {@link CompositeKey} implementation are equals to the current object.
     * Note: Any Alias names are ignored, there is necessary to use another comparator for it.
     * @param key A checked {@link CompositeKey} implementation
     */
    @Override
    public boolean equals(final Object key);

}
