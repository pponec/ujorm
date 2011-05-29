/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client;

import org.ujorm.gxt.client.tools.ColorGxt;
import java.io.Serializable;
import java.util.Date;

/**
 * The client GWT serializable object.
 * @author Pavel Ponec
 */
public class ClientObject implements Serializable {

    private String value;
    private AbstractCujo valueCujo;
    private String type;

    /** Serializable constructor */
    protected ClientObject() {
    }

    /** Common constructor */
    public ClientObject(Object aValue) {

        if (aValue == null) {
            type = null;
            value = null;
        } else if (ClientClassConfig.isCujo(aValue)) {
            type = null;
            valueCujo = (AbstractCujo) aValue;
        } else {
            Class aType = aValue.getClass();
            type = aType.getName();

            if (Byte.class.equals(aType)) {
                value = String.valueOf((int) (Byte) aValue);
            } else if (Date.class.equals(aType)) {
                value = String.valueOf(((Date) aValue).getTime());
            } else {
                value = aValue.toString();
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getTextValue() {
        return value;
    }

    /** Restore the original value */
    public Object getValue() {

        if (valueCujo != null) {
            return valueCujo;
        }
        if (type == null) {
            return null;
        }
        if (String.class.getName().equals(type)) {
            return value;
        }
        if (Byte.class.getName().equals(type)) {
            return new Integer(value).byteValue();
        }
        if (Short.class.getName().equals(type)) {
            return new Integer(value).shortValue();
        }
        if (Integer.class.getName().equals(type)) {
            return new Integer(value).intValue();
        }
        if (Long.class.getName().equals(type)) {
            return new Long(value).longValue();
        }
        if (Float.class.getName().equals(type)) {
            return new Float(value);
        }
        if (Double.class.getName().equals(type)) {
            return new Double(value).doubleValue();
        }
        if (Date.class.getName().equals(type)) {
            return new Date(new Long(value));
        }
        if (Boolean.class.getName().equals(type)) {
            return "true".equals(value);
        }
        if (CEnum.class.getName().equals(type)) {
            return new CEnum(0 /* not used */, value);
        }
        if (ColorGxt.class.getName().equals(type)) {
            return new ColorGxt(value);
        }
        
        String msg = "Unsupported serialization object: '" + value + "' type of class " + type;
        throw new RuntimeException(msg);
    }
}
