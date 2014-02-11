/*
 *  Copyright 2014-2014 Pavel Ponec
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
package org.ujorm.xsd;

import junit.framework.TestCase;
import org.ujorm.xsd.domains.Customer1;

/**
 * Test of the XSD Builder
 * @author Pavel Ponec
 */
public class XsdBuilderTest extends TestCase {

    public XsdBuilderTest(String testName) {
        super(testName);
    }

    /**
     * Test of print method, of class XsdBuilder.
     */
    public void testBuild_1() {
        System.out.println("testBuild_1");
        XsdBuilder builder = new XsdBuilder(Customer1.class);
        String result = builder.print();
        System.out.println("result:" + result);

    }
}
