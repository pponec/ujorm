/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.db.sample.BoOrder;
import org.ujoframework.tools.criteria.Expression;

/**
 *
 * @author pavel
 */
public class Query<UJO extends Ujo> {

    public Query(Class<UJO> aClass, Expression<UJO> expA) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readOnly(boolean b) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <ITEM> void setParameter(UjoProperty<UJO,ITEM> property, ITEM value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void sizeRequired(boolean b) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
