/*
 *  Don't modify the generated class.
 *  License the Apache License, Version 2.0
 */
package org.bo.gen;

import org.ujorm.extensions.Property;
import org.ujorm.orm.DbProcedure;
import org.ujorm.orm.annot.Parameter;
import org.ujorm.orm.annot.Procedure;

@Procedure(name = "ujorm_test")
abstract public class _MyProcedure extends DbProcedure<_MyProcedure> {

    /** The first parameter is the OUTPUT allways. If the stored procedure has no return parameter, use the type: java.lang.Void */
    public static final Property<_MyProcedure, Integer> result = newProperty(Integer.class);
    /** INPUT parameter */
    @Parameter(input = true)
    public static final Property<_MyProcedure, Integer> paramCode = newProperty(0);
    /** INPUT parameter */
    public static final Property<_MyProcedure, Boolean> paramEnabled = newProperty(false);

    public Integer getResult() {
        return result.getValue(this);
    }

    public void setResult(Integer _result) {
        result.setValue(this, _result);
    }

    public Integer getParamCode() {
        return paramCode.getValue(this);
    }

    public void setParamCode(Integer _paramCode) {
        paramCode.setValue(this, _paramCode);
    }

    public Boolean getParamEnabled() {
        return paramEnabled.getValue(this);
    }

    public void setParamEnabled(Boolean _paramEnabled) {
        paramEnabled.setValue(this, _paramEnabled);
    }
}
