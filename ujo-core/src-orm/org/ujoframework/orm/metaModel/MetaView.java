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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.AbstractMetaModel;

/**
 * Contains a sql select for a UJO view SELECT.
 * @author Ponec
 */
public class MetaView  extends AbstractMetaModel {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(MetaView.class.getName());
    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    public static final UjoProperty<MetaView,String> SELECT = newProperty("SELECT "   , "", propertyCount++);
    public static final UjoProperty<MetaView,String> FROM   = newProperty(" FROM "    , "", propertyCount++);
    public static final UjoProperty<MetaView,String> WHERE  = newProperty(" WHERE "   , "", propertyCount++);
    public static final UjoProperty<MetaView,String> GROUP  = newProperty(" GROUP BY ", "", propertyCount++);
    public static final UjoProperty<MetaView,String> ORDER  = newProperty(" ORDER BY ", "", propertyCount++);
    public static final UjoProperty<MetaView,String> LIMIT  = newProperty(" LIMIT "   , "", propertyCount++);

    private static String END_CHAR = ";";

    /**
     * Constructor.
     * @param select SQL SELECT, the sample of the parameter<br />
     *  SELECT DISTINCT count(*) as itemCount, ord.id as id FROM order ord, item itm
     *  WHERE ord.id=itm.orderId
     *  GROUP BY ord.id ;
     */
    public MetaView(String select) {
        parse(select);
    }

    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    /** Parse the SQL SELECT. */
    private void parse(String select) {
        String orig = select.trim();
        select = orig.toUpperCase();
        int i = select.length();

        if (select.endsWith(END_CHAR)) {
            i -= END_CHAR.length();
            select = select.substring(i);
        }

        UjoProperty[] props = readProperties();
        for (int j=props.length-1; j>=0; --j) {
            UjoProperty p = props[j];

            i = select.lastIndexOf(p.getName());
            if (i>=0) {
                String value = orig.substring(i + p.getName().length(), select.length()).trim();
                writeValue(p, value);
                select = select.substring(0, i);
            }
        }

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, getClass().getSimpleName() + ": " + toString());
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
