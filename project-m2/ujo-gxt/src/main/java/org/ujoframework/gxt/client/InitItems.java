/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;


import java.io.Serializable;
import java.util.List;

/**
 * Enumeration items and Client items transport
 * @author Pavel Ponec
 */
public class InitItems implements Serializable {

    private EnumItems enumItems;
    private List<Cujo> cujos;

    protected InitItems() {
    }

    public InitItems(EnumItems enumItems, List<Cujo> cujos) {
        this.enumItems = enumItems;
        this.cujos = cujos;
    }

    public List<Cujo> getCujos() {
        return cujos;
    }

    public EnumItems getEnumItems() {
        return enumItems;
    }

}
