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

package org.ujorm2;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A key metadata interface for value type of {@code List<ITEM>}.
 * @author Pavel Ponec
 */
@Immutable
public interface ListKey<D, ITEM> extends Key<D,List<ITEM>> {

    /** Returns a class of the key. */
    @Nonnull
    public Class<ITEM> getItemType();

    /** Returns a count of Items. If the key is null, method returns 0. */
    public int getItemCount(@Nonnull D ujo);

    /** Returns true if the item type is a type or subtype of the parameter class. */
    public boolean isItemTypeOf(Class type);

    /**
     * Returns a value of key. The result is the same, like Ujo#readValue(ListUjoPropertyCommon).
     */
    @Nullable
    public ITEM getItem(@Nonnull D ujo, int index);

    /**
     * Returns the first item value or the null, if no item was found.
     */
    @Nullable
    public ITEM getFirstItem(@Nonnull D ujo);

    /**
     * Returns the last item value or the null, if no item was found.
     */
    @Nullable
    public ITEM getLastItem(@Nonnull D ujo);


    /**
     * An alias for {@link #getItem(org.ujorm.Ujo, int)}.
     * @return Returns a value of key. The result is the same, like Ujo#readValue(ListUjoPropertyCommon).
     */
    @Nullable
    public ITEM of(@Nonnull D ujo, int index);

    /**
     * Return a not null List. If original list value is empty, the new List is created.
     * @see #getItem(Ujo, int)
     */
    @Nonnull
    public List<ITEM> getList(@Nonnull D ujo);

    /** Set a key item value.
     * @return the element previously at the specified position.
     */
    public ITEM setItem(@Nonnull D ujo, int index, @Nullable ITEM value);

    /** Add an Item value to a List key. If the list is {@code null}, than the method create a new instance of List (for exact behaviour see an implementation).
     * The method works like a similar code:
     * <pre class="pre">
     * if (ujo.get(VALUE_LIST)==null) {
     *    ujo.set(VALUE_LIST, new ArrayList());
     * }
     * ujo.get(VALUE_LIST).add(itemValue);
     * </pre>
     * @return Value {@code true} as per the general contract of Collection.add.
     */
    public boolean addItem(@Nonnull D ujo, @Nullable ITEM value);

    /** Removes the first occurrence in this list of the specified element.
     * @return true if this list is not null and contains the specified element, otherwise returns false.
     * @since 0.81
     */
    public boolean removeItem(@Nonnull D ujo, @Nonnull ITEM value);

    /** Indicates whether a list of items is null or empty. */
    @Override
    public boolean isDefault(@Nonnull D ujo);

}
