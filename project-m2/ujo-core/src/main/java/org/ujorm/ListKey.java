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

package org.ujorm;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * A key metadata interface for value type of {@code List<ITEM>}.
 * @author Pavel Ponec
 */
@Unmodifiable
public interface ListKey<UJO extends Ujo, ITEM> extends Key<UJO,List<ITEM>> {

    /** Returns a class of the key. */
    @NotNull Class<ITEM> getItemType();

    /** Returns a count of Items. If the key is null, method returns 0. */
    int getItemCount(@NotNull UJO ujo);

    /** Returns true if the item type is a type or subtype of the parameter class. */
    boolean isItemTypeOf(Class type);

    /**
     * Returns a value of key. The result is the same, like Ujo#readValue(ListUjoPropertyCommon).
     */
    @Nullable ITEM getItem(@NotNull UJO ujo, int index);

    /**
     * Returns the first item value or the null, if no item was found.
     */
    @Nullable ITEM getFirstItem(@NotNull UJO ujo);

    /**
     * Returns the last item value or the null, if no item was found.
     */
    @Nullable ITEM getLastItem(@NotNull UJO ujo);


    /**
     * An alias for {@link #getItem(org.ujorm.Ujo, int)}.
     * @return Returns a value of key. The result is the same, like Ujo#readValue(ListUjoPropertyCommon).
     */
    @Nullable ITEM of(@NotNull UJO ujo, int index);

    /**
     * Return a not null List. If original list value is empty, the new List is created.
     * @see #getItem(Ujo, int)
     */
    @NotNull List<ITEM> getList(@NotNull UJO ujo);

    /** Set a key item value.
     * @return the element previously at the specified position.
     */
    ITEM setItem(@NotNull UJO ujo, int index, @Nullable ITEM value);

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
    boolean addItem(@NotNull UJO ujo, @Nullable ITEM value);

    /** Removes the first occurrence in this list of the specified element.
     * @return true if this list is not null and contains the specified element, otherwise returns false.
     * @since 0.81
     */
    boolean removeItem(@NotNull UJO ujo, @NotNull ITEM value);

    /** Indicates whether a list of items is null or empty. */
    @Override
    boolean isDefault(@NotNull UJO ujo);

}
