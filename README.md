# <img src="docs/images/ujorm3-logo.png" align="right" height="150" hspace="20"> Ujorm3 Library

*<span style="color: grey;">The original Ujorm v2 homepage has moved [here](docs/ujorm2).</span>*

> *"Do the simplest thing that could possibly work."*  
> — Kent Beck, creator of Extreme Programming and pioneer of Test-Driven Development.

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
* [Basic CRUD Operations](#basic-crud-operations)
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

Mapping an object and inserting it into the database takes just a few lines of code.
Ujorm3 seamlessly supports standard Jakarta annotations and modern Java Records.

```java
/** Quick start demonstration */
void quickStart() {
    var crud = CITY_EM.crud(connection()); // the method provides: java.sql.Connection
    var saved = crud.insert(new City(null, "Barcelona", "ES"));
    var barcelona = crud.findById(saved.id()).orElseThrow();
}
```
> 💡 **Sample Application:** For a practical demonstration of the library in action, check out
> the **[PetStore reference implementation](https://github.com/pponec/ujorm-petstore?tab=readme-ov-file#ujorm-petstore)**.

## Basic CRUD Operations

Basic mapping utilizes standard Jakarta annotations (`@Table`, `@Column`).
Entities do not need to be registered beforehand, and multiple classes can map to the same database table.

### SELECT

The library offers a type-safe `SelectQuery` builder for constructing SQL queries smoothly in Java, while still fully supporting the classic `SqlQuery` for writing raw native SQL.
Both approaches utilize the generated `Meta` classes for mapping and aliases, preventing SQL typos and ensuring compile-time safety.

**Automatic Joins:**
The `SelectQuery` automatically generates `JOIN` clauses based on the entity metadata. The type of join is determined by the `@JoinColumn` annotation:
* INNER JOIN: Used when the attribute is marked as mandatory (e.g., `@JoinColumn(nullable = false)`).
* LEFT JOIN: Used by default or when the attribute is explicitly marked as nullable (e.g., `@Nullable` or `@JoinColumn(nullable = true)`).

**Filtering and Native SQL:**
Data filtering can be defined using the `where()` method, which accepts a **`Criterion`** object. This object can represent a complex logical structure in the form of a **binary tree**, providing a clear and type-safe way to build nested conditions.

Alternatively, you can use the **`tail()`** method to append native SQL fragments. Within this method, you can use **`Key`** objects to represent database columns, ensuring that even native SQL remains synchronized with your domain model. For recursive queries or self-referencing relationships, the library supports **table aliases** within the property-descriptor (Key), allowing you to uniquely identify different instances of the same table.

```java
final EntityContext CTX = EntityContext.ofDefault();
final EntityManager<Employee, Long> EMPLOYEE_EM = CTX.entityManager(Employee.class);

List<Employee> select() {
    return SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
            .sql("SELECT")                                // Optional: "SELECT" is the default
            .columnsOfDomain(true)
            .column(MetaEmployee.city, MetaCity.name)     // INNER JOIN (nullable = false)
            .column(MetaEmployee.city, MetaCity.countryCode)  
            .column(MetaEmployee.boss, MetaEmployee.name) // LEFT JOIN (nullable = true)
            .where(MetaEmployee.id.whereGe(1L))
            .tail("ORDER BY", MetaEmployee.id)
            .toList()
    );
}
```

If you need full control over building the SQL SELECT statement, use the `SqlQuery` class.
This class provides an API with methods for type-safe insertion of database columns or just their labels.
The individual approaches differ only in the way the SQL query is constructed.

Regardless of the chosen approach, the database columns are ultimately mapped to entities using column aliases in the format: `"city.name"`.
The resulting `ResultSet` is also mapped to entities using this same mechanism via the **`ResultSetMapper`** class.

A more detailed overview of the available query options, including advanced use cases of `SqlQuery` and other implementations of `SelectQuery`, can be found in the [TutorialTest.java](project-m2/ujo-orm/src/test/java/org/ujorm/orm/tutorial/TutorialTest.java) class.

### INSERT

Ujorm3 handles auto-assigned primary keys automatically.
When inserting an immutable Record, the library returns a new instance with the generated ID.
For mutable JavaBeans, the ID is injected into the existing object.
Batch operations are explicitly supported for high-performance scenarios.

```java
static final EntityManager<City, Long> CITY_EM = EntityManager.of(City.class);
static final EntityManager<Employee, Long> EMPLOYEE_EM = EntityManager.of(Employee.class);

void insert() {
    var employeeCrud = EMPLOYEE_EM.crud(connection());
    var cityCrud = CITY_EM.crud(connection());
    
    var cityOttawa = cityCrud.insert(new City(null, "Ottawa", "CA"));
    
    var emplIngrid = Employee.of("Ingrid", cityOttawa, null);
    var emplDave = Employee.of("Dave", cityOttawa, emplIngrid);
    var emplCarol = Employee.of("Carol", cityOttawa, emplIngrid);

    employeeCrud.insert(emplIngrid);
    employeeCrud.insert(emplDave, emplCarol);
}
```

### UPDATE

The optional vararg parameter in the `update` method allows you to explicitly define which attributes should be updated.
You can use type-safe keys from the metamodel or an array of Strings.

```java
void update() {
    var employeeCrud = EMPLOYEE_EM.crud(connection());

    var emplIngrid = employeeCrud.findById(1L).orElseThrow();
    var emplDave = employeeCrud.findById(2L).orElseThrow();
    var emplCarol = employeeCrud.findById(3L).orElseThrow();

    emplIngrid.setBoss(emplDave);
    emplDave.setBoss(null);
    emplCarol.setBoss(emplDave);

    employeeCrud.update(Stream.of(emplIngrid, emplDave, emplCarol),
            MetaEmployee.boss);
}
```

If the original version of the domain object is provided, the library automatically detects changes and updates only the modified columns.

### DELETE

To maintain database integrity, entities must often be deleted in a specific order (e.g., subordinates before their bosses).
The `SelectQuery` class provides a type-safe way to fetch entities with the necessary ordering.
Using the `tail` method, you can append native SQL fragments like `ORDER BY` to ensure that self-referencing relationships are handled correctly during bulk deletion.

```java
void delete() {
    var employeeCrud = EMPLOYEE_EM.crud(connection());
    var qBossId = MetaEmployee.as("b").key(MetaEmployee.id);
    var criterion = MetaEmployee.id.whereGe(1L);

    try (var query = new SelectQuery<>(connection(), EMPLOYEE_EM)) {
        var employees = query.sql("SELECT")
                .column(MetaEmployee.id)
                .column(MetaEmployee.boss, qBossId) // Build the relation
                .where(criterion)
                .tail("ORDER BY", qBossId, "DESC NULLS LAST") // Bosses last
                .streamMap(EMPLOYEE_MAPPER.mapper())
                .toList();

        employeeCrud.delete(employees.stream());
    }
}
```

### All Examples

These snippets are extracted from a sequential JUnit test suite demonstrating the full entity lifecycle.
You can run and modify this test locally: [TutorialTest.java](project-m2/ujo-orm/src/test/java/org/ujorm/orm/tutorial/TutorialTest.java).

## Class Diagram

<p align="center">
  <img src="docs/images/OrmApi.svg" width="700" height="400" alt="OrmApi Class Diagram">
</p>

* **SelectQuery:**
  A type-safe builder for constructing SELECT statements directly from the domain model using a fluent API.
  It automatically generates `FROM` and `JOIN` clauses based on the paths of used metamodel attributes.
  It integrates hierarchical `Criterion` trees for complex data filtering without manual SQL writing.
* **ResultSetMapper:**
  A universal tool for transforming `ResultSet` rows into Record or JavaBean objects.
  It utilizes the Stream API for memory-efficient and lazy processing of query results.
  It provides automatic type conversion between JDBC types and target class attributes.
* **EntityManager:**
  The central component responsible for managing metadata and configuring the ORM mapping.
  It serves as a factory for creating `Crud` objects used for standard database operations.
  It manages SQL logging configurations and defines rules for table and column quoting.

Ujorm3 derives mapping from JPA/Jakarta annotations (`@Table`, `@Column`, `@Id`).
If missing, it attempts to derive them automatically.
M:1 relationships are recognized if an attribute's class has a `@Table` annotation.

Which class should you use?
Use `SelectQuery` for all standard data reading to gain full type safety and automatic table joins.
Use `EntityManager` for operations on domain objects by primary key.
For specific cases requiring native SQL, the low-level `SqlQuery` interface is available (not shown in the diagram).

### Caching Strategy

There is **no data caching** for user queries.
Metadata is cached to maximize speed:
* **ResultSetMapper:** Caches column mapping structures (limit: 512 distinct queries).
* **EntityManager:** Retains the database table metamodel for each entity.
  Use `EntityManagerService` for shared singleton instances.

## Generated Meta Models

The Ujorm3 Annotation Processor generates `Meta` classes at compile time for type safety without string literals.

```java
/** Auto-generated metamodel for Employee */
public class MetaEmployee {
    private static final DomainHandler<Employee> meta = DomainHandlerProvider.getHandler(Employee.class);

    public static final Key<Employee, Long> id = meta.getKey("id");
    public static final Key<Employee, String> name = meta.getKey("name");
    public static final Key<Employee, City> city = meta.getKey("city");
    public static final Key<Employee, Employee> boss = meta.getKey("boss");
}
```

Metamodel keys are singletons.
Chaining them allows for strictly type-safe paths (e.g., `MetaEmployee.city, MetaCity.name`).
They implement `CharSequence`, meaning they can be used interchangeably with Strings in many parts of the API.

## Configuration

Configuration priority (from highest to lowest):
1. Manual assignment to `ConfigImpl`
2. Java system properties
3. `ujorm-config.properties` file
4. Internal defaults

See the [source code](project-m2/ujo-orm/src/main/java/org/ujorm/orm/Config.java) for all parameters.

## Maven Dependencies & Setup

Ujorm3 requires **Java 17 or higher**.

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

To enable the Meta Processor, configure the `maven-compiler-plugin`.
The library includes automated integration tests for PostgreSQL, MySQL, MariaDB, Oracle, and MS SQL Server via Testcontainers.

---

## Benchmarks

Performance tests comparing Ujorm3 to Hibernate, Jdbi, Exposed, and MyBatis were executed using an H2 database on Java 25.
To ensure an objective methodology, scenarios and implementations were designed by the **Gemini Pro AI** model.
The complete source code for these benchmarks is entirely open-source and fully auditable on GitHub, ensuring maximum transparency.

**Conclusions:**
* **Execution Speed:** Ujorm3 consistently ranks at the top across all tested database operations.
* **Memory Efficiency:** The library exhibits the lowest memory allocation rate (Bytes/op), reducing Garbage Collector pressure.
* **Minimal Footprint:** Zero external dependencies and a total compiled size under 3 MB makes it ideal for microservices and embedded devices.

**Version tested:** `3.0.0-RC4`  
**Full metrics:** 👉 [GitHub: orm-benchmarks](https://github.com/pponec/orm-benchmarks?tab=readme-ov-file#orm-benchmark)

---

## FAQ

**Will Ujorm v2 still be supported?**
No, support for v2 has ended.

**Do domain objects need to implement `Serializable`?**
No, Ujorm3 works with stateless data structures.

**Is `@JoinColumn` required?**
No, it is optional.
Relations are recognized by the `@Table` annotation on the attribute type.

**Are core components thread-safe?**
Yes, `EntityManager` and `Meta` classes are stateless and thread-safe.
`Crud` and `SqlQuery` are stateful and scoped to a single thread/request.

**Is runtime bytecode generation secure?**
Yes.
It is based purely on your project's domain classes with no external data input, a standard approach also used by HikariCP or Spring.

---

## Feedback & Contributions

Join the conversation or report issues on GitHub:  
👉 **[Join the Discussion on GitHub](https://github.com/pponec/ujorm)**

---

## Related Links

* [Ujorm Main Project](https://github.com/pponec/ujorm)
* [Petstore Demo](https://github.com/pponec/ujorm-petstore)
* [ORM Benchmarks](https://github.com/pponec/orm-benchmarks)
* [HTML Builder Benchmarks](https://github.com/pponec/html-benchmarks)