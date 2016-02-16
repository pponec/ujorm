/*
 *  Copyright 2010 Ponec.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ujorm.orm.pojo.orm_tutorial.sample.entity;

import org.ujorm.orm.annot.Parameter;
import org.ujorm.orm.annot.Procedure;

/**
 * The API for a database stored procedure:
 * <code>
 * CREATE OR REPLACE FUNCTION db1.ujorm_test(integer, boolean) RETURNS integer AS 'select $1 + $1;'
 *    LANGUAGE SQL
 *    IMMUTABLE
 *    RETURNS NULL ON NULL INPUT;
 * </code>
 * @author Pavel Ponec
 */
@Procedure(name="ujorm_test")
public class MyProcedure {

    /** The first parameter is the OUTPUT always. <br/>
     * If the stored procedure has no return parameter, set the first key type to: java.lang.Void */
    private Integer result;
    /** INPUT parameter */
    @Parameter(input=true)
    private Integer paramCode = 0;
    /** INPUT parameter */
    private Boolean paramEnabled = false;

    // --- Getters and Setters ---

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Integer getParamCode() {
        return paramCode;
    }

    public void setParamCode(Integer paramCode) {
        this.paramCode = paramCode;
    }

    public Boolean getParamEnabled() {
        return paramEnabled;
    }

    public void setParamEnabled(Boolean paramEnabled) {
        this.paramEnabled = paramEnabled;
    }

}
