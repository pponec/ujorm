/*
 *  Copyright 2013-2026 Pavel Ponec
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
package org.ujorm.wicket.component.grid;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;

/**
 * <p>This class called <strong>OrmDataProvider</strong> is an ORM based
 * Wicket DataProvider. For a customization you can use a your own {@link IColumn} implementations
 * or you can overwrite selected methods of this provider.
 * </p><p>
 * The implementation generates two database requests per a one rendering,
 * the first one get size and the second one get paged data. You can overwrite the two data methods:
 * {@link #iterator(long, long) iterator()} and the {@link #size() size()}
 * for more optimization.
 * </p><p>
 * The current class uses a {@link WicketApplication} implementation, which must
 * implement the interface OrmHandlerProvider for an ORM support. See the example:
 * </p>
 * <h4>See the simple sample:</h4>
 * <pre class="pre"> {@code
 *  Criterion<Hotel> allActiveHotels = Hotel.ACTIVE.whereEq(true);
 *  UjoDataProvider<Hotel> dataProvider = UjoDataProvider.of(allActiveHotels);
 *
 *  dataProvider.addColumn(Hotel.NAME);
 *  dataProvider.addColumn(Hotel.CITY.add(City.NAME)); // An example of relations
 *  dataProvider.addColumn(Hotel.STREET);
 *  dataProvider.addColumn(Hotel.PRICE);
 *  dataProvider.addColumn(KeyColumn.of(Hotel.CURRENCY, SORTING_OFF));
 *  dataProvider.addColumn(Hotel.STARS);
 *  dataProvider.addColumn(Hotel.PHONE);
 *  dataProvider.setSort(Hotel.NAME);
 *
 *  panel.add(dataProvider.createDataTable("datatable", 10));
 * }
 * </pre>
 * @author Pavel Ponec
 * @deprecated Use the class {@link OrmDataProvider} rather.
 */
@Deprecated
public class UjoDataProvider<U extends OrmUjo> extends OrmDataProvider<U> {
    private static final long serialVersionUID = 1L;

    public UjoDataProvider(IModel<Criterion<U>> criterion) {
        super(criterion);
    }

    public UjoDataProvider(IModel<Criterion<U>> criterion, Key<? super U, ?> defaultSort) {
        super(criterion, defaultSort);
    }

}