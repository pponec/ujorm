/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.server;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.cquery.CQuery;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;

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

    public UjoTranslator getTranslator(Class ctype, int relations);

    public OrmHandler getHandler();

    /** Return all client object type instances */
    public List<Cujo> getClientObjectList() throws IllegalStateException;

    /** Translate client query to the server query. */
    public Query translate(CQuery cquery);

    /** Translate client query to the server query. */
    public Query translate(CQuery cquery, Key orderBy, PagingLoadConfig cfg);

}
