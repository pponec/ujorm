/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.ujorm.validator;

import org.ujorm.*;
import org.ujorm.Validator.Build;
import org.ujorm.core.KeyFactory;
import org.ujorm.extensions.AbstractUjo;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class Relation extends AbstractUjo {

    /** Factory */
    private static final KeyFactory<Relation> f = newFactory(Relation.class);
    public static final Key<Relation, Long> PID = f.newKey(Build.notNull());
    public static final Key<Relation, Integer> CODE = f.newKey(Build.between(0, 10));
    public static final Key<Relation, String> NAME = f.newKey(Build.regexp("T.*T"));
    public static final Key<Relation, Double> CASH = f.newKeyDefault(0.0, Build.min(0.0));

    static {
        f.lock();
        CODE.getDefault();
    }

    public Relation() {
    }

    public Relation(Double cash) {
        CASH.setValue(this, cash);
    }

    public Relation(Integer code) {
        CODE.setValue(this, code);
    }
}
