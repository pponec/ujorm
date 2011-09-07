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

package org.ujorm.orm.metaModel;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.extensions.Property;
import org.ujorm.orm.AbstractMetaModel;

/**
 * Contains a SQL statement for a UJO view user SELECT.
 * @author Ponec
 */
final public class MetaSelect extends AbstractMetaModel {
    private static final Class CLASS = MetaSelect.class;

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(MetaSelect.class.getName());

    public static final Property<MetaSelect,String> SELECT = newProperty("SELECT "   , "");
    public static final Property<MetaSelect,String> FROM   = newProperty(" FROM "    , "");
    public static final Property<MetaSelect,String> WHERE  = newProperty(" WHERE "   , "");
    public static final Property<MetaSelect,String> GROUP  = newProperty(" GROUP BY ", "");
    public static final Property<MetaSelect,String> ORDER  = newProperty(" ORDER BY ", "");
    public static final Property<MetaSelect,String> LIMIT  = newProperty(" LIMIT "   , "");
    public static final Property<MetaSelect,String> OFFSET = newProperty(" OFFSET "   , "");

    /** The property initialization */
    static{init(CLASS);}

    private static String END_CHAR = ";";

    /**
     * Constructor.
     * @param select SQL SELECT, the sample of the parameter<br />
     *  SELECT DISTINCT size(*) as itemCount, ord.id as id FROM order ord, item itm
     *  WHERE ord.id=itm.orderId
     *  GROUP BY ord.id ;
     */
    public MetaSelect(String select) {
        parse(select);
    }

    /** Parse the SQL SELECT. */
    private void parse(String select) {
        String orig = select.trim();
        select = orig.toUpperCase();
        int i = select.length();
        int xi = -1;

        if (select.endsWith(END_CHAR)) {
            i -= END_CHAR.length();
            select = select.substring(i);
        }

        UjoPropertyList props = readProperties();
        UjoProperty px = props.get(0); // SELECT
        for (int j=0, max=props.size()-1; j<=max; ++j) {
            final UjoProperty p = props.get(j);
            final boolean fromLeft = p.getIndex() <= FROM.getIndex();

            i = fromLeft
              ? select.indexOf(p.getName(), xi)
              : select.lastIndexOf(p.getName())
              ;
            if (xi < i) {
                if (j>SELECT.getIndex()) {
                    final String value = orig.substring(xi, i).trim();
                    writeValue(px, value);
                    px = p;
                }
                xi = i + p.getName().length();
            }
            if (j==max) {
                final String value = orig.substring(xi).trim();
                writeValue(px, value);
            }
        }

        if (LOGGER.isLoggable(Level.INFO)) {
            final String msg = getClass().getSimpleName() + ": " + toString();
            LOGGER.log(Level.INFO, msg);
        }
    }

    /** Returns a select. */
    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        final StringBuilder r = new StringBuilder(128);
        for (UjoProperty p : readProperties()) {
            String value = (String) p.of(this);
            if (!value.isEmpty()) {
                r.append(p);
                r.append(value);
            }
        }
        return r.toString();
    }

}
