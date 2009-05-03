/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class OrmView  extends AbstractMetaModel {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(OrmView.class.toString());
    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    public static final UjoProperty<OrmView,String> SELECT = newProperty("SELECT "   , "", propertyCount++);
    public static final UjoProperty<OrmView,String> FROM   = newProperty(" FROM "    , "", propertyCount++);
    public static final UjoProperty<OrmView,String> WHERE  = newProperty(" WHERE "   , "", propertyCount++);
    public static final UjoProperty<OrmView,String> GROUP  = newProperty(" GROUP BY ", "", propertyCount++);
    public static final UjoProperty<OrmView,String> ORDER  = newProperty(" ORDER BY ", "", propertyCount++);
    public static final UjoProperty<OrmView,String> LIMIT  = newProperty(" LIMIT "   , "", propertyCount++);

    public static String END_CHAR = ";";

    /**
     * Constructor.
     * @param select SQL SELECT, the sample of the parameter<br />
     *  SELECT DISTINCT count(*) as itemCount, ord.id as id FROM order ord, item itm
     *  WHERE ord.id=itm.orderId
     *  GROUP BY ord.id ;
     */
    public OrmView(String select) {
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
