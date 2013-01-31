/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.ujorm.validator;

import java.util.Date;
import org.ujorm.*;
import org.ujorm.core.KeyFactory;
import org.ujorm.extensions.AbstractUjo;
import static org.ujorm.validator.impl.ValidatorFactory.*;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class ValidBo extends AbstractUjo {

    /** Factory */
    private static final KeyFactory<ValidBo> f = newFactory(ValidBo.class);
    /** Documentation: */
    public static final Key<ValidBo, Long> PID = f.newKey(notNull());
    public static final Key<ValidBo, Integer> CODE = f.newKey(between(0, 10));
    public static final Key<ValidBo, String> NAME = f.newKey(regexp("T.*T"));
    public static final Key<ValidBo, Double> CASH = f.newKey(min(0.0).and(notNull()));
    /** Test Properties: */
    public static final Key<ValidBo, String> NOT_NULL = f.newKey(notNull());
    public static final Key<ValidBo, String> NOT_EMPTY = f.newKey(notEmpty());
    public static final Key<ValidBo, String> NOT_BLANK = f.newKey(notBlank());
    public static final Key<ValidBo, Double> MAX_10 = f.newKey(max(10.0));
    public static final Key<ValidBo, Double> MIN_10 = f.newKey(min(10.0));
    public static final Key<ValidBo, Integer> FORBIDDEN_1_3 = f.newKey(forbidden(1, 3));
    public static final Key<ValidBo, Integer> REQUIRED_1_3 = f.newKey(required(1, 3));
    public static final Key<ValidBo, String> LENGTH_MAX_3 = f.newKey(length(3));
    public static final Key<ValidBo, String> LENGTH_2_4 = f.newKey(length(2,4));
    public static final Key<ValidBo, Integer> BETWEEN_1_10 = f.newKey(between(1, 10));
    public static final Key<ValidBo, Integer> RANGE_1_10 = f.newKey(range(1, 10));
    public static final Key<ValidBo, Relation> CRN_CODE_3 = f.newKey(relation(Relation.CODE.whereEq(3)));
    public static final Key<ValidBo, Date> FUTURE = f.newKey(future());
    public static final Key<ValidBo, Date> PAST = f.newKey(past());
    public static final Key<ValidBo, String> REG_EXP = f.newKey(regexp("T.*T"));
    public static final Key<ValidBo, String> MAIL = f.newKey(email());
    public static final Key<ValidBo, String> READ_ONLY = f.newKey(readOnly());
    public static final Key<ValidBo, String> ALL_ALLOWED = f.newKey(everything());
    public static final Key<ValidBo, Integer> COMPOSITE_AND = f.newKey(min(0).and(max(10)));
    public static final Key<ValidBo, Integer> COMPOSITE_OR = f.newKey(max(0).or(min(10)));
    public static final Key<ValidBo, Number> NUMBER_TYPE = f.newKey(type());
    public static final Key<ValidBo, Number> NUMBER_TYPE_EXPL = f.newKey(type(Number.class));

    static {
        f.lock();
    }
}
