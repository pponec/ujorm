/*
 *  Copyright 2016-2022 Pavel Ponec
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

import java.io.Serializable;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.IExportableColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.wicket.KeyModel;

/**
 * Exportable column model
 * @see KeyModel
 * @author Pavel Ponec
 * @param <U extends Ujo> The Model object type
 */
public class KeyColumnExportable<U extends Ujo, T> implements IExportableColumn<U, KeyRing<U>> {
    private static final long serialVersionUID = 1L;

    /** Original column */
    private final KeyColumn<U, T> column;

    /** {@inheritDoc} */
    public KeyColumnExportable(KeyColumn<U, T> column) {
        this.column = column;
    }

    /**
     * Returns an {@link IModel} of the data displayed by this column for the {@code rowModel} provided.
     * @param rowModel An {@link IModel} of the row data.
     * @return an {@link IModel} of the data displayed by this column for the {@code rowModel} provided.
     */
    @Override
    @SuppressWarnings("unchecked")
    public IModel<T> getDataModel(IModel<U> rowModel) {
        return new Model((Serializable) getDataValue(rowModel));
    }

    /** Get raw data value
     * @param rowModel Row model */
    protected T getDataValue(IModel<U> rowModel) {
        final U row = rowModel.getObject();
        final T result = column.getKey().of(row);
        return result;
    }

    /**
     * Returns a model of the column header. The content of this model is used as a heading for the column
     * when it is exported.
     * @return a model of the column header.
     */
    @Override
    public IModel<String> getDisplayModel() {
        return column.getDisplayModel();
    }

    /** {@inheritDoc} */
    @Override
    public Component getHeader(String componentId) {
        return column.getHeader(componentId);
    }

    /** {@inheritDoc} */
    @Override
    public KeyRing<U> getSortProperty() {
        return column.getSortProperty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSortable() {
        return column.isSortable();
    }

    /** {@inheritDoc} */
    @Override
    public void populateItem(Item<ICellPopulator<U>> cellItem, String componentId, IModel<U> rowModel) {
        column.populateItem(cellItem, componentId, rowModel);
    }

    /** {@inheritDoc} */
    @Override
    public void detach() {
        column.detach();
    }

    /** Domain class + key */
    @Override
    public String toString() {
        return column.toString(); //To change body of generated methods, choose Tools | Templates.
    }

}
