/*
 *  Copyright 2013-2022 Pavel Ponec
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
import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;

/**
 * A <strong>PathListProperty</strong> class is an composite ending with a ListKey objects.
 * The PathListProperty class can be used wherever is used ListKey - with a one important <strong>exception</strong>:
 * do not send the PathListProperty object to methods Ujo.readValue(...) and Ujo.writeValue(...) !!!
 * <p>Note that method isDirect() returns a false in this class. For this reason, the Key is not included
 * in the list returned by Ujo.readProperties().
 *
 * @author Pavel Ponec
 * @since 0.81
 */
@Unmodifiable
@SuppressWarnings("deprecation")
final public class PathListProperty<U extends Ujo, VALUE>
      extends PathProperty<U, List<VALUE>>
      implements ListKey<U, VALUE> {

    public PathListProperty(String lastSpaceName, List<Key> keys) {
        super(lastSpaceName, keys);
    }

    /** The main constructor. It is recommended to use the factory method
     * {@link #of(org.ujorm.Key, org.ujorm.Key) of(..)}
     * for better performance in some cases.
     * @see #of(org.ujorm.Key, org.ujorm.Key) of(..)
     */
    public PathListProperty(String lastSpaceName, Key... keys) {
        super(lastSpaceName, keys);
    }

    /** Get the last Key of the current object. The result may not be the direct key. */
    @SuppressWarnings("unchecked")
    @Override
    public <U extends Ujo> ListKey<U, VALUE> getLastPartialProperty() {
        return (ListKey) super.getLastPartialProperty();
    }

    /** Item type */
    @Override
    public Class<VALUE> getItemType() {
        return getLastPartialProperty().getItemType();
    }

    @Override
    public boolean isItemTypeOf(Class type) {
        return getLastPartialProperty().isItemTypeOf(type);
    }

    @Override
    public VALUE getItem(U ujo, int index) {
        return of(ujo, index);
    }

    /** Returns the first item or the {@code null} value */
    @Override
    public VALUE getFirstItem(final U ujo) {
        final int i = getItemCount(ujo);
        return i > 0 ? getItem(ujo, 0) : null;
    }

    /** Returns the last item or the {@code null} value */
    @Override
    public VALUE getLastItem(final U ujo) {
        final int i = getItemCount(ujo);
        return i > 0 ? getItem(ujo, i - 1) : null;
    }

    @Override
    public VALUE of(U ujo, int index) {
        final Ujo u = getSemiValue(ujo, false);
        return  u != null ? getLastPartialProperty().getItem(u, index) : null;
    }

    @Override
    public int getItemCount(U ujo) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().getItemCount(u) : 0 ;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VALUE> getList(U ujo) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().getList(u) : null;
    }

    @Override
    public VALUE setItem(U ujo, int index, VALUE value) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().setItem(u, index, value) : null;
    }

    @Override
    public boolean addItem(U ujo, VALUE value) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().addItem(u, value) : null;
    }

    @Override
    public boolean removeItem(U ujo, VALUE value) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null && getLastPartialProperty().removeItem(u, value);
    }

    @Override
    public CompositeKey add(Key key) {
        return throwException();
    }

    @Override
    public ListKey add(ListKey key) {
        return throwException();
    }

    private <T> T throwException() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Concatenation of the ListKey is not supported");
    }

}
