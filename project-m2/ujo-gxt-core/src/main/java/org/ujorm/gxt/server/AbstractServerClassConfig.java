/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2012 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.server;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import org.ujorm.Key;
import org.ujorm.gxt.client.cquery.CQuery;
import org.ujorm.orm.Query;

/**
 * Inicialization the CUJO objects + mapping to BO.
 * Scope is an Singleton.
 * @author Pavel Ponec
 */
abstract public class AbstractServerClassConfig implements IServerClassConfig {


    /** Translate client query to the server query. */
    @Override
    public Query translate(CQuery cquery) {
        return translate(cquery, null, null);
    }

    /** Translate client query to the server query. */
    @Override
    public Query translate(CQuery cquery, Key orderBy, PagingLoadConfig cfg) {

        Query query = QueryTranslator.newInstance(cquery, getHandler(), this).translate();

        if (cfg!=null) {
            if (cfg.getLimit()!=Integer.MAX_VALUE) {
                query.setLimit(cfg.getLimit());
            }

            if (cfg.getOffset()!=0) {
                query.setOffset(cfg.getOffset());
            }
        }

        if (orderBy != null) {
            query.getOrderBy().add(0, orderBy); // The first orderBy
        }

        return query;
    }


}
