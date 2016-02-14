/*
 *  Copyright 2009-2016 Pavel Ponec
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
package org.ujorm.orm;

import org.ujorm.orm.*;
import java.io.Closeable;
import java.util.List;
import java.util.NoSuchElementException;
import org.ujorm.core.DefaultUjoConverter;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.orm.Session;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.ao.CachePolicy;
import org.ujorm.orm.ao.LazyLoading;
import org.ujorm.orm.metaModel.MetaParams;

/**
 * The ORM session.
 * <br />Methods of the session are not thread safe.
 * @author Pavel Ponec
 * @composed * - 1 OrmHandler
 * @assoc - - - JdbcStatement
 */
@SuppressWarnings(value = "unchecked")
public class PojoSession implements Closeable {

    /** UjoSession */
    private final Session session;

    private final DefaultUjoConverter<OrmUjo> converter;

    public PojoSession(Session session) {
        this(session, new DefaultUjoConverter<OrmUjo>());
    }

    public PojoSession(Session session, DefaultUjoConverter<OrmUjo> converter) {
        if (session == null) {
            throw new IllegalArgumentException("The session is required");
        }
        this.session = session;
        this.converter = converter != null
                ? converter
                : new DefaultUjoConverter<OrmUjo>();
    }

    /** Returns a handler */
    public final OrmHandler getHandler() {
        return session.getHandler();
    }

    /** Make a commit om all databases  for the current transaction level. */
    public void commitTransaction() {
        session.commitTransaction();
    }

    /** Make a commit for all databases. */
    public void commit() {
        session.commit();
    }

    /** Make a rollback on all databases for the current transaction level. */
    public void rollbackTransaction() {
        session.rollbackTransaction();
    }

    /** Make a rollback for all databases. */
    public final void rollback() {
        session.rollback();
    }

    /** Make commit/rollback for all 'production' databases.
     * @param commit if parameters is false than make a rollback.
     */
    public final void commit(final boolean commit) throws IllegalStateException {
        session.commit(commit);
    }

    /** Create query for all table rows. */
    public <T> PojoQuery<T> createQuery(Class<?> aClass) {
        return new PojoQuery<T>(session.createQuery(converter.marshalType(aClass)));
    }

    /** The table class is derived from the first criterion column. */
    public <T> PojoQuery<T> createQuery(final Criterion<?> criterion) {
        return new PojoQuery<T>(session.createQuery((Criterion)criterion));
    }

    /** Returns {@code true} if exists any database row with the required condition. */
    public final <T extends OrmUjo> boolean exists(final Criterion<T> criterion) {
        return session.exists(criterion);
    }

    /** Returns {@code true} if exists any database row for the required entity. */
    public final <T> boolean exists(final Class<?> entity) {
        return session.exists( converter.marshalType(entity));
    }

    /** Make a statement INSERT or UPDATE into a database table
     * according to attribute {@link Session}. Related objects
     * must be saved using an another call of the method.
     * The method cleans all flags of modified attributes.
     */
    public void saveOrUpdate(final Object bo) throws IllegalStateException {
        session.saveOrUpdate(converter.marshal(bo));
    }

    /** INSERT object into table using the <a href="http://en.wikipedia.org/wiki/Insert_%28SQL%29">Multirow inserts</a>.
     * @param bos Business objects
     * @throws IllegalStateException
     * @see MetaParams#INSERT_MULTIROW_ITEM_LIMIT
     */
    public void save(final List<?> bos) throws IllegalStateException {
        session.save(converter.marshalList(bos));
    }

    /** INSERT object into table using the <a href="http://en.wikipedia.org/wiki/Insert_%28SQL%29">Multirow inserts</a>.
     * The method cleans all flags of modified attributes.
     * @param bos List of the business object of the same class. If the list must not contain object of different types
     * @param multiLimit Row limit for the one insert.
     *        If the value will be out of range <1,bos.size()> than the value will be corrected.
     *        If the list item count is greater than multi limit so insert will be separated by more multirow inserts.
     * @throws IllegalStateException
     */
    public void save(final List<?> bos, int multiLimit) throws IllegalStateException {
        session.save(converter.marshalList(bos), multiLimit);
    }

    /** Save all persistent attributes into DB table by an INSERT SQL statement.
     * The method cleans all flags of modified attributes. */
    public void save(final Object bo) throws IllegalStateException {
        session.save(converter.marshal(bo));
    }

    /** Database UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} for the selected object.
     * The method cleans all flags of modified attributes.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    public int update(Object bo) throws IllegalStateException {
        return session.update(converter.marshal(bo));
    }

    /** Database Batch UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} along a criterion.
     * The method cleans all flags of modified attributes.
     * <br />Warning: method does affect to parent objects, see the {@link MetaParams#INHERITANCE_MODE} for more information.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    public int update(Object bo, Criterion criterion) {
        return session.update(converter.marshal(bo), criterion);
    }

    /** Delete all object object by the criterion from parameter.
     * <br />Warning 1: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
     * <br />Warning 2: method does not delete parent objects, see the {@link MetaParams#INHERITANCE_MODE} for more information.
     * @param criterion filter for deleting tables.
     * @return Returns a number of the really deleted objects.
     */
    public <T extends OrmUjo> int delete(final Criterion<T> criterion) {
        return session.delete(criterion);
    }

    /** Delete an optional object from the parameters.
     * @param bo Business object to delete, or the {@code null} argument as a result of some nullable relation.
     * @return Returns a number of the removing items or the zero if the argument is {@code null}.
     */
    public int delete(final Object bo) {
        return session.delete(converter.marshal(bo));
    }

    /** Delete all objects of the <strong>same type</strong> from database.
     * @param bos Business objects to delete, the the {@code null} argument is not allowed
     * and the {@code null} items are not allowed too.
     * @return Returns a number of the removing items or the zero if the argumetn is {@code empty}.
     */
    public int delete(final List<?> bos) {
        return session.delete(converter.marshalList(bos));
    }

    /** Returns a count of rows */
    public <UJO extends OrmUjo> long getRowCount(Query<UJO> query) {
        return session.getRowCount(query);
    }

    /**
     * Load UJO by a unique id. If a result is not found then a null value is passed.
     * @param tableType Type of POJO
     * @param id Value ID
     */
    public <T extends Object> T load
        ( final Class<?> tableType
        , final Object id
        ) throws NoSuchElementException {
        return (T) session.load(converter.marshalType(tableType), id);
    }

    /**
     * Load UJO by a unique id. If primary key is {@code null} or no result is found
     * then the {@code null} value is returned.
     * @param tableType POJO
     */
    public <T extends Object> T loadBy(Object bo) throws NoSuchElementException {
        return (T) session.loadBy(converter.marshal(bo));
    }

    /** Reload values of the persistent object. <br>
     * Note: If the object has implemented the interface
     * {@link ExtendedOrmUjo ExtendedOrmUjo} than foreign keys are reloaded
     * else a lazy initialization is loaded - for the first key depth.
     * @param bo The persistent object to reloading values.
     * @return The FALSE value means that the object is missing in the database.
     */
    @SuppressWarnings("unchecked")
    public boolean reload(final Object bo) {
        return session.reload(converter.marshal(bo));
    }

    /** Close and release all DB connections.
     * @throws java.lang.IllegalStateException The exception contains a bug from Connection close;
     */
    @SuppressWarnings("unchecked")
    @Override
    public void close() throws IllegalStateException {
        session.close();
    }

    /** Is the session closed? */
    public boolean isClosed() {
        return session.isClosed();
    }

    /** Find object from internal cache */
    public Object findCache(Class type, Object pkey) {
        return session.findCache(converter.marshalType(type), pkey);
    }

    /** Find object from internal cache */
    public Object findCache(Class type, Object... pkeys) {
        return session.findCache(converter.marshalType(type), pkeys);
    }

    /** Clear the cache. */
    public void clearCache() {
        session.clearCache();
    }

    /** Clear cache and change its policy. */
    public final void clearCache(final CachePolicy policy) {
        session.clearCache(policy);
    }

    /** Returns parameters */
    public final MetaParams getParameters() {
        return session.getParameters();
    }

    /** The rollback is allowed only.
     * @return The result is {@code true} if an inner session attribute is true or
     * a related transaction have got the status equals {link Status#STATUS_ROLLEDBACK}.
     */
    public boolean isRollbackOnly() {
        return session.isRollbackOnly();
    }

    public void markForRolback() {
        session.markForRolback();
    }

    /** Check dialect-type */
    public final SqlDialect getDialect(Class<?> ormType) {
        return session.getDialect(converter.marshalType(ormType));
    }

    /** Returns true, if ORM type have got any from listed dialects
     * @param ormType Entity type
     * @param dialects Entity dialect type
     * @return Returns true, if ORM type have got any from listed dialects
     */
    public boolean hasDialect(Class<?> ormType, Class<? extends SqlDialect> ... dialects) {
        return session.hasDialect(converter.marshalType(ormType), dialects);
    }

    /** Enable a lazy-loading of related Ujo object.
     * The default value is assigned from the parameter {@link MetaParams#LAZY_LOADING_ENABLED}.
     * @return the lazyLoadingEnabled */
    public LazyLoading getLazyLoading() {
        return session.getLazyLoading();
    }

    /** Enable a lazy-loading of related Ujo object.
     * The default value is assigned from the parameter {@link MetaParams#LAZY_LOADING_ENABLED}.
     * @param lazyLoadingEnabled the lazyLoadingEnabled to set */
    public void setLazyLoading(LazyLoading lazyLoadingEnabled) {
        session.setLazyLoading(lazyLoadingEnabled);
    }

}
