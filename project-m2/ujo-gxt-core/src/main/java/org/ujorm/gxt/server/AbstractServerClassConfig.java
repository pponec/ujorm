/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm.gxt.server;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import org.ujorm.UjoProperty;
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
    public Query translate(CQuery cquery, UjoProperty orderBy, PagingLoadConfig cfg) {

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
