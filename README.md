# <img src="docs/images/ujorm3-logo.png" align="right" height="150" hspace="20"> Ujorm3 Library

*<span style="color: grey;">The original Ujorm2 homepage has moved [here](https://ujorm.org/www/web/).</span>*

> *"Do the simplest thing that could possibly work."*
— Kent Beck, creator of Extreme Programming and pioneer of Test-Driven Development.

Ujorm3 is a lightweight Object-Relational Mapping (ORM) library designed for efficient relational database development with minimalist code and a straightforward API.
The library maps database rows to standard Java objects using clean SQL without unnecessary abstraction.
It supports mapping to both mutable JavaBeans and immutable Records, including M:1 relations.

To achieve data manipulation speeds comparable to hand-written JDBC code, Ujorm3 compiles its own bytecode at runtime.
At its core, the library is built around the **Typed Key Pattern**.
These keys act as typed descriptors, providing compile-time safety without casting and enabling fast bulk operations.
Consequently, the overhead of Java reflection is strictly limited to the initial loading of object metadata.

### Design Philosophy

To maintain a high utility-to-code ratio and minimize bugs, Ujorm3 intentionally limits its scope:
* **No Lazy-Loading:** Relationships are not lazily fetched to prevent hidden performance costs and the N+1 query problem.
* **M:1 Relations Only:** Collection attributes (1:M) are not supported.
  Query from the "many" side or use a secondary SQL query instead.
* **No Magic / No Stateful Lifecycle:** The library does not manage database transactions or entity caching.
  Entities are treated as stateless data carriers.
* **No SQL Dialects:** Advanced queries are written in native SQL.
  This unlocks the full performance and feature set of your specific database engine.

<div align="center">
<img src="docs/images/benchmark-graph.svg" alt="Benchmark graph" width="500" />
</div>

Ujorm3 outperforms popular ORM competitors across virtually all performance metrics while maintaining a minimal memory footprint.
Detailed results and methodology are available in the [Benchmarks](#benchmarks) section.

---

## Menu
* [Quick Start (TL;DR)](#quick-start-tldr)
* [Detailed Operations & Relations](#detailed-operations--relations)
    * [SELECT](#select)
    * [INSERT](#insert)
    * [UPDATE](#update)
    * [DELETE](#delete)
    * [All Examples](#all-examples)
* [Class Diagram](#class-diagram)
* [Generated Meta Models](#generated-meta-models)
* [Configuration](#configuration)
* [Maven Dependencies & Setup](#maven-dependencies--setup)
* [Benchmarks](#benchmarks)
* [FAQ](#faq)
* [Feedback & Contributions](#feedback--contributions)
* [Related Links](#related-links)

---

## Quick Start (TL;DR)

Ujorm3 provides a specialized toolset for different levels of complexity. Choose the one that best fits your immediate task:

### 1. EntityManager (Simple CRUD by ID)
The fastest way to handle single-table operations by primary key. It requires no SQL writing and supports standard Jakarta annotations.

```java
/** Simple CRUD operations for Entities */
void simpleCrud(java.sql.Connection connection) {
    var crud = CITY_EM.crud(connection);
    var saved = crud.insert(new City(null, "Barcelona", "ES"));
    var barcelona = crud.findById(saved.id()).orElseThrow();
}
```

### 2. SelectQuery (Type-Safe Object Querying)
The primary tool for searching and fetching relations. It uses **`Criterion`** objects for type-safe filtering (verifying both structure and parameter types) and handles `JOIN` clauses automatically.

```java
/** Type-safe selection */
List<Employee> findEmployees(Connection connection) {
    return SelectQuery.run(connection, EMPLOYEE_EM, query -> query
            .columns(true)     // Select all domain columns including foreign keys
            .column(MetaEmployee.city, MetaCity.name)
            .where(MetaEmployee.id.whereGe(1L))
            .toList());
}
```

### 3. SqlQuery (Universal Native SQL)
A universal approach for complex requirements. It allows writing raw SQL while maintaining safety via `bind()` and `label()`. Using the **generic mapper**, Ujorm3 can automatically populate even nested relations by matching SQL aliases to Key paths.

```java
/** Universal native SQL with relations */
List<Employee> findEmployees(Connection connection) {
    return SqlQuery.run(connection, query -> query
            .sql("""
                    SELECT e.id  AS ${e.id}
                    , e.name     AS ${e.name}
                    , c.name     AS ${c.name}
                    FROM employee e
                    JOIN city c ON c.id = e.city_id
                    WHERE e.id >= :id
                    """)
            .label("e.id"  , MetaEmployee.id)
            .label("e.name", MetaEmployee.name)
            .label("c.name", MetaEmployee.city, MetaCity.name) // Key Path mapping
            .bind("id", 1L)
            .streamMap(EMPLOYEE_MAPPER.mapper()) // Generic mapping including relations
            .toList());
}
```

---

## Detailed Operations & Relations

While the Quick Start covers basics, real-world development involves complex relationships and batch processing.

### SELECT: Joins and Advanced Filtering

Building on the `SelectQuery` builder, Ujorm3 excels at querying hierarchical data by automatically generating `JOIN` clauses. The join type depends on the `@JoinColumn` annotation:
* **INNER JOIN:** Used for mandatory attributes (`nullable = false`).
* **LEFT JOIN:** Used for nullable attributes (default).

For complex logic, use the binary-tree based **`Criterion`** for type-safe filtering. If you need to append specific SQL fragments (like `ORDER BY` or `GROUP BY`), use the **`tail()`** method.

```java
final EntityContext CTX = EntityContext.ofDefault();
final EntityManager<Employee, Long> EMPLOYEE_EM = CTX.entityManager(Employee.class);

/** Fetching an entity with its relations */
List<Employee> select(Connection connection) {
    return SelectQuery.run(connection, EMPLOYEE_EM, query -> query
            .columns(true)
            .column(MetaEmployee.city, MetaCity.name)     // INNER JOIN
            .column(MetaEmployee.boss, MetaEmployee.name) // LEFT JOIN
            .where(MetaEmployee.id.whereGe(1L))
            .tail("ORDER BY", MetaEmployee.id)
            .toList()
    );
}
```

### INSERT: Generation and Batching

Ujorm3 handles auto-assigned primary keys out of the box. Batch operations are explicitly supported for high-performance scenarios.

```java
void insert(Connection connection) {
    var employeeCrud = EMPLOYEE_EM.crud(connection);
    var cityCrud = CITY_EM.crud(connection);

    var cityOttawa = cityCrud.insert(new City(null, "Ottawa", "CA"));
    var emplDave = Employee.of("Dave", cityOttawa, null);
    var emplCarol = Employee.of("Carol", cityOttawa, null);

    employeeCrud.insert(emplDave, emplCarol); // Batch insert
}
```

### UPDATE: Partial Changes

You can explicitly define which attributes should be updated using type-safe keys. If the original domain object is provided, Ujorm3 detects changes and updates only modified columns automatically.

```java
void update(Connection connection) {
    var employeeCrud = EMPLOYEE_EM.crud(connection);
    var emplIngrid = employeeCrud.findById(1L).orElseThrow();

    emplIngrid.setBoss(newBoss);
    employeeCrud.update(Stream.of(emplIngrid), MetaEmployee.boss);
}
```

### DELETE: Integrity and Ordering

When deleting self-referencing relationships, use the `tail` method to ensure correct ordering (e.g., deleting subordinates before bosses).

```java
void delete(Connection connection) {
    var employeeCrud = EMPLOYEE_EM.crud(connection);
    var qBossId = MetaEmployee.as("b").key(MetaEmployee.id);

    try (var query = new SelectQuery<>(connection, EMPLOYEE_EM)) {
        var employees = query.column(MetaEmployee.id)
                .column(MetaEmployee.boss, qBossId)
                .tail("ORDER BY", qBossId, "DESC NULLS LAST")
                .streamMap(EMPLOYEE_MAPPER.mapper())
                .toList();

        employeeCrud.delete(employees.stream());
    }
}
```

### All Examples
Extract from: [TutorialTest.java](project-m2/ujo-orm/src/test/java/org/ujorm/orm/tutorial/TutorialTest.java).

---

## Class Diagram

<p align="center">
  <img src="docs/images/OrmApi.svg" width="700" height="400" alt="OrmApi Class Diagram">
</p>

* **SelectQuery:** Type-safe builder for SELECT statements.
* **ResultSetMapper:** Universal tool for transforming `ResultSet` into Records/JavaBeans.
* **EntityManager:** Core component managing metadata and creating `Crud` objects.

### Caching Strategy
Metadata is cached for speed (ResultSetMapper limit: 512 queries). Domain data is **never cached** to ensure consistency.

---

## Generated Meta Models

The Ujorm3 Annotation Processor generates `Meta` classes for compile-time safety.

```java
/** Auto-generated metamodel for Employee */
public class MetaEmployee {
    private static final DomainHandler<Employee> meta = DomainHandlerProvider.getHandler(Employee.class);

    public static final Key<Employee, Long> id = meta.getKey("id");
    public static final Key<Employee, String> name = meta.getKey("name");
    public static final Key<Employee, City> city = meta.getKey("city");
}
```
Metamodel keys are singletons, enabling lightning-fast reference comparison and precompiled data access.

---

## Configuration

Ujorm3 is configured using the `Config` class, where each parameter is represented by a type-safe `Key`. 
To ensure consistency and thread safety in a multi-threaded environment, it is highly recommended to make the configuration immutable by calling the `lock()` method before passing it to the ORM engine.
Configuration values are assembled dynamically from multiple sources. 
When a parameter is requested, the mechanism evaluates these sources following a strict priority (from highest to lowest):

1. **Manual Settings:** Values explicitly assigned using the `setValue(Key, Object)` method on the `Config` instance.
2. **System Properties:** JVM system properties prefixed with `org.ujorm.` (e.g., `-Dorg.ujorm.batchSize=1000`).
3. **Configuration File:** Values loaded from the `ujorm-config.properties` file located on the classpath.
4. **Internal Defaults:** The initial fallback values defined directly in the library's source code.

See the [JavaDoc](https://www.javadoc.io/doc/org.ujorm/ujo-orm/latest/org/ujorm/orm/Config.html) for all available parameters.

---

## Maven Dependencies & Setup

Requires **Java 17+**.

```xml
<dependencies>
    <dependency>
        <groupId>org.ujorm</groupId>
        <artifactId>ujo-core</artifactId>
        <version>3.0.0-RC4</version>
    </dependency>
    <dependency>
        <groupId>org.ujorm</groupId>
        <artifactId>ujorm-orm</artifactId>
        <version>3.0.0-RC4</version>
    </dependency>
</dependencies>
```

---

## Benchmarks

Ujorm3 consistently ranks at the top for execution speed and memory efficiency (lowest Bytes/op) compared to Hibernate, Jdbi, or MyBatis.
👉 [Full metrics on GitHub](https://github.com/pponec/orm-benchmarks)

---

## FAQ

* **Serializability:** Domain objects do not need to implement `Serializable`.
* **Bytecode Safety:** Generation is based purely on your domain classes, occurring entirely in RAM.
* **Thread Safety:** `EntityManager` and `Meta` classes are thread-safe. `Crud` and `SqlQuery` are request-scoped.

---

## Feedback & Contributions
👉 **[Join the Discussion on GitHub](https://github.com/pponec/ujorm)**

---

## Related Links
* [Ujorm Main Project](https://github.com/pponec/ujorm)
* [Petstore Demo](https://github.com/pponec/ujorm-petstore)
* [HTML Builder Benchmarks](https://github.com/pponec/html-benchmarks)