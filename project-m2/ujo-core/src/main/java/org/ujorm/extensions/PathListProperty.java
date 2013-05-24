/*
 *  Copyright 2013-2013 Pavel Ponec
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
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.core.annot.Immutable;

/**
 * A <strong>PathListProperty</strong> class is an composite ending wtih a ListKey objects.
 * The PathListProperty class can be used wherever is used ListKey - with a one important <strong>exception</strong>:
 * do not send the PathListProperty object to methods Ujo.readValue(...) and Ujo.writeValue(...) !!!
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the property is not included
 * in the list returned by Ujo.readProperties().
 *
 * @author Pavel Ponec
 * @since 0.81
 */
@Immutable
@SuppressWarnings("deprecation")
final public class PathListProperty<UJO extends Ujo, VALUE>
      extends PathProperty<UJO, List<VALUE>>
      implements ListKey<UJO, VALUE> {

    public PathListProperty(List<Key> keys) {
        super(keys);
    }

    /** The main constructor. It is recommended to use the factory method
     * {@link #newInstance(org.ujorm.Key, org.ujorm.Key) newInstance(..)}
     * for better performance in some cases.
     * @see #newInstance(org.ujorm.Key, org.ujorm.Key) newInstance(..)
     */
    public PathListProperty(Key... keys) {
        super(keys);
    }

    /** Get the last property of the current object. The result may not be the direct property. */
    @SuppressWarnings("unchecked")
    @Override
    public final <UJO_IMPL extends Ujo> ListKey<UJO_IMPL, VALUE> getLastPartialProperty() {
        return (ListKey) super.getLastPartialProperty();
    }

    /** Item type */
    public Class<VALUE> getItemType() {
        return getLastPartialProperty().getItemType();
    }

    public boolean isItemTypeOf(Class type) {
        return getLastPartialProperty().isItemTypeOf(type);
    }

    final public VALUE getItem(UJO ujo, int index) {
        return of(ujo, index);
    }

    public VALUE of(UJO ujo, int index) {
        final Ujo u = getSemiValue(ujo, false);
        return  u != null ? getLastPartialProperty().getItem(u, index) : null;
    }

    public int getItemCount(UJO ujo) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().getItemCount(u) : 0 ;
    }

    @SuppressWarnings("unchecked")
    public List<VALUE> getList(UJO ujo) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().getList(u) : null;
    }

    public VALUE setItem(UJO ujo, int index, VALUE value) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().setItem(u, index, value) : null;
    }

    public boolean addItem(UJO ujo, VALUE value) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().addItem(u, value) : null;
    }

    public boolean removeItem(UJO ujo, VALUE value) {
        final Ujo u = getSemiValue(ujo, false);
        return u != null ? getLastPartialProperty().removeItem(u, value) : false;
    }

    @Override
    public CompositeKey add(Key property) {
        return throwException();
    }

    @Override
    public ListKey add(ListKey property) {
        return throwException();
    }

    private <T> T throwException() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Concatenation of the ListKey is not supported");
    }


}
