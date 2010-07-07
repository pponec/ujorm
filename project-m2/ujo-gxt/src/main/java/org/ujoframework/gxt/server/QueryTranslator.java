/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.server;

import org.ujoframework.gxt.client.Cujo;
import org.ujoframework.gxt.client.CujoProperty;
import org.ujoframework.gxt.client.cquery.CBinaryCriterion;
import org.ujoframework.gxt.client.cquery.CCriterion;
import org.ujoframework.gxt.client.cquery.CQuery;
import org.ujoframework.gxt.client.cquery.CValueCriterion;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.criterion.BinaryOperator;
import org.ujoframework.criterion.Criterion;
import org.ujoframework.criterion.Operator;
import org.ujoframework.extensions.Property;
import org.ujoframework.gxt.client.CEnum;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.metaModel.MetaTable;

/**
 * Query translator
 * @author Pavel Ponec
 */
public class QueryTranslator<UJO extends OrmUjo> {
    /** Base type */
    private final Class<? extends UJO> type;
    private final CQuery cquery;
    private final UjoManager manager = UjoManager.getInstance();
    private final OrmHandler handler;
    private final IServerClassConfig config;

    @SuppressWarnings("unchecked")
    public QueryTranslator(CQuery cquery, OrmHandler handler, IServerClassConfig config) {
        if (!cquery.isRestored()) {
            try {
                cquery.restore(Class.forName(cquery.getTypeName()));
            } catch (ClassNotFoundException e) {
                String msg = "Can't get Class for the type: " + cquery.getTypeName();
                throw new IllegalStateException(msg);
            }
        }

        this.cquery = cquery;
        this.type = (Class<UJO>) config.getServerClass(cquery.getTypeName());
        this.handler = handler;
        this.config = config;

        if (type == null) {
            throw new IllegalStateException("Initializaton bug for type: " + cquery.getTypeName());
        }
    }

    /** The query is translated without Session. Assign an open Session before excuting a database query. */
    @SuppressWarnings({"unchecked"})
    public Query<UJO> translate() {
        Criterion cn = getCriterion();
        MetaTable metaTable = handler.findTableModel(type);
        Query<UJO> result = new Query(metaTable, cn);
        result.orderBy(orderBy(cquery.getOrderBy()));
        return result;
    }

    public Query<UJO> translate(Session session) {
        Query<UJO> result = translate();
        result.setSession(session);
        return result;
    }


    public Criterion getCriterion() {
        return getCriterion(cquery.getCriterion());
    }

    @SuppressWarnings("unchecked")
    protected Criterion getCriterion(CCriterion ccriterion) {
        Criterion result = null;
        if (ccriterion == null) {
            return result;
        }

        if (ccriterion.isBinary()) {
            final CBinaryCriterion c = (CBinaryCriterion) ccriterion;

            Criterion c1 = getCriterion(c.getLeftNode());
            Criterion c2 = getCriterion(c.getRightNode());
            BinaryOperator opt = BinaryOperator.valueOf(c.getOperator().getEnum().name());
            if (c2 != null) {
                return c1.join(opt, c2);
            } else {
                return c1;
            }
        } else {

            final CValueCriterion c = (CValueCriterion) ccriterion;

            CujoProperty c1 = c.getLeftNode();
            Object c2 = c.getRightNode();
            //
            UjoProperty p1;
            try {
                p1 = manager.findIndirectProperty(type, c1.getName());
            } catch (IllegalArgumentException e) {
                p1 = Property.newInstance("["+c1.getName()+"]\u0020", Object.class);
            }
            Object p2;

            if (c2 instanceof CujoProperty) {
                p2 = manager.findIndirectProperty(type, ((CujoProperty) c2).getName());
            } else if (c2 instanceof Cujo) {
                UjoTranslator translator = new UjoTranslator(
                        ((Cujo) c2).readProperties(),
                        UjoManager.getInstance().readProperties(p1.getType()),
                        config);
                p2 = translator.translateToServer((Cujo) c2);
            } else if (c2 instanceof Date && p1.isTypeOf(java.sql.Date.class)) {
                p2 = new java.sql.Date(((Date) c2).getTime());
            } else {
                p2 = c2;
            }

            Operator opt = Operator.valueOf(c.getOperator().getEnum().name());
            return Criterion.where(p1, opt, p2);
        }
    }

    public static <UJO extends OrmUjo> QueryTranslator<UJO> newInstance(CQuery cquery, OrmHandler handler, IServerClassConfig config) {
        return new QueryTranslator<UJO>(cquery, handler, config);
    }

    /** Convert from Cujo.orderBy to Ujo.orderBy */
    public List<UjoProperty> orderBy(List<CujoProperty> properties) {
        List<UjoProperty> result = new ArrayList<UjoProperty>();

        if (properties != null) {
            for (CujoProperty cp : properties) {
                UjoProperty up = manager.findIndirectProperty(type, cp.getName());
                result.add(cp.isAscending() ? up : up.descending());
            }
        }
        return result;
    }
}
