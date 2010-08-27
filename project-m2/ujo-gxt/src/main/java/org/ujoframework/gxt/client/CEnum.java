/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;

import java.io.Serializable;

/**
 * Client ENUM.
 * @author Pavel Ponec, Tomas Hampl
 */
public class CEnum extends AbstractCujo implements Serializable {

    private static final CujoPropertyList pl = list(CEnum.class);
    /** Primary Key */
    public static final CujoProperty<CEnum, Integer> id = pl.newProperty("id", Integer.class);
    /** UID */
    public static final CujoProperty<CEnum, String> name = pl.newProperty("name", String.class);

    public CEnum() {
    }

    public CEnum(int _id, String _name) {
        id.setValue(this, _id);
        name.setValue(this, _name);
    }

    @Override
    public CujoPropertyList readProperties() {
        return pl;
    }

    public Integer getId() {
        return CEnum.id.getValue(this);
    }
    public String getName() {
        return CEnum.name.getValue(this);
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        if (obj==null) {
            return false;
        }
        int i1 = id.getValue(this);
        int i2 = id.getValue((CEnum)obj);

        return i1==i2;
    }

    @Override
    public String toString() {
        return getName();
    }

}
