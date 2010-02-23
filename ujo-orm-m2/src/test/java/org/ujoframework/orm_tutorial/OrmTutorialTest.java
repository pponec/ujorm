/*
 *  Copyright 2009-2010 Pavel Ponec
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

package org.ujoframework.orm_tutorial;

import junit.framework.TestCase;
import org.ujoframework.orm_tutorial.sample.SampleORM;


/**
 * Tutorial test
 * @author Pavel Ponec
 */
public class OrmTutorialTest extends TestCase {

    private static boolean noRunned = true;
    
    public OrmTutorialTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return OrmTutorialTest.class;
    }

    // ----------------------------------------------------

    /**
     * Test of getItemCount method, of class AbstractPropertyList.
     */
    public void testTutorial() {

        if (noRunned) {
            noRunned = true;
            String[] params = new String[] {};
            SampleORM.main(params);            
        }

    }
 

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
