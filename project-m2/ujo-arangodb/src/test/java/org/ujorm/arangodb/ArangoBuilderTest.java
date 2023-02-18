/*
 *  Copyright 2021-2022 Pavel Ponec
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
package org.ujorm.arangodb;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.mapping.ArangoJack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Test of ArangoDB  builder
 *
 * See: https://www.arangodb.com/tutorials/tutorial-java-driver/
 *
 * @author Pavel Ponec
 */
public class ArangoBuilderTest extends org.junit.jupiter.api.Assertions {
    private static final boolean DISABLED = true;

    @Disabled
    @Test
    public void testBulder() throws ArangoDBException {
        if (DISABLED) {
            return;
        }

        String dbName = "myTestDb";
        String collectionName = "firstCollection";

        // The default connection is to http://127.0.0.1:8529
        InputStream in = ArangoBuilderTest.class.getResourceAsStream("/arangodb.properties");
        ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(in)
                .serializer(new ArangoJack())
                .build();
        {
            createDB(arangoDB, dbName);
            createCollection(arangoDB, dbName, collectionName);
            createDocument(arangoDB, dbName, collectionName);
            executeQuery(arangoDB, dbName);
        }

        // A sample with an argument type of date-time:
        Stream<BaseDocument> result = new ArangoBuilder()
                .add("FOR t IN", collectionName).line()
                .add("FILTER t.date <=").param(OffsetDateTime.now()).line()
                .add("FILTER t.name ==").param("TEST").line()
                .add("RETURN t")
                .execute(arangoDB.db(dbName));
        BaseDocument[] arrayResult = result.toArray(BaseDocument[]::new);
        assertTrue(arrayResult.length >= 1);
    }

    @Test
    public void testAQL() {
        String collectionName = "firstCollection";

        // A sample with an argument type of date-time:
        ArangoBuilder result = new ArangoBuilder()
                .add("FOR t IN", collectionName).line()
                .add("FILTER t.date <=").param(OffsetDateTime.now()).line()
                .add("FILTER t.name ==").param("TEST").line()
                .add("RETURN t");

        String expected = "FOR t IN firstCollection\n"
                + " FILTER t.date <= @param1\n"
                + " FILTER t.name == @param2\n"
                + " RETURN t";
        assertEquals(expected, result.toString());
    }


    @Test
    public void testAQLwithNamedParams() {
        String collectionName = "firstCollection";

        // A sample with an argument type of date-time:
        ArangoBuilder result = new ArangoBuilder()
                .add("FOR t IN", collectionName).line()
                .add("FILTER t.date <=").param(OffsetDateTime.now(), "date").line()
                .add("FILTER t.name ==").param("TEST", "name").line()
                .add("RETURN t");

        String expected = "FOR t IN firstCollection\n"
                + " FILTER t.date <= @date\n"
                + " FILTER t.name == @name\n"
                + " RETURN t";
        assertEquals(expected, result.toString());
    }

    // --- UTILITIES ---

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
        myObject.addAttribute("name", "TEST");
        myObject.addAttribute("date", OffsetDateTime.now().minusDays(1).toEpochSecond());
        try {
            arangoDB.db(dbName).collection(collectionName).deleteDocument(myObject.getKey());
            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
            System.out.println("Document created");
        } catch (ArangoDBException e) {
            System.err.println("Failed to create document. " + e.getMessage());
        }
    }

    protected void executeQuery(ArangoDB arangoDB, String dbName) {
        try {
            String query = "FOR t IN firstCollection FILTER t.name == @name RETURN t";
            Map<String, Object> bindVars = Collections.singletonMap("name", "TEST");
            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null, BaseDocument.class);
            cursor.forEachRemaining(aDocument -> {
                System.out.println("Key: " + aDocument.getKey());
            });
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }
    }

}
