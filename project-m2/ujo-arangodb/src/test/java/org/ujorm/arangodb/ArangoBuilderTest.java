/*
 * Copyright 2021 pavel.
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
package org.ujorm.arangodb;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.mapping.ArangoJack;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Test;

/**
 *
 * @author pavel
 */
public class ArangoBuilderTest {

    public ArangoBuilderTest() {
    }

    @Test
    public void testBulder() {

        String dbName = "myTestDb";
        String collectionName = "firstCollection";

        ArangoDB arangoDB = new ArangoDB.Builder()
                .serializer(new ArangoJack())
                .build();
        {
            createDB(arangoDB, dbName);
            createCollection(arangoDB, dbName, collectionName);
            createDocument(arangoDB, dbName, collectionName);
            executeQuery(arangoDB, dbName);
        }

        try {
            Stream<BaseDocument> result1 = new ArangoBuilder()
                .add("FOR t IN", collectionName).line()
                .add("FILTER t.name == ").param("Homer").line()
                .add("RETURN t")
                .query(arangoDB.db(dbName));

            Stream<BaseDocument> result2 = new ArangoBuilder()
                    .add("FOR t IN", collectionName).line()
                    .add("FILTER t.date < ").param(OffsetDateTime.now()).line()
                    .add("RETURN t")
                    .query(arangoDB.db(dbName));

        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }
    }

    protected void createDB(ArangoDB arangoDB, String dbName) {
        try {
            arangoDB.createDatabase(dbName);
            System.out.println("Database created: " + dbName);
        } catch (ArangoDBException e) {
            System.err.println("Failed to create database: " + dbName + "; " + e.getMessage());
        }
    }

    protected void createCollection(ArangoDB arangoDB, String dbName, String collectionName) {
        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(collectionName);
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + collectionName + "; " + e.getMessage());
        }
    }

    protected void createDocument(ArangoDB arangoDB, String dbName, String collectionName) {
        BaseDocument myObject = new BaseDocument();
        myObject.setKey("myKey");
        myObject.addAttribute("a", "Foo");
        myObject.addAttribute("b", 42);
        try {
            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
            System.out.println("Document created");
        } catch (ArangoDBException e) {
            System.err.println("Failed to create document. " + e.getMessage());
        }
    }

    protected void executeQuery(ArangoDB arangoDB, String dbName) {
        try {
            String query = "FOR t IN firstCollection FILTER t.name == @name RETURN t";
            Map<String, Object> bindVars = Collections.singletonMap("name", "Homer");
            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null, BaseDocument.class);
            cursor.forEachRemaining(aDocument -> {
                System.out.println("Key: " + aDocument.getKey());
            });
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }
    }

}
