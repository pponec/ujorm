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
package org.ujoframework.orm.utility;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import org.ujoframework.UjoProperty;
import org.ujoframework.criterion.Criterion;
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.orm.ExtendedOrmUjo;
import org.ujoframework.orm.ForeignKey;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaTable;

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
     * @return
     */
    public static final SerialBlob createBlob(byte[] bytes) {
        try {
            return bytes!=null ? new SerialBlob(bytes) : null;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Create a new Blob
     * @param inputStream
     * @return
     */
    public static final SerialBlob createBlob(InputStream inputStream) {
        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
            int len;

            while ((len = inputStream.read(buffer)) >= 0) {
                baos.write(buffer, 0, len);
            }
            return new SerialBlob(baos.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Reding error", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new IllegalStateException("Reding error", e);
            }
        }
    }

    /**
     * Returns byte array to the maximal length Integer.MAX_VALUE.
     * @param blob The null value is supported.
     * @throws IllegalStateException A container for the SQLExeption
     * @throws IndexOutOfBoundsException Length of the bytes is great than Integer.MAX_VALUE
     */
    public static byte[] getBlobBytes(Blob blob) throws IllegalStateException, IndexOutOfBoundsException {
        try {
            if (blob==null) {
                return null;
            }
            if (blob.length() <= Integer.MAX_VALUE) {
                return blob.getBytes(1, (int) blob.length());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Reding error", e);
        }
        throw new IndexOutOfBoundsException("Length of the result is great than Integer.MAX_VALUE");
    }

    /**
     * Returns a Blob byte array to the maximal length Integer.MAX_VALUE.
     * @throws IllegalStateException A container for the SQLExeption
     */
    public static InputStream getBlobStream(Blob blob) throws IllegalStateException {
        try {
            return blob.getBinaryStream();
        } catch (Exception e) {
            throw new IllegalStateException("Reding error", e);
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
            throw new IllegalStateException(e);
        }
    }

    /**
     * Create a new Clob.
     * @param text The null value is supported.
     */
    public static final SerialClob createClob(String text) {
        return text!=null ? createClob(text.toCharArray()) : null;
    }

    /**
     * Create a new Clob.
     */
    public static final SerialClob createClob(Reader reader) {
        try {
            char[] buffer = new char[1024];
            CharArrayWriter baos = new CharArrayWriter(buffer.length);
            int len;

            while ((len = reader.read(buffer)) >= 0) {
                baos.write(buffer, 0, len);
            }
            return new SerialClob(baos.toCharArray());
        } catch (Exception e) {
            throw new IllegalStateException("Reader error", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new IllegalStateException("Reader error", e);
            }
        }
    }

    /**
     * Returns a result to the maximal length Integer.MAX_VALUE.
     * @param clob The null value is supported.
     * @throws IllegalStateException A container for the SQLExeption
     * @throws IndexOutOfBoundsException Length of the bytes is great than Integer.MAX_VALUE
     */
    public static String getClobString(Clob clob) throws IllegalStateException, IndexOutOfBoundsException {
        try {
            if (clob==null) {
                return null;
            }
            if (clob.length() <= Integer.MAX_VALUE) {
                return clob.getSubString(1, (int) clob.length());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Reding error", e);
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

        for (UjoProperty p : ujo.readProperties()) {
            if (p.isTypeOf(OrmUjo.class)) {
                Object value = p.getValue(ujo);
                if (value!=null && depth>0) {
                    loadLazyValues((OrmUjo) value, depth);
                }
            }
        }
    }

    /** Load all lazy values for the current parameter recursively until optional depth.
     * @param ujos The parameter can be the
     *        {@link org.ujoframework.orm.Query Query},
     *        {@link org.ujoframework.core.UjoIterator UjoIterator} and some
     *         List for example.
     * @param depth The object resursion depth.
     * @return Returns a list of items or the parameter ujos.
     *         If the 'ujos' parameter is type of List, than method returns the parameter directly.
     */
    public static <UJO extends OrmUjo> List<UJO> loadLazyValues(final Iterable<UJO> ujos, int depth) {

        List<UJO> result = ujos instanceof List
                ? null
                : new ArrayList<UJO>(64);
        for (UJO ujo : ujos) {
            loadLazyValues(ujo, depth);
            if (result!=null) {
                result.add(ujo);
            }
        }
        if (result==null) {
            result = (List<UJO>) ujos;
        }
        return result;
    }

    /** Reload values of the persistent object. <br>
     * Note: If the object has implemented the interface
     * {@link org.ujoframework.orm.ExtendedOrmUjo ExtendedOrmUjo} than foreign keys are reloaded, else a lazy initialization for first depth is done.
     * @param ujo The persistent object to relading values.
     * @return The FALSE value means that the object is missing in the database.
     * @see Session#reload(org.ujoframework.orm.OrmUjo) 
     */
    public boolean reload(final OrmUjo ujo, final Session session) {
        return session.reload(ujo);
    }

    /** Load lazy value for all items and required property by the one SQL statement.
     * @param ujos The parameter can be the
     *        {@link org.ujoframework.orm.Query Query},
     *        {@link org.ujoframework.core.UjoIterator UjoIterator} and some
     *         List for example.
     * @return Returns a list of items or the parameter ujos.
     *         If the 'ujos' parameter is type of List, than method returns the parameter directly.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends ExtendedOrmUjo> List<UJO> loadLazyValuesAsBatch(final Iterable<UJO> ujos, UjoProperty<UJO, ? extends OrmUjo> property) {

        List<UJO> result = new ArrayList<UJO>(ujos instanceof List ? ((List) ujos).size() : 128);
        HashMap<Object, OrmUjo> map = new HashMap<Object, OrmUjo>(64);
        while (!property.isDirect()) {
            property = ((PathProperty)property).getProperty(0);
        }
        for (UJO u : ujos) {
            result.add(u);
            ForeignKey fk = u.readFK(property);
            if (fk!=null) {
                map.put(fk.getValue(), null);
            }
        }
        if (result.isEmpty()) {
            return result;
        }
        Session session = result.get(0).readSession();
        MetaColumn column = (MetaColumn) session.getHandler().findColumnModel(property);
        MetaColumn pkColumn = column.getForeignColumns().get(0);
        Query<OrmUjo> query = session.createQuery(pkColumn.getTable().getType());
        int limit = session.getParameters().get(MetaParams.MAX_ITEM_COUNT_4_IN);
        int count = map.size();
        List<Object> idList = new ArrayList(Math.min(limit, count));
        Iterator<Object> keys = map.keySet().iterator();

        for (int i = 1; i <= count; i++) {
            idList.add(keys.next());

            if (i % limit == 0 || i == count) {
                query.setCriterion(Criterion.whereIn(pkColumn.getProperty(), idList));
                for (OrmUjo u : query) {
                    map.put(pkColumn.getValue(u), u);
                }
                idList.clear();
            }
        }
        for (UJO u : result) {
            ForeignKey fk = u.readFK(property);
            if (fk!=null) {
                u.writeValue(property, map.get(fk.getValue()));
            }
        }
        return result;
    }

    /** Load lazy value for all items and all relation properties by the rule: a one SQL statement per relation property.
     * @param query
     * @return Returns a list of items or the parameter ujos.
     *         If the 'ujos' parameter is type of List, than method returns the parameter directly.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends ExtendedOrmUjo> List<UJO> loadLazyValuesAsBatch(final Query<UJO> query) {
        List<UJO> result = query.iterator().toList();
        List<MetaColumn> columns = MetaTable.COLUMNS.getList(query.getTableModel());
        for (MetaColumn col : columns) {
            if (col.isForeignKey()) {
                loadLazyValuesAsBatch(result, col.getProperty());
            }
        }
        return result;
    }
}
