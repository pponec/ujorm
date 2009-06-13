/*
 *  Copyright 2009 Paul Ponec
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
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.UjoManagerXML;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;

/**
 * A logical database description.
 * The class is a root of database configuration.
 * @author Pavel Ponec
 * @composed 1 - * MetaDatabase
 * @composed 1 - 1 MetaParams
 */
public class MetaRoot extends AbstractMetaModel {

    public static final Logger LOGGER = Logger.getLogger(MetaRoot.class.getName());
    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    /** List of tables */
    public static final ListProperty<MetaRoot,MetaDatabase> DATABASES = newPropertyList("database", MetaDatabase.class, propertyCount++);
    /** ORM parameters */
    public static final UjoProperty<MetaRoot,MetaParams> PARAMETERS = newProperty("parameters", MetaParams.class, propertyCount++);

    public MetaRoot() {
        // A default instance:
        PARAMETERS.setValue(this, new MetaParams());
    }

    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    /** Returns the first database or return null */
    public MetaDatabase getDatabase() {
        final MetaDatabase result = DATABASES.getItemCount(this)>0 ? DATABASES.getItem(this, 0) : null;
        return result;
    }

    /** Returns the first database with required name or returns null; */
    public MetaDatabase getDatabase(String name) {
        for (MetaDatabase database : DATABASES.getList(this)) {
            if (MetaDatabase.SCHEMA.equals(database, name)) {
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

    /** Pring all model in a XML format */
    public void print(Writer writer) throws IOException {
        String defaultXmlHeader = new StringBuilder(128)
          .append(UjoManagerXML.XML_HEADER)
          .append("\n<!-- OrmUjo configuration file release ")
          .append(UjoManager.projectVersion())
          .append(" -->")
          .toString()
          ;
        UjoManagerXML.getInstance().saveXML(writer, this, defaultXmlHeader, getClass());
    }

    /** Returns the first database with the same schemaName - and remove it from the list.
     * The method is for internal use only.
     * @param schemaName The identifier for looking the database
     */
    public MetaDatabase removeDb(String schemaName) {
        if (super.readOnly()) {
            throw new UnsupportedOperationException("The internal state is 'read only'");
        }
        if (isValid(schemaName)) for (MetaDatabase db : DATABASES.getList(this)) {
            if (MetaDatabase.ID.equals(db, schemaName)) {
                DATABASES.getList(this).remove(db);
                return db;
            }
        }
        return null;
    }

}
