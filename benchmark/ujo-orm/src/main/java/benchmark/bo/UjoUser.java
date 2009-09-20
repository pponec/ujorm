/*
 *  Copyright 2009 Paul Ponec
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


package benchmark.bo;

import org.ujoframework.UjoPropertyList;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.orm.annot.Column;

/**
 *
 * @author pavel
 */
public class UjoUser extends OrmTable<UjoUser> {

    @Column(pk=true)
    public static final Property<UjoUser,Long> id = newProperty("id", Long.class);
    @Column(lenght=8)
    public static final Property<UjoUser,String> personalId = newProperty(String.class);
    public static final Property<UjoUser,String> surename = newProperty(String.class);
    public static final Property<UjoUser,String> lastname = newProperty(String.class);



    // Optional code for better performance:
    private static UjoPropertyList properties = init(UjoUser.class);
    @Override public UjoPropertyList readProperties() { return properties; }
}
