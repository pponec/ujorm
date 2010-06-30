/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.server;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import java.util.List;
import org.ujoframework.UjoProperty;
import org.ujoframework.gxt.client.Cujo;
import org.ujoframework.gxt.client.cquery.CQuery;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.Session;

/**
 * Inicialization the CUJO objects + mapping to BO
 * @author Pavel Ponec
 */
public interface IServerClassConfig {
    
    
    /** Convert CType to UjoClass */
    public Class<? extends OrmUjo> getServerClass(String ctype) throws IllegalStateException;

    public Class<? extends OrmUjo> getServerClassOrNull(String ctype) throws IllegalStateException;

    /** Convert CType to UjoClass */
    public OrmUjo newServerObject(String ctype) throws IllegalStateException;

    public UjoTranslator getTranslator(Class ctype, boolean relations);

    public OrmHandler getHandler();

    /** Return all client object type instances */
    public List<Cujo> getClientObjectList() throws IllegalStateException;

    /** Translate client query to the server query. */
    public Query translate(CQuery cquery);

    /** Translate client query to the server query. */
    public Query translate(CQuery cquery, UjoProperty orderBy, PagingLoadConfig cfg);

}
