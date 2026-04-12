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
<img src="docs/images/benchmark-graph.svg" alt="Benchmark graph" width="400" />
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
void quickStart() {
    var crud = CITY_EM.crud(connection());
    var saved = crud.insert(new City(null, "Barcelona", "ES"));
    var barcelona = crud.findById(saved.id()).orElseThrow();
}
```
> 💡 **Sample Application:** For a practical demonstration of the library in action, check out
> the **[PetStore reference implementation](https://github.com/pponec/ujorm-petstore?tab=readme-ov-file#ujorm-petstore)**.

## Basic CRUD Operations

Basic mapping utilizes standard Jakarta annotations (`@Table`, `@Column`).
Advanced SELECT queries with JOINs use a dot-notation alias format (e.g., `city.name`) directly in the native SQL.
Entities do not need to be registered beforehand, and multiple classes can map to the same database table.

### SELECT

The library allows you to write native SQL queries while maintaining type safety.
By using the generated `Meta` classes for aliases (`${...}`), you prevent SQL typos and ensure safe mapping.
The conversion from the `ResultSet` to the domain object is entirely explicit, which inherently eliminates the N+1 query problem.

```java
static final ResultSetMapper<Employee> EMPLOYEE_MAPPER = ResultSetMapper.of(Employee.class);

void select() {
    var sql = """
            SELECT ${COLUMNS}
            FROM employee e
            JOIN city c ON c.id = e.city_id
            LEFT JOIN employee b ON b.id = e.boss_id
            WHERE e.id > :employeeId
            """;

    var employees = SqlQuery.run(connection(), query -> query
            .sql(sql)
            .column("e.id", MetaEmployee.id)
            .column("e.name", MetaEmployee.name)
            .column("c.name", MetaEmployee.city, MetaCity.name)
            .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
            .column("b.name", MetaEmployee.boss, MetaEmployee.name)
            .bind("employeeId", 0L)
            .streamMap(EMPLOYEE_MAPPER.mapper())
            .toList());
}
```

In addition to the `column()` method, the API provides a `label()` method.
While `column()` works with the runtime-replaced `${COLUMNS}` placeholder, `label()` requires explicit placeholders (e.g., `SELECT e.id AS ${e.id}`).
These are resolved and properly quoted at runtime, keeping the query structure transparent and easy to test in database clients.
Note that these two approaches cannot be combined within a single query.

Database columns can also be mapped without metamodel keys by using dot-notation for property names (e.g., `"city.name"`).
Note that these expressions must be enclosed in the quotes required by your database vendor.

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

The `selectWhere` method enables retrieving entities without listing individual columns in the SELECT statement.
Within this block, the `SqlQuery` instance allows you to map the result set using the `streamMap` method.

```java
void delete() {
    var employeeCrud = EMPLOYEE_EM.crud(connection());

    var allEmployees = employeeCrud
            .selectWhere("id > :id", builder -> builder
                    .bind("id", 0L)
                    .streamMap(EMPLOYEE_EM::map)
                    .sorted(Comparator.comparing(e -> e.getBoss() == null))
                    .toList());

    employeeCrud.delete(allEmployees.stream());
}
```

### All Examples

These snippets are extracted from a sequential JUnit test suite demonstrating the full entity lifecycle.
You can run and modify this test locally: [TutorialTest.java](project-m2/ujo-orm/src/test/java/org/ujorm/orm/tutorial/TutorialTest.java).

## Class Diagram

<p align="center">
  <img src="docs/images/OrmApi.svg" width="700" height="400" alt="OrmApi Class Diagram">
</p>

* **SqlQuery:** A facade over `PreparedStatement` with no external dependencies.
* **ResultSetMapper:** Converts `Stream<ResultSet>` to objects (JavaBeans or Records).
* **EntityManager:** The core component that handles mapping and creates `Crud` objects for standard operations.

Ujorm3 derives mapping from JPA/Jakarta annotations (`@Table`, `@Column`, `@Id`).
If missing, it attempts to derive them automatically.
M:1 relationships are recognized if an attribute's class has a `@Table` annotation.

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