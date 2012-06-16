/*
 *  Copyright 2012-2012 Pavel Ponec
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

package org.ujorm.spring;

/**
 * Test implementation of the AbstractAplicationContextAdapter.
 * @author Pavel Ponec
 */
public class AplicationContextAdapter extends AbstractAplicationContextAdapter {
    
    /** Key with a name: summySpringController */
    public static final SpringKey<DummySpringController> springController = newSpringKey();
    /** Key with a name: springService */
    public static final SpringKey<DummySpringService> dummySpringService = newSpringKey("springService");
    

}
