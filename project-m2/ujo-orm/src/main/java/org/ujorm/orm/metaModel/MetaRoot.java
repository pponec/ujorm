/*
 *  Copyright 2009-2014 Pavel Ponec
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

package org.ujorm.orm.metaModel;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.core.XmlHeader;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.utility.OrmTools;

/**
 * A logical database description.
 * The class is a root of database configuration.
 * @author Pavel Ponec
 * @composed 1 - * MetaDatabase
 * @composed 1 - 1 MetaParams
 */
@Immutable
final public class MetaRoot extends AbstractMetaModel {
    private static final Class<MetaRoot> CLASS = MetaRoot.class;
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(CLASS);
    /** XSD source */
    private static final String XSD_SOURCE = "http://ujorm.org/ujorm-1.71.xsd";

    /** Property Factory */
    private static final KeyFactory<MetaRoot> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** List of tables */
    public static final ListKey<MetaRoot,MetaDatabase> DATABASES = fa.newListKey("database");
    /** ORM parameters */
    public static final Key<MetaRoot,MetaParams> PARAMETERS = fa.newKey("parameters");
    /** The key initialization */
    static{fa.lock();}

    public MetaRoot() {
        // A default instance:
        PARAMETERS.setValue(this, new MetaParams());
    }

    /** Returns the first database or return null */
    public MetaDatabase getDatabase() {
        final MetaDatabase result = DATABASES.getItemCount(this)>0 ? DATABASES.getItem(this, 0) : null;
        return result;
    }

    /** Returns the first database with required name or returns null.
     * @param name If the parameter "name" is null than method returns a first database.
     */
    @Nullable
    public MetaDatabase getDatabase(String name) {
        for (MetaDatabase database : DATABASES.getList(this)) {
            if (name==null || MetaDatabase.SCHEMA.equals(database, name)) {
                return database;
            }
        }
        return null;
    }

    /** Returns the total count of databases. */
    public int getDatabaseCount() {
        return DATABASES.getItemCount(this);
    }

    /** Add a new database into repository. */
    final public void add(MetaDatabase database) {
        DATABASES.addItem(this, database);
    }

    /** Returns all model in a XML format */
    @Override
    public String toString() {
        CharArrayWriter out = new CharArrayWriter(128);
        try {
            print(out);
            out.append("\n---\n");
        } catch (IOException ex) {
            LOGGER.log(UjoLogger.ERROR, "Can't export model into XML", ex);
        }
        return out.toString();
    }

    /** Returns XML header */
    private XmlHeader getXmlHeader() {
        final XmlHeader result = new XmlHeader();
        final SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        final String description = new StringBuilder(128)
          .append("The Ujorm configuration file release ")
          .append(UjoManager.version())
          .append(" was created ")
          .append(dFormat.format(new Date()))
          .toString()
          ;
        result.setComment(description);
        result.getAttributes().put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        result.getAttributes().put("xsi:noNamespaceSchemaLocation", XSD_SOURCE);
        return result;
    }

    /** Pring all model in a XML format */
    public void print(Writer writer) throws IOException {
        UjoManagerXML.getInstance().saveXML(writer, this, getXmlHeader(), getClass());
    }

    /** Pring all model in a XML format */
    public void print(File file) throws IOException {
        UjoManagerXML.getInstance().saveXML(file, this, getXmlHeader(), getClass());
    }

    /** Returns the first database with the same schemaName - and remove it from the list.
     * The method is for internal use only.
     * @param databaseId The identifier for looking the database
     */
    @Nullable
    public MetaDatabase removeDb(String databaseId) {
        if (super.readOnly()) {
            throw new UnsupportedOperationException("The internal state is 'read only'");
        }
        if (OrmTools.hasLength(databaseId)) for (MetaDatabase db : DATABASES.getList(this)) { MetaDatabase.ID.of(db);
            if (MetaDatabase.ID.equals(db, databaseId)) {
                DATABASES.getList(this).remove(db);
                return db;
            }
        }
        return null;
    }

}
