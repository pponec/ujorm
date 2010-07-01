/*
 *  Copyright 2009-2010 Pavel Ponec
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

package org.ujoframework.orm.metaModel;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.UjoManagerXML;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.Property;

/**
 * A logical database description.
 * The class is a root of database configuration.
 * @author Pavel Ponec
 * @composed 1 - * MetaDatabase
 * @composed 1 - 1 MetaParams
 */
final public class MetaRoot extends AbstractMetaModel {
    private static final Class CLASS = MetaRoot.class;
    private static final Logger LOGGER = Logger.getLogger(MetaRoot.class.getName());

    /** List of tables */
    public static final ListProperty<MetaRoot,MetaDatabase> DATABASES = newListProperty("database", MetaDatabase.class);
    /** ORM parameters */
    public static final Property<MetaRoot,MetaParams> PARAMETERS = newProperty("parameters", MetaParams.class);
    /** The property initialization */
    static{init(CLASS);}

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
    public MetaDatabase getDatabase(String name) {
        for (MetaDatabase database : DATABASES.getList(this)) {
            if (name==null || MetaDatabase.SCHEMA.equals(database, name)) {
                return database;
            }
        }
        return null;
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
            LOGGER.log(Level.SEVERE, "Can't export model into XML", ex);
        }
        return out.toString();
    }

    /** Returns XML header */
    private String getXmlHeader() {
        final SimpleDateFormat dFormat = new SimpleDateFormat("yyyy/MM/dd mm:HH", Locale.ENGLISH);
        final String result = new StringBuilder(128)
          .append(UjoManagerXML.XML_HEADER)
          .append("\n<!-- The Ujorm configuration file release ")
          .append(UjoManager.projectVersion())
          .append(" was created ")
          .append(dFormat.format(new Date()))
          .append(" -->")
          .toString()
          ;
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
     * @param schemaName The identifier for looking the database
     */
    public MetaDatabase removeDb(String schemaName) {
        if (super.readOnly()) {
            throw new UnsupportedOperationException("The internal state is 'read only'");
        }
        if (isUsable(schemaName)) for (MetaDatabase db : DATABASES.getList(this)) {
            if (MetaDatabase.ID.equals(db, schemaName)) {
                DATABASES.getList(this).remove(db);
                return db;
            }
        }
        return null;
    }

}
