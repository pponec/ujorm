/*
 * Copyright 2012 ponec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.extensions;

import java.util.Date;

/**
 * Simple object
 * @author ponec
 */
public class WeakKeyCollectionSample {

    private static final WeakKeyFactory f = new WeakKeyFactory(WeakKeyCollectionSample.class); 
    
    public static final WeakKey<String> NAME = f.newKey();
    public static final WeakKey<Date>   BORN = f.newKey();
    public static final WeakKey<Double> CASH = f.newKeyDefault(0.0);
    public static final WeakKey<Boolean> WIFE = f.newKeyDefault(true);
    
    static {
        f.lock();
    }
    
}
