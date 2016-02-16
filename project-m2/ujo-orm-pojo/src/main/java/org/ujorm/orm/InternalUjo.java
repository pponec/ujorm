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

package org.ujorm.orm;

import java.io.Serializable;
import java.util.Set;
import org.ujorm.Key;
import org.ujorm.core.DefaultUjoConverter;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.implementation.orm.RelationToMany;

/**
 * The OrmUjo is a basic interface of the persistent object in the ORM support.
 * The class can be Serializable.
 * A class that implements the interface must have got a next special features:
 * <ul>
 *   <li>exactly one Key must be identified as the primary key. </li>
 *   <li>A Key for a relation type of many-to-one (for lazy lodading) must be an instance of {@link OrmKey}</li>
 *   <li>reference to a foreign BO must be able to store an object of any type by the method Ujo.writeProperty(...).
 *       This feature is necessary for the proper functioning of the lazy initialization</li>
 *   <li>relation many to one can be mapped by a RelationToMany key</li>
 *   <li>each table OrmUjo must be registered in the Database by a key type of RelationToMany</li>
 * </ul>
 *
 * @author Ponec
 * @see OrmKeyFactory
 * @see OrmKey
 * @see RelationToMany
 */
public class InternalUjo implements Serializable {
    /** Default UJORM converter */
    public static final DefaultUjoConverter<OrmUjo> CONVERTER = new DefaultUjoConverter<OrmUjo>();

    /** An empty array of the UJO keys */
    @PackagePrivate
    static final Key[] EMPTY = new Key[0];

    /** ORM session  */
    private Session session;

    /** Foreign key for an internal use */
    private ForeignKey internalFK;

    /** Set of changes */
    transient private Set<Key> changes = null;

    /** Read an ORM session where the session is an transient key. */
    public Session readSession() {
        return session;
    }

    /** Write an ORM session. */
    public void writeSession(Session session) {
        this.session = session;
    }

    /**
     * Returns keys of changed values in a time when any <strong>session</strong> is assigned.
     * The method is used by a SQL UPDATE statement to update assigned values only.
     * Implementation tip: create a new key type of {@link Set<Key>}
     * and in the method writeValue assign the current Key always.
     * @param clear True value clears all the key changes.
     * @return Key array of the modified values.
     */
    public Key[] readChangedProperties(boolean clear) {
         final Key[] result
            = (changes==null || changes.isEmpty())
            ? EMPTY
            : changes.toArray(new Key[changes.size()])
            ;
        if (clear) {
            changes = null;
        }
        return result;
    }

    /** Get an original foreign key for an internal use only.
     * The {@code non null} value means the undefined object properties of the current object.
     * @return An original foreign key can be {@code nullable} */
    public ForeignKey readInternalFK() {
        return internalFK;
    }

    /** A method to a foreign key for an internal use only.
     * @param fk New key to assign can be {@code null} */
    public void writeInternalFK(ForeignKey fk) {
        this.internalFK = fk;
    }

   /** Read the foreign key.
     * This is useful to obtain the foreign key value without (lazy) loading the entire object.
     * If the lazy object is loaded, the method will need the Session to build the ForeignKey instance.
     * <br>NOTE: The method is designed for developers only, the Ujorm doesn't call it newer.
     * @param key Must be direct key only ({@link Key#isDirect()}==true)
     * @return If no related object is available, then the result has the NULL value.
     * @throws IllegalStateException Method throws an exception for a wrong key type.
     * @throws NullPointerException Method throws an exception if a Session is missing after a lazy initialization of the key.
     */
    public <UJO extends OrmUjo> ForeignKey readFK(OrmUjo ormUjo, Object value, Key<UJO, ? extends OrmUjo> key) throws IllegalStateException {
        if (value==null) {
            return null;
        }

        final ForeignKey fk = value instanceof OrmUjo ? ((OrmUjo)value).readInternalFK() : null;
        if (fk != null) {
            return fk;
        }

        if (session!=null) {
            final OrmUjo ujo = value instanceof OrmUjo
                    ? (OrmUjo) value
                    : ormUjo ;
            return session.readFK(ujo, key);
        }
        throw new NullPointerException("Can't get FK by the key '"
                + key.getFullName()
                + "' due a missing Session");
    }

}
