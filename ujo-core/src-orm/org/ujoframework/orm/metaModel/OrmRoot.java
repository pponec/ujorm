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
import org.ujoframework.core.UjoManagerXML;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;

/**
 * A logical database description.
 * The class is a root of database configuration.
 * @author pavel
 * @composed 1 - * OrmDatabase
 * @composed 1 - 1 OrmParameters
 */
public class OrmRoot extends AbstractMetaModel {

    public static final Logger LOGGER = Logger.getLogger(OrmRoot.class.getName());


    /** List of tables */
    public static final ListProperty<OrmRoot,OrmDatabase> DATABASES = newPropertyList("database", OrmDatabase.class);

    /** ORM parameters */
    public static final UjoProperty<OrmRoot,OrmParameters> PARAMETERS = newProperty("parameters", OrmParameters.class);

    public OrmRoot() {
        // A default instance:
        PARAMETERS.setValue(this, new OrmParameters());
    }


    /** Returns the first database or return null */
    public OrmDatabase getDatabase() {
        final OrmDatabase result = DATABASES.getItemCount(this)>0 ? DATABASES.getItem(this, 0) : null;
        return result;
    }

    /** Returns the first database with required name or returns null; */
    public OrmDatabase getDatabase(String name) {
        for (OrmDatabase database : DATABASES.getList(this)) {
            if (OrmDatabase.NAME.equals(database, name)) {
                return database;
            }
        }
        return null;
    }

    /** Add a new database into repository. */
    final public void add(OrmDatabase database) {
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
        final String defaultXmlHeader = null;
        UjoManagerXML.getInstance().saveXML(writer, this, defaultXmlHeader, getClass());
    }

}
