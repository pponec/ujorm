/*
 *  Copyright 2010 Pavel Ponec.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujorm.orm.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.ExtendedOrmUjo;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaPKey;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.Assert;
import static org.ujorm.tools.Check.hasLength;

/**
 * Many useful methods for
 *  <ul>
 *  <li>BLOB/CLOB</li>
 *  <li>lazy loading</li>
 *  <li>reloading</li>
 *  <ul>
 * @author Pavel Ponec
 */
final public class OrmTools {

    /**
     * Create a new Blob
     * @param bytes The null value is supported.
     */
    public static final SerialBlob createBlob(byte[] bytes) {
        try {
            return bytes!=null ? new SerialBlob(bytes) : null;
        } catch (Exception e) {
            throw new IllegalUjormException(e);
        }
    }

    /**
     * Create a new Blob
     * @param inputStream
     */
    public static SerialBlob createBlob(InputStream inputStream) {
        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
            int len;

            while ((len = inputStream.read(buffer)) >= 0) {
                baos.write(buffer, 0, len);
            }
            return new SerialBlob(baos.toByteArray());
        } catch (RuntimeException | IOException | SQLException e) {
            throw new IllegalUjormException("Reding error", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new IllegalUjormException("Reding error", e);
            }
        }
    }

    /**
     * Returns byte array to the maximal length Integer.MAX_VALUE.
     * @param blob The null value is supported.
     * @throws IllegalStateException A container for the SQLExeption
     * @throws IndexOutOfBoundsException Length of the bytes is great than Integer.MAX_VALUE
     */
    public static byte[] getBlobBytes(Blob blob) throws IllegalUjormException, IndexOutOfBoundsException {
        try {
            if (blob==null) {
                return null;
            }
            if (blob.length() <= Integer.MAX_VALUE) {
                return blob.getBytes(1, (int) blob.length());
            }
        } catch (Exception e) {
            throw new IllegalUjormException("Reding error", e);
        }
        throw new IndexOutOfBoundsException("Length of the result is great than Integer.MAX_VALUE");
    }

    /**
     * Returns a Blob byte array to the maximal length Integer.MAX_VALUE.
     * @throws IllegalStateException A container for the SQLExeption
     */
    public static InputStream getBlobStream(Blob blob) throws IllegalUjormException {
        try {
            return blob.getBinaryStream();
        } catch (Exception e) {
            throw new IllegalUjormException("Reding error", e);
        }
    }

    // --------------
    /**
     * Create a new Clob.
     * @param text The null value is supported.
     */
    public static final SerialClob createClob(char[] text) {
        try {
            return text!=null ? new SerialClob(text) : null;
        } catch (Exception e) {
            throw new IllegalUjormException(e);
        }
    }

    /**
     * Create a new Clob.
     * @param text The null value is supported.
     */
    public static SerialClob createClob(String text) {
        return text!=null ? createClob(text.toCharArray()) : null;
    }

    /**
     * Create a new Clob.
     */
    public static SerialClob createClob(Reader reader) {
        try {
            final char[] buffer = new char[1024];
            final StringBuilder baos = new StringBuilder(buffer.length);
            int len;

            while ((len = reader.read(buffer)) >= 0) {
                baos.append(buffer, 0, len);
            }
            return new SerialClob(toCharArray(baos));
        } catch (RuntimeException | IOException | SQLException e) {
            throw new IllegalUjormException("Reader error", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new IllegalUjormException("Reader error", e);
            }
        }
    }

    /** Convert StringBuilder to a character array */
    public static char[] toCharArray(final @Nonnull StringBuilder baos) {
        final char[] result = new char[baos.length()];
        baos.getChars(0, result.length, result, 0);
        return result;
    }

    /**
     * Returns a result to the maximal length Integer.MAX_VALUE.
     * @param clob The null value is supported.
     * @throws IllegalStateException A container for the SQLExeption
     * @throws IndexOutOfBoundsException Length of the bytes is great than Integer.MAX_VALUE
     */
    public static String getClobString(Clob clob) throws IllegalUjormException, IndexOutOfBoundsException {
        try {
            if (clob==null) {
                return null;
            }
            if (clob.length() <= Integer.MAX_VALUE) {
                return clob.getSubString(1, (int) clob.length());
            }
        } catch (Exception e) {
            throw new IllegalUjormException("Reding error", e);
        }
        throw new IndexOutOfBoundsException("Length of the result is great than Integer.MAX_VALUE");
    }

    /**
     * Returns a result to the maximal length Integer.MAX_VALUE.
     * @param clob The null value is supported
     * @throws IllegalStateException A container for the SQLExeption
     * @throws IndexOutOfBoundsException Length of the bytes is great than Integer.MAX_VALUE
     */
    public static char[] getClob(Clob clob) throws IllegalStateException, IndexOutOfBoundsException {
        return clob!=null ? getClobString(clob).toCharArray() : null;
    }

    /** Load all lazy values for the current parameter. */
    public static void loadLazyValues(OrmUjo ujo) {
        loadLazyValues(ujo, 1);
    }

    /** Load all lazy values for the current parameter recursively until optional depth.
     * @param ujo The object must not be null.
     * @param depth The object depth.
     */
    @SuppressWarnings("unchecked")
    public static void loadLazyValues(final OrmUjo ujo, int depth) {
        if (--depth<0) {
            return;
        }

        for (Key p : ujo.readKeys()) {
            if (p.isTypeOf(OrmUjo.class)) {
                Object value = p.of(ujo);
                if (value!=null && depth>0) {
                    loadLazyValues((OrmUjo) value, depth);
                }
            }
        }
    }

    /** Load all lazy values for the current parameter recursively until optional depth.
     * @param ujos The parameter can be the
     *        {@link org.ujorm.orm.Query Query},
     *        {@link org.ujorm.core.UjoIterator UjoIterator} and some
     *         List for example.
     * @param depth The object resursion depth.
     * @return Returns a list of items or the parameter ujos.
     *         If the 'ujos' parameter is type of List, than method returns the parameter directly.
     */
    public static <U extends OrmUjo> List<U> loadLazyValues(final Iterable<U> ujos, int depth) {

        List<U> result = ujos instanceof List
                ? null
                : new ArrayList<>(64);
        for (U ujo : ujos) {
            loadLazyValues(ujo, depth);
            if (result!=null) {
                result.add(ujo);
            }
        }
        if (result==null) {
            result = (List<U>) ujos;
        }
        return result;
    }

    /** Reload values of the persistent object. <br>
     * Note: If the object has implemented the interface
     * {@link org.ujorm.orm.ExtendedOrmUjo ExtendedOrmUjo} than foreign keys are reloaded, else a lazy initialization for first depth is done.
     * @param ujo The persistent object to relading values.
     * @return The FALSE value means that the object is missing in the database.
     * @see Session#reload(org.ujorm.orm.OrmUjo)
     */
    public boolean reload(final OrmUjo ujo, final Session session) {
        return session.reload(ujo);
    }

        /** Load lazy value for all items and required keys by the one SQL statement.
     * @param ujos The parameter can be the
     *        {@link org.ujorm.orm.Query Query},
     *        {@link org.ujorm.core.UjoIterator UjoIterator} and some
     *         List for example.
     * @return Returns a list of items or the parameter ujos.
     *         If the 'ujos' parameter is type of List, than method returns the parameter directly.
     */
    @SuppressWarnings("unchecked")
    public static <U extends ExtendedOrmUjo> List<U> loadLazyValuesAsBatch(final Iterable<U> ujos, Key<U, ? extends OrmUjo> key) {

        final List<U> result = new ArrayList<>(ujos instanceof List ? ((List) ujos).size() : 128);
        final HashMap<Object, OrmUjo> map = new HashMap<Object, OrmUjo>(64);
        while (key.isComposite()) {
            key = ((CompositeKey)key).getKey(0);
        }
        for (U u : ujos) {
            result.add(u);
            final Object fk = u.readValue(key);
            if (fk instanceof ForeignKey) {
                map.put(((ForeignKey)fk).getValue(), null);
            }
        }
        if (result.isEmpty()) {
            return result;
        }
        final Session session = result.get(0).readSession();
        final MetaColumn column = (MetaColumn) session.getHandler().findColumnModel(key, true);
        final MetaColumn pkColumn = column.getForeignColumns().get(0);
        final Query<OrmUjo> query = session.createQuery(pkColumn.getTable().getType());
        final int limit = session.getParameters().get(MetaParams.MAX_ITEM_COUNT_4_IN);
        final int count = map.size();
        final List<Object> idList = new ArrayList<>(Math.min(limit, count));
        final Iterator<Object> keys = map.keySet().iterator();

        for (int i = 1; i <= count; i++) {
            idList.add(keys.next());

            if (i % limit == 0 || i == count) {
                query.setCriterion(Criterion.whereIn(pkColumn.getKey(), idList));
                for (OrmUjo u : query) {
                    map.put(pkColumn.getValue(u), u);
                }
                idList.clear();
            }
        }
        for (U u : result) {
            final Object fk = u.readValue(key);
            if (fk instanceof ForeignKey) {
                u.writeSession(null); // switch off the change management
                u.writeValue(key, map.get(((ForeignKey)fk).getValue()));
                u.writeSession(session);
            }
        }
        return result;
    }

    /** Load lazy value for all items and all relation keys by the rule: a one SQL statement per relation key.
     * @param query An Ujorm query
     * @return Returns a list of items or the parameter ujos.
     *         If the 'ujos' parameter is type of List, than method returns the parameter directly.
     */
    @SuppressWarnings("unchecked")
    public static <U extends ExtendedOrmUjo> List<U> loadLazyValuesAsBatch(final Query<U> query) {
        List<U> result = query.iterator().toList();
        List<MetaColumn> columns = MetaTable.COLUMNS.getList(query.getTableModel());
        for (MetaColumn col : columns) {
            if (col.isForeignKey()) {
                loadLazyValuesAsBatch(result, col.getKey());
            }
        }
        return result;
    }

    /** Create new a Criterion. Both parameters are joined by the
     * OR operator however if a one from data parameters is null, than the operator is ignored.
     * @param table NotNull
     * @param crn Nullable
     * @param primaryKeys Nullable
     * @return NotNull
     */
    public static Criterion createCriterion
            ( final MetaTable table
            , final Criterion crn
            , final List<Object> primaryKeys
            ) {
        Criterion result = crn;
        if (hasLength(primaryKeys)) {
            final MetaPKey pKey = MetaTable.PK.of(table);
            Assert.isTrue(pKey.getCount()==1, "There are supported objects with a one primary keys only");

            final Criterion c = pKey.getFirstColumn().getKey().whereIn(primaryKeys);
            result = result!=null ? result.or(c) : c;
        }
        return result!=null ? result : table.getFirstPK().getKey().forNone();
    }

    /** Clone the argument entity using all direct keys
     * including the original ORM session, if any.
     * All lazy relations will be loaded.
     */
    public static <U extends Ujo> U clone(U entity) throws IllegalStateException {
        final KeyList<U> keys = entity.readKeys();
        final U result = keys.newBaseUjo();
        for (Key k : keys) {
            k.copy(entity, result);
        }
        if (entity instanceof OrmUjo) {
           ((OrmUjo)result).writeSession(((OrmUjo)entity).readSession());
        }
        return result;
    }
}
