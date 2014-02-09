/*
 * Copyright 2014 pavel.
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
package org.ujorm.xsd;

import java.util.HashMap;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.orm.metaModel.MetaRoot;
import org.ujorm.xsd.domains.RootSchema;

/**
 *
 * @author pavel
 */
public class XsdBuilder {

     private final Ujo ujoRoot;
     private final RootSchema schemaRoot = new RootSchema();

     private HashMap<Class, String> set = new HashMap<Class, String>();

    public XsdBuilder(MetaRoot ujo) {
        this.ujoRoot = ujo;
    }

    public void prinXsd(Appendable out) {
        loadData(ujoRoot);
        print();

    }

    /** Load */
    public void loadData(Class<Ujo> ujoClass) {
        try {
            Ujo ujo = ujoClass.newInstance();
            loadData(ujo);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Error", ex);
        }
    }


    /** Load */
    public void loadData(Ujo ujo) {
        for (Key<?,?> key : ujo.readKeys()) {
            Class type = key.getType();
            if (!set.containsKey(type)) {
                set.put(type, type.getSimpleName());
                if (Ujo.class.isInstance(type)) {
                    loadData(type);
                }
            }
        }
    }

    /** Print */
    private void print() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
