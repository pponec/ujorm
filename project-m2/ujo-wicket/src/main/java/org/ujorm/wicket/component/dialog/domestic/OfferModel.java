/*
 * Copyright 2015, Pavel Ponec
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
package org.ujorm.wicket.component.dialog.domestic;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.core.UjoManager;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.wicket.OrmSessionProvider;
import org.ujorm.wicket.component.form.Closeable;
import org.ujorm.wicket.component.grid.AbstractDataProvider;
import org.ujorm.wicket.component.grid.CommonAction;
import org.ujorm.wicket.component.grid.CommonActionPanel;
import org.ujorm.wicket.component.grid.KeyColumn;
import org.ujorm.wicket.component.grid.ListDataProvider;
import org.ujorm.wicket.component.grid.OrmDataProvider;

/**
 * UjoFieldModel
 * @author Pavel Ponec
 */
public class OfferModel<U extends Ujo & Serializable> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OfferModel.class);

    /** Main filter */
    private final IModel<Criterion<U>> filter;
    /** Main filter */
    private IModel<Criterion<U>> highliting;
    /** Title */
    private IModel<String> title;
    /** Window dimension */
    private final Dimension dimension = new Dimension(700, 400);
    /** Table columns */
    private KeyList<U> columns;
    /** Unique identifier */
    private KeyList<?> id;
    /** Display column of the UjoField */
    private KeyList<?> display;
    /** DataProvider */
    private AbstractDataProvider provider;
    /** Row count */
    private int rowCount = 10;
    /** Orm Handler */
    transient private OrmHandler ormHandler;
    /** Closable object */
    private Closeable<U> closable;


    /** All item */
    public OfferModel(Class<U> type) {
        this(UjoManager.getInstance().readKeys(Args.notNull(type, "type")).getFirstKey().forAll());
    }

    /** Filtering */
    public OfferModel(Criterion<U> filter) {
        this.filter = Model.of(Args.notNull(filter, "filter"));
        this.highliting = new Model<Criterion<U>>(null);
    }

    /** Returns filter */
    @Nonnull
    public IModel<Criterion<U>> getFilter() {
        return filter;
    }

    /** Returns highliting */
    @Nullable
    public void setHighliting(@Nullable Criterion<U> highliting) {
        this.highliting.setObject(highliting);
    }

    /** Returns highliting with a non-null model */
    @Nonnull
    public IModel<Criterion<U>> getHighliting() {
        return highliting;
    }

    /** Return a base class */
    @Nonnull
    public Class<U> getType() {
        return (Class<U>) filter.getObject().getDomain();
    }

    /** Dialog title */
    @Nonnull
    public IModel<String> getTitle() {
        if (title== null) {
            title = new Model<String>("Offer");
        }
        return title;
    }

    /** Set dimension of Window */
    public void setDimension(int width, int height) {
        dimension.width = width;
        dimension.height = height;
    }

    /** Set dimension of Window */
    @Nonnull
    public Dimension getDimension() {
        return dimension;
    }

    /** Row count */
    public int getRowCount() {
        return rowCount;
    }

    /** Row count */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /** Table columns */
    public <V> KeyList<U> getColumns() {
        if (columns == null) {
            final List<Key> keys = new ArrayList<Key>(32);
            final KeyList<U> fullKeys = UjoManager.getInstance().readKeys(getType());
            final Key<U, V> id = getId();
            for (Key<U,?> key : fullKeys) {
                if (id == key) {
                    continue;
                }
                if (key.isTypeOf(Ujo.class)) {
                    continue;
                }
                keys.add(key);
            }
            columns = keys.isEmpty() ? fullKeys : KeyRing.of((List)keys);
        }
        return columns;
    }

    /** Table columns */
    public void setColumns(KeyRing<U> columns) {
        this.columns = columns;
    }

    /** Data Provider
     * @return  */
    public AbstractDataProvider getProvider() {
        if (provider == null) {
            final Key<U,?> sortKey = getColumns().getFirstKey();
            if (isOrm()) {
                provider = new OrmDataProvider(getFilter(), sortKey);
            } else {
                provider = new ListDataProvider(getFilter(), sortKey);
            }
            addTableColumns(provider);
            provider.setHighlighting(highliting);
        }
        return provider;
    }

    /** Add table columns */
    protected void addTableColumns(final AbstractDataProvider provider) {
        provider.add(createSelectColumn(CommonAction.SELECT));
        final KeyList<U> columns = getColumns();
        for (int i = 0, max = columns.size(); i < max; i++) {
            final Key<U,?> key = columns.get(i);
            if (i == 0) {
                // TODO: the first column must be link to the SELECT action
                provider.add(key);
            } else {
                provider.add(key);
            }
        }
    }

    /** Create new instance of an KeyColumn */
    public KeyColumn<U, Object> createSelectColumn(final String action) {
        Key column = SelectUjo.SELECT; // Some litle hack
        final KeyColumn<U, Object> result = new KeyColumn<U, Object>(KeyRing.<U>of(column), null) {
            @Override public void populateItem(final Item<ICellPopulator<U>> item, final String componentId, final IModel<U> model) {
                item.add(new CommonActionPanel(componentId, model.getObject(), CommonAction.of(action)) {
                    @Override protected void onClick(AjaxRequestTarget target, CommonAction action) {
                        if (closable != null) {
                            closable.closeDialog(target, (U) row);
                        } else {
                            LOGGER.warn("Can't close dialog");
                        }
                    }
                });
            }
        };
        result.setCssClass(AbstractDataProvider.DEFAULT_CSS_ACTION);
        return result;
    }

    /** Is the Domain type an ORM class ? */
    protected boolean isOrm() {
        return OrmUjo.class.isAssignableFrom(getType());
    }

    /** Create Date Table */
    public <S> DataTable<U,S> createDataTable() {
        return getProvider().createDataTable(getRowCount());
    }

    /** Data Provider */
    public void setProvider(AbstractDataProvider provider) {
        this.provider = provider;
    }

    /** Display column of the UjoField */
    @Nullable
    public <D extends Ujo> Key<D,U> getDisplay() {
        return display != null ? (Key<D, U>) display.getFirstKey() : null;
    }

    /** Display column of the UjoField */
    public <D extends Ujo> void setDisplay(@Nullable Key<? super D,U> display) {
        this.display = display != null ? KeyRing.of(display) : null;
    }

    /** Display column of the UjoField */
    @Nonnull
    public <V> Key<U,V> getId() {
        if (id == null) {
            if (isOrm()) {
                final Class<OrmUjo> ormType = (Class<OrmUjo>) (Class) getType();
                final MetaTable table = getOrmHandler().findTableModel(ormType);
                id = KeyRing.of(table.getFirstPK().getKey());
            } else {
                final KeyList<U> fullKeys = UjoManager.getInstance().readKeys(getType());
                id = findKeyByName("ID", fullKeys);
                if (id == null) {
                    id = KeyRing.of(fullKeys.getFirstKey());
                }
            }
        }
        return (Key<U, V>) id.getFirstKey();
    }
    
    /** FindKey by name with ignore case
     * @param keyName Key name in UPPER CASE
     * @param fullKeys all direct keys
     * @return Primary Key in a KeyList format
     */
    protected KeyList<?> findKeyByName(final String keyName, final KeyList<U> fullKeys) {
        for (Key<U, ?> key : fullKeys) {
            if (keyName.equals(key.getName().toUpperCase(Locale.ENGLISH))) {
                return KeyRing.of(key);
            }
        }
        return null;
    }

    /** Display column of the UjoField */
    public <D extends Ujo> void setId(@Nullable Key<? super D,U> id) {
        this.id = id != null ? KeyRing.of(id) : null;
    }

    /** Returns ORM handelr */
    protected OrmHandler getOrmHandler() {
        if (ormHandler == null) {
            ormHandler = OrmSessionProvider.getOrmHandler();
        }
        return ormHandler;
    }

    /** Closable object */
    public Closeable<U> getClosable() {
        return closable;
    }

    /** Closable object */
    public void setClosable(@Nonnull Closeable<U> closable) {
        this.closable = Args.notNull(closable, "closable");
    }

}
