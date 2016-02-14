/*
 *  Copyright 2009-2015 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.orm.pojo.orm_tutorial;

import junit.framework.TestCase;
import org.ujorm.orm.pojo.orm_tutorial.sample.SampleOrmPojo;


/**
 * Tutorial test
 * @author Pavel Ponec
 */
public class OrmPojoTutorialTest extends TestCase {

    private static boolean noRunned = true;

    public OrmPojoTutorialTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return OrmPojoTutorialTest.class;
    }

    // ----------------------------------------------------

//    /**
//     * Test of getItemCount method, of class AbstractPropertyList.
//     */
//    public void testInit() throws Exception {
//
//        if (conn==null) {
//            Class.forName(new H2Dialect().getJdbcDriver());
//            conn = java.sql.DriverManager.getConnection
//                ( "jdbc:h2:mem:db1"
//                , "sa"
//                , ""
//                );
//            assertNotNull(conn);
//        }
//    }

    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
    public void testOrmTutorial() {
        if (noRunned) {
            noRunned = true;
            SampleOrmPojo.main(new String[]{});
            //SampleCORE.main(new String[]{});
        }
    }

    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
//    public void testTutorialExtended() {
//
//        if (noRunnedX) {
//            noRunnedX = true;
//            org.ujorm.orm_tutorial.xtended.XSampleORM.main(new String[]{});
//        }
//    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
