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
import org.ujorm.logger.UjoLogger;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.PropertyFactory;
import org.ujorm.core.annot.Immutable;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.AbstractMetaModel;

/**
 * Contains a SQL statement for a UJO view user SELECT.
 * @author Ponec
 */
@Immutable
final public class MetaSelect extends AbstractMetaModel {
    private static final Class CLASS = MetaSelect.class;

    /** Schema variable is replaced for a real schema name. */
    public static final String SCHEMA = "${SCHEMA}";

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(MetaSelect.class);

    /** Property Factory */
    private static final PropertyFactory<MetaSelect> fa = PropertyFactory.CamelBuilder.get(CLASS);
    public static final UjoProperty<MetaSelect,String> SELECT = fa.newProperty("SELECT "   , "");
    public static final UjoProperty<MetaSelect,String> FROM   = fa.newProperty(" FROM "    , "");
    public static final UjoProperty<MetaSelect,String> WHERE  = fa.newProperty(" WHERE "   , "");
    public static final UjoProperty<MetaSelect,String> GROUP  = fa.newProperty(" GROUP BY ", "");
    public static final UjoProperty<MetaSelect,String> ORDER  = fa.newProperty(" ORDER BY ", "");
    public static final UjoProperty<MetaSelect,String> LIMIT  = fa.newProperty(" LIMIT "   , "");
    public static final UjoProperty<MetaSelect,String> OFFSET = fa.newProperty(" OFFSET "  , "");

    /** The property initialization */
    static{fa.lock();}

    private static String END_CHAR = ";";

    /**
     * Constructor.
     * @param select SQL SELECT, the sample of the parameter<br />
     *  SELECT DISTINCT size(*) as itemCount, ord.id as id FROM order ord, item itm
     *  WHERE ord.id=itm.orderId
     *  GROUP BY ord.id ;
     */
    public MetaSelect(String select, String schema) {
        parse(modifySchema(select, schema));
    }

    public MetaSelect(MetaTable view) {
        this( MetaTable.SELECT.of(view)
            , MetaTable.SCHEMA.of(view)
            ) ;
    }

    /** Replace Schema for value */
    private String modifySchema(String select, String schema) {
        int j=0, i=select.indexOf(SCHEMA);
        if (i<0) {
            return select;
        }
        final boolean emptySchema = MetaTable.SCHEMA.getDefault().equals(schema);
        final StringBuilder sb = new StringBuilder(select.length()+3);

        while (i>=0) {
            final boolean constant = i>0 && select.charAt(i-1)=='\\';
            if (constant) {
                sb.append(select.substring(j, i-1));
                sb.append(SCHEMA);
            } else {
                sb.append(select.substring(j, i));
                sb.append(schema);
            }

            j = i + SCHEMA.length();
            if (emptySchema && select.charAt(j)=='.' && !constant) {
                ++j;
            }
            i = select.indexOf(SCHEMA, j);
        }
        sb.append(select.substring(j));
        return sb.toString();
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
            if (value.length()>0) {
                r.append(p);
                r.append(value);
            }
        }
        return r.toString();
    }

}