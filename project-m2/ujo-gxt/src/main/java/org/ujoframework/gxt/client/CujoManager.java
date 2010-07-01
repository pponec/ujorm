/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client Ujo Manager.
 * @author Pavel Ponec
 */
public class CujoManager {

    static private Map<Class, CujoPropertyList> map = new HashMap<Class, CujoPropertyList>();

    static void add(Class type, CujoPropertyList pl) {
        if (map.containsKey(type)) {
            String msg = "The key '" + type.getName() + "' is already occupied in the Cujo type repository";
            throw new IllegalArgumentException(msg);
        } else {
            map.put(type, pl);
        }
    }

    static public CujoPropertyList find(Class type) {
        if (map.size()==0) {
            ClientClassConfig.getInstance(); // reinitialization
        }

        CujoPropertyList result = map.get(type);
        if (result==null) {
            throw new IllegalArgumentException("The type is not found: " + type);
        }

        return result;
    }

    /** Find <strong>direct</strong> property by the name */
    static public CujoProperty findProperty(Class cujoType, String name) {
        final CujoPropertyList properties = map.get(cujoType);
        return properties.findProperty(name);
    }

    /** Find <strong>indirect</strong> property by the name */
    @SuppressWarnings("unchecked")
    public static CujoProperty findIndirectProperty(Class ujoType, String names) {

        int j, i = 0;
        List<CujoProperty> props = new ArrayList<CujoProperty>(4);
        names += ".";

        while ((j = names.indexOf('.', i + 1)) >= 0) {
            final String name = names.substring(i, j);
            final CujoProperty p = findProperty(ujoType, name);
            props.add(p);
            ujoType = p.getType();
            i = j + 1;
        }
        switch (props.size()) {
            case 0:
                throw new IllegalStateException("Invalid property name: " + names);
            case 1:
                return props.get(0);
            default:
                return new CPathProperty(null, props);
        }
    }
}
