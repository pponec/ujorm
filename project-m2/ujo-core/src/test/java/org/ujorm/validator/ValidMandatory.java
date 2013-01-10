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
public class ValidMandatory extends AbstractUjo {

    /** Factory */
    private static final KeyFactory<ValidMandatory> f = newFactory(ValidMandatory.class);
    /** Documentation: */
    public static final Key<ValidMandatory, Long> PID = f.newKey(notNull());
    public static final Key<ValidMandatory, Integer> CODE = f.newKey(between(MANDATORY, 0, 10));
    public static final Key<ValidMandatory, String> NAME = f.newKey(regexp(MANDATORY, "T.*T"));
    public static final Key<ValidMandatory, Double> CASH = f.newKey(min(MANDATORY, 0.0));
    /** Test Properties: */
    public static final Key<ValidMandatory, String> NOTNULL = f.newKey(notNull());
    public static final Key<ValidMandatory, String> NOT_EMPTY = f.newKey(notEmpty());
    public static final Key<ValidMandatory, String> NOT_BLANK = f.newKey(notBlank());
    public static final Key<ValidMandatory, Double> MAX_10 = f.newKey(max(MANDATORY, 10.0));
    public static final Key<ValidMandatory, Double> MIN_10 = f.newKey(min(MANDATORY, 10.0));
    public static final Key<ValidMandatory, Integer> FORBIDDEN_1_3 = f.newKey(forbidden(MANDATORY, 1, 3));
    public static final Key<ValidMandatory, Integer> REQUIRED_1_3 = f.newKey(required(MANDATORY, 1, 3));
    public static final Key<ValidMandatory, String> LENGTH_MIN_3 = f.newKey(length(MANDATORY, 3));
    public static final Key<ValidMandatory, String> LENGTH_2_4 = f.newKey(length(MANDATORY, 2,4));
    public static final Key<ValidMandatory, Integer> BETWEEN_1_10 = f.newKey(between(MANDATORY, 1, 10));
    public static final Key<ValidMandatory, Integer> RANGE_1_10 = f.newKey(range(MANDATORY, 1, 10));
    public static final Key<ValidMandatory, Relation> CRN_CODE_3 = f.newKey(relation(MANDATORY, Relation.CODE.whereEq(3)));
    public static final Key<ValidMandatory, Date> FUTURE = f.newKey(future(MANDATORY));
    public static final Key<ValidMandatory, Date> PAST = f.newKey(past(MANDATORY));
    public static final Key<ValidMandatory, String> REG_EXP = f.newKey(regexp(MANDATORY, "T.*T"));
    public static final Key<ValidMandatory, String> MAIL = f.newKey(email(MANDATORY));
    public static final Key<ValidMandatory, String> READ_ONLY = f.newKey(readOnly());
    public static final Key<ValidMandatory, String> ALL_ALLOWED = f.newKey(allAllowed());
    public static final Key<ValidMandatory, Integer> COMPOSITE_AND = f.newKey(min(0).and(max(10)).and(notNull()));
    public static final Key<ValidMandatory, Integer> COMPOSITE_OR = f.newKey(max(0).or(min(10)).and(notNull()));
    public static final Key<ValidMandatory, Number> NUMBER_TYPE = f.newKey(type(MANDATORY));
    public static final Key<ValidMandatory, Number> NUMBER_TYPE_EXPL = f.newKey(type(MANDATORY, Number.class));

    static {
        f.lock();
    }
}
