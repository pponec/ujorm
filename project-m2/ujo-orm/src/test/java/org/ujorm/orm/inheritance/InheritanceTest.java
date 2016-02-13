/*
 *  Copyright 2009-2014 Pavel Ponec
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
package org.ujorm.orm.inheritance;

import junit.framework.TestCase;
import org.ujorm.orm.inheritance.sample.SampleOfInheritance;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class InheritanceTest extends TestCase {

    public InheritanceTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return InheritanceTest.class;
    }

    // ---------- TESTS -----------------------

    @SuppressWarnings("deprecation")
    public void testInheritance() {
        final SampleOfInheritance sample = new SampleOfInheritance();

        sample.loadMetaModel();
        sample.useInsert();
        sample.useSelect();
        sample.useSelectCountDistinct();
        sample.getPrimaryKey();
        sample.printMetadata();
    }

    // -----------------------------------------------------

    @SuppressWarnings("unchecked")
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
