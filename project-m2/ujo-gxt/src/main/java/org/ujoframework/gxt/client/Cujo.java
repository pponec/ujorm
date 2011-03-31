/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * Client Ujo
 * @author Pavel Ponec
 */
public interface Cujo extends ModelData {

    public CujoPropertyList readProperties();

    public <UJO extends Cujo,VALUE> VALUE get(CujoProperty<UJO,VALUE> property);

    public <UJO extends Cujo,VALUE> void set(CujoProperty<UJO,VALUE> property, VALUE value);

    /** Create new instance from the object */
    public <T extends Cujo> T createInstance();

}
