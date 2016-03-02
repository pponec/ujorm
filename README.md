# Ujorm Framework
The Ujorm is an open source Java small library which provides non-traditional objects based on the key-value architecture to open up new exciting opportunities for writing efficient code. This library offers a unique ORM module designed for rapid Java development with great performance and a small footprint. The key features are type safe database queries, relation mapping by Java code, no entity states and a memory overloading protection cache.

## Why a new ORM mapping?
The Ujorm (original name was UJO Framework) is designed primarily for the rapid Java development based on a relation database.
*    java compiler can discover a syntax error of Ujorm database query similar like 4GL database languages
*    easy to configure the ORM model by java source code, optionally by annotations and a XML file
*    great performance, some types of SELECT query are very fast in comparison to its competitors
*    lazy loading or the one request data loading of relations are supported optionally as a fetch strategy
*    database tables, columns and indexes can be optionally updated according to Java meta-model in the run-time
*    no confusing proxy or binary modified business objects
*    very lightweight framework with no library dependencies in the run-time

## Some other features

*    batch SQL statements for more rows like INSERT, UPDATE and DELETE are supported
*    features LIMIT and OFFSET are available from the API
*    nested transactions are supported using the partially implemented JTA
*    resources for ORM mapping can be a database table, view or native SQL SELECT
*    subset of table columns on SELECT can be specified for the SQL statement
*    JDBC query parameters are passed by a 'question mark' notation to the PreparedStatement for a high security
*    stored database procedures and functions are supported
*    all persistent objects are based on the interface OrmUjo, namely on the implementation OrmTable
*    internal object cache is based on the WeakHashMap class so that large transactions does not cause any OutOfMemoryException
*    database indexes are created by the meta-model, added support for unique, non-unique indexes including the composed one

## Home Page
http://ujorm.org/

## Maven Repository

 The ORM module:

    <dependency>
        <groupId>org.ujorm</groupId>
        <artifactId>ujo-orm</artifactId>
        <version>${ujorm.version}</version>
    </dependency>

 Module for [Apache Wicket](http://wicket.apache.org/) integration:

    <dependency>
        <groupId>org.ujorm</groupId>
        <artifactId>ujo-wicket</artifactId>
        <version>${ujorm.version}</version>
    </dependency>

## How to open the Ujorm project

The Java 7.0 is required to compile of the project souces, the Java 6+ is required to runtime.
Use a one of the preferred tools to open the project:

  *  NetBeans IDE,
  *  InteliJ IDEA,
  *  Eclipse with some Maven pluggin, or use the
  *  Maven toolkit if you like a console command line





