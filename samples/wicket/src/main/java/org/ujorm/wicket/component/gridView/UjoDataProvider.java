/*
 *  Copyright 2013 Pavel Ponec
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
package org.ujorm.wicket.component.gridView;

import java.io.Serializable;
import java.util.Iterator;
import org.apache.wicket.Application;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.gui.WicketApplication;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.spring.UjormTransactionManager;

/**
 * SortableDataProvider extended form the Ujorm
 * @author Pavel Ponec
 */
public class UjoDataProvider<T extends OrmUjo> extends SortableDataProvider<T, String> {
    private static final long serialVersionUID = 1L;

    /** Data criterion */
    protected Criterion<T> criterion;
    /** Domain model */
    protected KeyRing<T> model;
    /** ORM transaction manager */
    protected UjormTransactionManager transactionManager;
    /** transaction state */
    private boolean transaction = false;

    /** Constructor */
    public UjoDataProvider(Criterion<T> criterion) {
        this(criterion, null);
    }

    /** Constructor */
    public UjoDataProvider(Criterion<T> criterion, Key<T,?> defaultSort) {
        this.criterion = criterion;
        model = KeyRing.of((Class<T>)criterion.findDomain());
        if (defaultSort == null) {
            defaultSort = model.getFirstKey();
        }
        setSort(defaultSort);
    }

    /**
     * Sets the current sort state and assign the BaseClass
     *
     * @param property
     * sort property
     * @param order
     * sort order
     */
    final public void setSort(Key<T, ?> property) {
        super.setSort(property.getName(), property.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING);
    }

    /** Vrací klíč pro řazení */
    public Key<T,?> getSortKey() {
        final Key<T,?> result = model.find(super.getSort().getProperty());
        return result.descending(!super.getSort().isAscending());
    }

    @Override
    public Iterator<T> iterator(long first, long count) {
        return createQuery(criterion).setLimit((int)(first + count), (int)first).addOrderBy(getSortKey()).iterator();
    }

    @Override
    public long size() {
        return createQuery(criterion).getCount();
    }

    /** Returns ORM Transaction Manager */
    protected UjormTransactionManager getOrmManager() {
        return ((WicketApplication)Application.get()).getOrmManager();
    }

    /** Returns orm Session */
    protected Session getOrmSession() {
        Session result = getOrmManager().getLocalSession();
        if (!transaction) {
            transaction = true;
            result.beginTransaction();
        }
        return result;
    }

    /** Commit and close transaction */
    @Override
    public void detach() {
        if (transaction) {
            transaction = false;
            getOrmManager().getLocalSession().commit();
        }
    }

    /** Create default Query */
    protected Query<T> createQuery(Criterion<T> criterion) {
        return getOrmSession().createQuery(criterion);
    }

    /** Get a bean Model */
    public KeyRing<T> getModel() {
        return model;
    }

    /** ORM transaction manager */
    public UjormTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /** ORM transaction manager */
    public void setTransactionManager(UjormTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /** Create a model */
    @Override
    public IModel<T> model(T object) {
        return new Model((Serializable)object);
    }

}
