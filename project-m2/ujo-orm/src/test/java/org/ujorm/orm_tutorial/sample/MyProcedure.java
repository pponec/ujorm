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

package org.ujorm.orm_tutorial.sample;

import org.ujorm.extensions.Property;
import org.ujorm.orm.DbProcedure;
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
public final class MyProcedure extends DbProcedure<MyProcedure> {

    /** The first parameter is the OUTPUT allways. <br/>
     * If the stored procedure has no return parameter, set the first property type to: java.lang.Void */
    public static final Property<MyProcedure,Integer> RESULT = newProperty(Integer.class);
    /** INPUT parameter */
    @Parameter(input=true)
    public static final Property<MyProcedure,Integer> PARAM_CODE = newProperty(0);
    /** INPUT parameter */
    public static final Property<MyProcedure,Boolean> PARAM_ENABLED = newProperty(false);


}
