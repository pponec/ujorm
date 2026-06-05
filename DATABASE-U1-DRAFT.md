# U1 — Embedded Java-Native Persistent Store

> **WORKING DRAFT — FOR DISCUSSION ONLY**
> This document is a preliminary design proposal subject to change.
> Nothing described here is implemented or committed to the Ujorm roadmap.

---

## 1. Motivation

Ujorm already provides a clean, type-safe API for querying relational databases via
`SelectQuery` and `EntityManager`. The query plan is expressed entirely as Java objects
(no SQL string building on the user side). This raises a natural question:

> Why does the execution layer have to be SQL at all?

If the query is already a Java object tree, it can be *interpreted directly* against a
key-value store — without a SQL parser, without a JDBC driver, and without a network
layer. U1 is that idea taken to its logical conclusion.

---

## 2. Goals and Philosophy

### Primary goals

- **Zero new API to learn.** The user writes the same `SelectQuery` and `EntityManager`
  calls they already know. Only the backing store changes.
- **Zero SQL.** No SQL strings, no DDL scripts, no migration files for common schema
  changes. The Java `@Entity` class *is* the schema.
- **Embedded-first.** No server mode, no TCP listener, and no remote connections
  in v1. Network access is not excluded from the long-term roadmap but is out of
  scope for the initial release.
- **Minimal footprint.** Target: single JAR under 300 KB of own code, one optional
  dependency (H2 MVStore ~200 KB).
- **M:1 relations as first-class citizens.** Unlike typical NoSQL stores, U1 supports
  references between persistent classes via `@JoinColumn`. A related entity is stored
  as a foreign key and resolved in a single query pass — no application-side join loop.
- **Parallel writes with full isolation.** Multiple threads (including virtual threads)
  may write concurrently via independent `U1Session` instances. MVCC guarantees that
  readers never block writers and writers never block readers.
- **First-class test support.** `U1.openInMemory()` provides a zero-config in-memory
  store that is fast to create and requires no cleanup — designed for JUnit usage.

### Philosophy

| Principle | Consequence |
|---|---|
| Schema = Java class | No DDL; structure inferred from `@Entity` annotations |
| Query = Java object | No parser; `Criterion` tree evaluated directly over rows |
| Safety over magic | Schema conflicts produce clear exceptions, not silent data loss |
| Standard annotations | Re-use JPA/Jakarta: `@Entity`, `@Id`, `@Column`, `@JoinColumn`, `@Index` |
| Ujorm Key pattern | Type-safe field references; no stringly-typed queries |
| Modern Java | Records, sealed classes, virtual threads, and Stream API used throughout |

### Non-goals for v1

- Network / server mode (deferred — not excluded from future versions)
- Stored procedures or triggers
- Binary data types (BLOB, CLOB)
- Distributed transactions or replication
- SQL compatibility or JDBC driver
- Schema-less / unstructured document storage

---

## 3. Planned Technical Properties

| Property | Detail |
|---|---|
| **Storage engine** | H2 MVStore (`com.h2database:h2-mvstore`, ~200 KB, zero deps) |
| **Persistence format** | One `.u1` file per database; crash-safe (COW + two file headers) |
| **Concurrency** | MVCC via MVStore `TransactionStore`; multiple concurrent readers and writers |
| **Parallel writes** | Each `U1Session` holds an independent MVStore transaction; writers do not block each other under MVCC |
| **Transactions** | `COMMIT` / `ROLLBACK` / `SAVEPOINT` at `U1Session` level; auto-rollback on close without commit |
| **Isolation level** | READ COMMITTED (MVStore default) |
| **Virtual threads** | `U1Session` is designed to be held on a virtual thread; no thread-local state; lock contention is minimal |
| **Indexes** | B-tree via secondary `MVMap`; rebuilt automatically on schema change |
| **In-memory mode** | `U1.openInMemory()` — no file, data lost on close; ideal for JUnit |
| **Serialisation** | Field-by-field binary encoding driven by Ujorm `Key` metadata; no reflection at runtime |
| **Data types** | `INTEGER`, `BIGINT`, `DECIMAL`, `DOUBLE`, `VARCHAR`, `BOOLEAN`, `DATE`, `TIME`, `TIMESTAMP` |
| **Relations** | M:1 only (`@JoinColumn`); stored as `Long` FK, resolved during query execution |
| **Stream API** | Query results exposed as lazy `Stream<T>`; compatible with Java Stream pipelines |
| **License** | To be decided (Apache 2.0 candidate) |
| **Java version** | Java 21 minimum (virtual threads, records, sealed classes, pattern matching); Java 25 LTS preferred — see §3.1 |

### 3.1 Java version strategy

U1 requires **Java 21 as the minimum** — virtual threads (JEP 444), sealed classes,
pattern-matching switch, and `SequencedCollection` are all stable in Java 21 and
are core to the design. Java 17 (current Ujorm minimum) is explicitly not supported
because virtual-thread-safe session handling would require significant compromises.

**Java 25 LTS (September 2025) is the preferred target** for new deployments.
It adds three features directly relevant to U1:

| Java 25 feature | Benefit for U1 |
|---|---|
| `ScopedValue` (JEP 487, final in Java 23) | Replaces `ThreadLocal` for propagating the active `U1Session` transaction context down virtual-thread call stacks — cleaner and GC-friendly |
| Structured concurrency (JEP 505) | Clean coordination of parallel index lookups or multi-entity operations within a single `SelectQuery` execution |
| Stream gatherers (JEP 485, final in Java 23) | Custom intermediate `Stream` operations in the query result pipeline without intermediate collections |

**Practical decision:** the module is compiled with `--release 21` to maximise
compatibility; Java 25-specific APIs (`ScopedValue`, `StructuredTaskScope`) are used
in the implementation where available and isolated behind a version-guarded abstraction
layer. Applications running on Java 21 get correct behaviour; applications on Java 25
get the optimised path.

---

## 4. Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│  User code                                          │
│  EntityManager / SelectQuery (existing Ujorm API)   │
└────────────────────┬────────────────────────────────┘
                     │  QueryBackend SPI (new)
          ┌──────────┴──────────┐
          │                     │
   JdbcBackend            MvStoreBackend        ← U1
   (existing)             (new module)
          │                     │
    JDBC / SQL            MvStoreEngine
                               │
                ┌──────────────┼──────────────┐
                │              │              │
         RowSerializer   IndexManager   TransactionCoordinator
         (Key metadata)  (MVMaps)       (MVStore TransactionStore)
                │
          MVStore file  /  in-memory MVStore
          (*.u1)
```

### Key internal components

**`MvStoreBackend`** — implements the `QueryBackend` SPI; receives a compiled
`SelectQuery` or `EntityManager` call and routes it to `MvStoreEngine`.

**`MvStoreEngine`** — owns the `MVStore` instance; manages one `MVMap<Long, byte[]>`
per `@Entity` class; dispatches reads and writes through the active session transaction.

**`RowSerializer`** — converts between a Java entity and `byte[]` using Ujorm `Key`
metadata (field name from `@Column(name=...)`, type from `Key<E,V>`). Uses sealed
interfaces and pattern-matching switch for type dispatch. No reflection at query time.

**`IndexManager`** — maintains secondary `MVMap<Object, long[]>` per indexed field.
Index maps are keyed on field value; values are arrays of primary keys.

**`CriterionEvaluator`** — walks a `Criterion` tree and evaluates it against a
deserialised row. Uses pattern-matching switch over the sealed `Criterion` hierarchy.
Re-uses Ujorm's existing `Criterion` types.

**`RelationResolver`** — when a `SelectQuery` column references a related entity
(e.g. `MetaEmployee.city, MetaCity.name`), resolves the FK to the target `MVMap`
in the same query pass. No second query issued to user code.

**`TransactionCoordinator`** — wraps MVStore's `TransactionStore`; assigns one
`Transaction` object per `U1Session`; coordinates commit, rollback, and savepoints
across all entity maps touched within the session.

**`SchemaManager`** — on `db.repository(X.class)`, compares the live `@Entity`
class against stored metadata. Applies the configured `SchemaMode`.

---

## 5. Modern Java Features in U1

U1 targets Java 17 as a minimum but is designed to take full advantage of Java 21+
(LTS) and Java 25 (LTS) features throughout its implementation.

### Virtual threads (Java 21+)

Each `U1Session` is safe to hold on a virtual thread. MVStore's write lock is
held only for the duration of a single page flush — typically microseconds. Virtual
threads parked on that lock do not block carrier threads, making high-concurrency
embedded usage (e.g. a web application with hundreds of concurrent sessions) practical
without a thread pool.

```java
// Each request handler runs on its own virtual thread — no pooling needed
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        try (U1Session session = db.openSession()) {
            session.entityManager(Order.class).insert(order);
            session.commit();
        }
    });
}
```

### Records — immutable result and metadata types

Internal value types use records to eliminate boilerplate and enforce immutability:

```java
record SchemaDiff(String fieldName, DiffKind kind, Class<?> storedType, Class<?> liveType) {}
record IndexDescriptor(String mapName, Key<?,?> key, IndexType type) {}
record RowPage(long id, byte[] data) {}
```

### Sealed interfaces + pattern matching — type dispatch

The `Criterion` evaluator and row serialiser use sealed hierarchies for exhaustive,
compiler-checked dispatch instead of `instanceof` chains:

```java
// Exhaustive dispatch — compiler enforces all Criterion subtypes are handled
private boolean evaluate(Criterion<?,?> c, Object row) {
    return switch (c) {
        case ValueCriterion<?,?> vc  -> evaluateValue(vc, row);
        case BinaryCriterion<?>  bc  -> evaluateBinary(bc, row);
        case NotCriterion<?>     nc  -> !evaluate(nc.inner(), row);
    };
}
```

### Stream API — lazy query results

`SelectQuery` results are exposed as a lazy `Stream<T>`, enabling composition with
standard Java stream pipelines without materialising the full result set:

```java
// Stream pipeline — rows deserialised lazily, filter applied before materialisation
long seniorCount = SelectQuery.run(db, Employee.class, q -> q
    .where(MetaEmployee.city.innerJoin(MetaCity.name.whereEq("Prague")))
    .toStream()                              // Stream<Employee>
).filter(e -> e.getAge() > 50)
 .count();
```

### SequencedCollection — ordered results

`toList()` returns a `SequencedCollection<T>` (Java 21), giving callers access to
`getFirst()`, `getLast()`, and `reversed()` without additional wrapping.

---

## 6. Concurrency and Transactions in Detail

### Parallel write model

MVStore's `TransactionStore` implements MVCC at the storage level. U1 maps one
`U1Session` to one MVStore `Transaction`. The result:

```
Thread A (Session 1): read Employee#1  →  sees committed version V3
Thread B (Session 2): write Employee#1 →  creates pending version V4
Thread A (Session 1): read Employee#1  →  still sees V3 (READ COMMITTED)
Thread B (Session 2): commit           →  V4 becomes visible
Thread A (Session 3): read Employee#1  →  now sees V4
```

Multiple sessions may write *different* rows concurrently without contention.
Writing the *same* row from two sessions concurrently results in a
`U1WriteConflictException` on the second committer — the application should retry.

### Session lifecycle

```java
// Recommended: try-with-resources guarantees rollback on exception
try (U1Session session = db.openSession()) {
    var empEm  = session.entityManager(Employee.class);
    var cityEm = session.entityManager(City.class);

    City prague = cityEm.findById(1L).orElseThrow();
    empEm.insert(new Employee(0L, "Alice", "alice@example.com", prague));
    empEm.insert(new Employee(0L, "Bob",   "bob@example.com",   prague));

    session.savepoint("after-inserts");

    empEm.deleteById(99L);              // something goes wrong
    session.rollbackToSavepoint("after-inserts");

    session.commit();
}
```

### Auto-commit mode

For simple single-operation use cases:

```java
// Auto-commit: each EntityManager call is its own transaction
EntityManager<Employee> em = db.autoCommitEntityManager(Employee.class);
em.insert(employee);   // committed immediately
```

---

## 7. In-Memory Mode and JUnit Integration

`U1.openInMemory()` uses MVStore's in-memory store (no file path). The store is
fully functional — transactions, indexes, and schema management all work identically
to the file-based mode. Data is lost when `close()` is called.

### JUnit 5 pattern

```java
class EmployeeRepositoryTest {

    U1Database db;
    EntityManager<Employee> em;

    @BeforeEach
    void setUp() {
        db = U1.builder()
               .inMemory()
               .schemaMode(SchemaMode.CREATE)   // fresh schema every test
               .build();
        em = db.autoCommitEntityManager(Employee.class);

        // Seed data
        City prague = new City(1L, "Prague");
        db.autoCommitEntityManager(City.class).insert(prague);
        em.insert(new Employee(1L, "Alice", "alice@example.com", prague));
    }

    @AfterEach
    void tearDown() {
        db.close();   // drops all data; nothing to clean up on disk
    }

    @Test
    void findByCity() {
        List<Employee> result = SelectQuery.run(db, Employee.class, q -> q
            .where(MetaCity.name.whereEq("Prague"))
            .toList()
        );
        assertThat(result).hasSize(1);
    }
}
```

---

## 8. API Design

### 8.1 Opening a database

```java
// File-based (persistent)
U1Database db = U1.open("myapp.u1");

// File-based with explicit configuration
U1Database db = U1.builder()
    .filePath("myapp.u1")
    .schemaMode(SchemaMode.AUTO_UPDATE)   // default
    .compress(true)
    .encryptionKey(password)
    .build();

// In-memory (tests, caches)
U1Database db = U1.openInMemory();

db.close();   // AutoCloseable — safe in try-with-resources
```

### 8.2 Entity definition

Standard JPA/Jakarta annotations — identical to what Ujorm already reads for SQL:

```java
@Entity
@Index(value = "email", type = IndexType.UNIQUE)
@Index(value = "name",  type = IndexType.NON_UNIQUE)
public class Employee {

    @Id
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @JoinColumn(name = "city_id", nullable = true)
    private City city;            // M:1 — stored as Long FK, resolved on read

    // getters / setters, or use as Java Record (see §8.3)
}

@Entity
public class City {

    @Id
    private long id;

    @Column(name = "name", nullable = false)
    private String name;
}
```

Renaming a Java field without changing storage name — no migration needed:

```java
// Storage key stays "first_name"; Java field renamed freely.
@Column(name = "first_name")
private String fullName;
```

### 8.3 Entities as Java Records

Records are supported as read-only projections or as fully persistent entities when
all fields map to persistent columns:

```java
@Entity
public record CityRecord(
    @Id long id,
    @Column(name = "name") String name
) {}
```

### 8.4 CRUD via EntityManager

```java
EntityManager<Employee> em = db.entityManager(Employee.class);

// Insert — returns generated id
long id = em.insert(new Employee(0L, "Alice", "alice@example.com", city));

// Find by primary key
Optional<Employee> emp = em.findById(id);

// Update
emp.get().setName("Alice Doe");
em.update(emp.get());

// Delete
em.delete(emp.get());
em.deleteById(id);

// Exists
boolean exists = em.existsById(id);
```

### 8.5 Querying via SelectQuery

The API is identical to the existing SQL-backed `SelectQuery`. The backend is
switched transparently:

```java
// All employees in Prague, ordered by name
List<Employee> result = SelectQuery.run(db, Employee.class, q -> q
    .column(MetaEmployee.id)
    .column(MetaEmployee.name)
    .column(MetaEmployee.city, MetaCity.name)   // M:1 resolved in one pass
    .where(MetaCity.name.whereEq("Prague"))
    .orderBy(MetaEmployee.name)
    .toList()
);

// LEFT JOIN semantics — employees regardless of city assignment (nullable FK)
List<Employee> all = SelectQuery.run(db, Employee.class, q -> q
    .column(MetaEmployee.name)
    .column(MetaEmployee.city, MetaCity.name)
    .toList()
);

// Compound criterion
Criterion<Employee, Boolean> filter =
    MetaEmployee.name.whereStartsWith("A")
    .and(MetaEmployee.city.innerJoin(MetaCity.name.whereEq("Prague")));

long count = SelectQuery.run(db, Employee.class, q -> q
    .where(filter)
    .toCount()
);

// Lazy stream — avoids materialising large result sets
try (Stream<Employee> stream = SelectQuery.run(db, Employee.class, q -> q
        .where(MetaEmployee.active.whereEq(true))
        .toStream())) {
    stream.forEach(this::process);
}
```

### 8.6 Transactions and sessions

```java
try (U1Session session = db.openSession()) {
    EntityManager<Employee> em = session.entityManager(Employee.class);

    em.insert(new Employee(0L, "Bob",   "bob@example.com",   city));
    em.insert(new Employee(0L, "Carol", "carol@example.com", city));

    session.savepoint("sp1");
    em.deleteById(99L);
    session.rollbackToSavepoint("sp1");

    session.commit();
}
// Auto-rollback on exception if commit was not reached
```

### 8.7 Schema management

```java
SchemaManager schema = db.schema();

// Index operations
schema.createIndex(MetaEmployee.email, IndexType.UNIQUE);
schema.dropIndex(MetaEmployee.email);
schema.reindex(Employee.class);              // rebuild all indexes from data

// Inspection
SchemaInfo         info  = schema.describe(Employee.class);
List<SchemaDiff>   diffs = schema.diff(Employee.class);

// Explicit field-type migration (only when auto-conversion is unsafe)
schema.migrate(Employee.class, m ->
    m.convertField("salary", raw -> Long.parseLong(raw.toString()))
);

// Housekeeping
schema.compact(Employee.class);   // remove stored bytes for dropped fields
schema.drop(Employee.class);      // delete entire collection (irreversible)
```

### 8.8 Schema modes

```java
public enum SchemaMode {

    /** Add new fields and indexes automatically. Never drop or rename. Default. */
    AUTO_UPDATE,

    /** Throw U1SchemaException if stored metadata differs from the entity class. */
    VALIDATE,

    /** Drop and recreate all collections on open. Intended for tests only. */
    CREATE,

    /** Do nothing. Trust the caller entirely. */
    NONE
}
```

---

## 9. Schema Evolution Rules

| Change | AUTO_UPDATE behaviour | Manual step required |
|---|---|---|
| Add field | Old records return `null` / default | None |
| Remove field | Old data retained but ignored on read | `schema.compact()` to reclaim space |
| Rename Java field | Use `@Column(name="oldName")` | None |
| Add `@Index` | Index rebuilt automatically on next open | None |
| Remove `@Index` | Index map dropped automatically | None |
| Change type (safe: `int`→`long`) | Auto-converted on read | None |
| Change type (unsafe: `String`→`Long`) | `U1SchemaException` at startup | `schema.migrate(...)` |
| Rename `@Entity` storage name | Use `@Entity(storeName="oldName")` | None |

---

## 10. Internal Storage Layout

Each `@Entity` maps to two or more `MVMap` entries inside the single `.u1` file:

```
"_meta"                       → MVMap<String, byte[]>   (schema metadata per entity)
"employee"                    → MVMap<Long,   byte[]>   (primary data)
"employee.idx.email"          → MVMap<String, long[]>   (unique index)
"employee.idx.name"           → MVMap<String, long[]>   (non-unique index)
"city"                        → MVMap<Long,   byte[]>
```

Row serialisation uses a compact tag-length-value (TLV) binary format driven by the
`Key` metadata (field ordinal → value). Unknown tags are skipped on read, enabling
forward compatibility when fields are added by a newer version of the application.

---

## 11. Dependency Budget

| Artifact | Size | Role |
|---|---|---|
| `com.h2database:h2-mvstore` | ~200 KB | Storage, MVCC, crash recovery |
| `ujo-orm` (existing) | existing | SelectQuery, EntityManager, Key, Criterion |
| `ujo-tools` (existing) | existing | Type system, utilities |
| **New U1 module own code** | target ~150 KB | MvStoreBackend + engine |

Total added dependency: **~200 KB**. No Jackson, no OkHttp, no Lucene.

---

## 12. Storage Engine: MVStore Alternatives and Rationale

### 12.1 Pure-Java embedded key-value store landscape

| Library | Size | MVCC | Transactions | Crash-safe | License | Maintenance |
|---|---|---|---|---|---|---|
| **H2 MVStore** | ~200 KB | ✅ native | ✅ `TransactionStore` | ✅ COW | MPL 2.0 / EPL 1.0 | ✅ active (H2 project) |
| **MapDB** | ~350 KB | ❌ locking | ✅ limited | ✅ | Apache 2.0 | ⚠️ slow (last release 2023) |
| **LevelDB Java** (iq80) | ~150 KB | ❌ | ❌ single writer | ✅ | Apache 2.0 | ⚠️ minimal activity |
| **Xodus** (JetBrains) | ~3–5 MB | ✅ native | ✅ | ✅ | Apache 2.0 | ✅ active (YouTrack) |
| **Chronicle Map** | ~500 KB | ❌ | ❌ | ❌ by default | Apache 2.0 | ✅ active |
| **RocksDB / LMDB** | native code | ✅ | ✅ | ✅ | various | ✅ active |

RocksDB and LMDB are excluded because they require native libraries — U1 must be
pure Java with zero native dependencies.

### 12.2 Why MVStore

MVStore is the only pure-Java embedded store that combines all four requirements
at once: MVCC, full transactions with savepoints, crash safety, and a sub-250 KB
footprint with zero transitive dependencies. It is already published as a standalone
Maven artifact (`com.h2database:h2-mvstore`) completely decoupled from H2's SQL
layer. Nitrite v3+ uses the same artifact for the same reasons — it is a proven fit
for exactly this use case.

### 12.3 Should MVStore be forked or cloned?

**No.** The reasons:

- **Security and bug fixes** flow automatically via a version bump in `pom.xml`.
  A fork would require manual backporting of every H2 patch indefinitely.
- **The artifact is already clean.** `h2-mvstore` has no dependency on H2's SQL
  engine, parser, or server. There is no coupling to untangle.
- **The license permits embedding** in both open-source and commercial products
  without forking (MPL 2.0 / EPL 1.0).
- **API surface is narrow and stable.** U1 uses only `MVStore`, `MVMap`, and
  `TransactionStore`. These interfaces have not had breaking changes across multiple
  major H2 releases.

The only hypothetical reason to fork would be if U1 needed to change MVStore's
on-disk format or add storage-level features absent from upstream. That is not
required for v1 and is unlikely to be required in the foreseeable future.

---

## 14. Competitor Comparison

| Feature | **U1** | **H2** | **ArcadeDB** | **OrientDB** |
|---|---|---|---|---|
| Pure Java | ✅ | ✅ | ✅ (Java 21+) | ✅ |
| Embedded mode | ✅ | ✅ | ✅ | ✅ |
| Server / network mode | ❌ v1 (roadmap) | ✅ | ✅ | ✅ |
| Query language | Java API (Ujorm Keys) | SQL | AQL + SQL subset | SQL variant (OSQL) |
| SQL parser required | ❌ | ✅ | ✅ | ✅ |
| JDBC driver | ❌ | ✅ | ✅ | ✅ |
| Type-safe query API | ✅ (Key pattern) | ❌ | ❌ | ❌ |
| M:1 relations / JOIN | ✅ native | ✅ (SQL JOIN) | ✅ (graph edges) | ✅ (graph edges) |
| ACID transactions | ✅ | ✅ | ✅ | ✅ |
| Parallel writes (MVCC) | ✅ | ✅ | ✅ | ✅ |
| In-memory mode | ✅ | ✅ | ✅ | ✅ |
| Schema from annotations | ✅ auto | ⚠️ DDL or hbm2ddl | ⚠️ DDL or auto | ⚠️ DDL or auto |
| Schema migration | annotation-driven | Flyway / manual SQL | manual | manual |
| Stored procedures | ❌ | ✅ | ✅ | ✅ |
| Full-text search | ❌ v1 | ✅ | ✅ | ✅ |
| Graph model | ❌ | ❌ | ✅ | ✅ |
| JAR size (own code) | ~350 KB target | ~2.5 MB | ~25 MB | ~8 MB |
| External dependencies | MVStore only | none | several | several |
| Virtual thread friendly | ✅ designed for | ⚠️ not optimised | ⚠️ not optimised | ⚠️ not optimised |
| Java Records support | ✅ | ⚠️ via workarounds | ❌ | ❌ |
| Active development | planned | ✅ | ✅ | ⚠️ fragmented |
| Learning curve (Ujorm user) | zero | low (SQL) | medium (AQL) | medium (OSQL) |

### When to choose U1 over the alternatives

**Choose U1 when:**
- You want **type-safe query construction via Java API** — no SQL strings,
  compile-time field references, IDE auto-complete, and refactoring support
- You want a **clear migration path to JDBC**: switching from U1 to H2, PostgreSQL,
  or any other SQL backend requires changing only the backend configuration —
  `SelectQuery` and `EntityManager` call sites remain untouched
- You need M:1 relations with JOIN semantics but not the weight of a full SQL engine
- You need fast, isolated test databases with `openInMemory()` — zero disk I/O,
  no cleanup
- Minimal JAR size and zero extra dependencies matter

**Choose H2 when:**
- You need full SQL compatibility or an existing SQL schema
- You need a JDBC driver for integration with third-party tools
- Your team is more comfortable with SQL than with a Java DSL

**Choose ArcadeDB when:**
- You need a multi-model database (graph + document + vector)
- You need server mode alongside embedded
- AQL's expressiveness justifies the ~25 MB footprint

**Choose OrientDB when:**
- You have an existing OrientDB installation (note: development is fragmented;
  consider ArcadeDB for new projects)

---

## 15. Open Questions

1. **Serialisation format** — binary TLV (zero deps, fast) vs. Jackson JSON
   (human-readable, adds ~1.5 MB)? Initial lean: custom binary using Ujorm type system.

2. **`QueryBackend` SPI location** — should the SPI live in `ujo-orm` (cleaner
   architecture) or in the new U1 module (avoids touching the stable ORM module)?

3. **Aggregate functions** — `COUNT`, `SUM`, `MIN`, `MAX`, `AVG` are feasible with
   a full-scan evaluator. Worth including in v1?

4. **Compound indexes** — MVStore supports composite keys natively. Should v1 expose
   multi-field indexes?

5. **Auto-increment ID strategy** — use `MVStore`'s atomic long counter per collection,
   or let the user assign IDs explicitly (with optional generator)?

6. **Write-conflict retry** — should U1 offer a built-in retry policy for
   `U1WriteConflictException`, or leave retry logic entirely to the caller?

7. **Module name** — `ujo-u1`? `ujorm-store`? `ujo-nosql`?

---

## 16. Relationship to Existing Ujorm Modules

```
ujo-tools   — type system, no changes needed
ujo-core    — Key pattern, Criterion, no changes needed
ujo-orm     — EntityManager, SelectQuery: add QueryBackend SPI (minimal change)
ujo-u1      — new module: MvStoreBackend, MvStoreEngine, RowSerializer, SchemaManager
```

The existing SQL path is untouched. U1 is additive — a user can run both backends
in the same application if needed (e.g. H2 for legacy data, U1 for a new embedded
subsystem).

---

## 17. Work Breakdown and Effort Estimates

All estimates assume **one experienced Java developer working full-time (8-hour
working days)**. Estimates cover net productive coding time; they do not include
holidays, meetings, or onboarding. The "With Claude" column reflects realistic
acceleration observed in AI-assisted development: ~80–90% for pure boilerplate,
~50–60% for algorithm implementation, ~60–70% for test generation, and ~15–25%
for architecture decisions and debugging.

### 17.1 Task breakdown

| # | Task | Description | Without AI | With Claude |
|---|---|---|---|---|
| 1 | Project setup | New Maven module `ujo-u1`, BOM wiring, MVStore dependency, CI config | 2 d | 0.5 d |
| 2 | `QueryBackend` SPI | Interface design in `ujo-orm`; integration points in `SelectQuery` and `EntityManager` | 3 d | 1 d |
| 3 | `RowSerializer` | TLV binary encoder/decoder for all 9 data types; null handling; field ordinal mapping via Ujorm `Key` metadata | 10 d | 5 d |
| 4 | `MvStoreEngine` | MVStore lifecycle; one `MVMap<Long, byte[]>` per entity; atomic ID counter per collection | 7 d | 4 d |
| 5 | `CriterionEvaluator` | Pattern-matching walk over sealed `Criterion` hierarchy; all operators (`eq`, `gt`, `in`, `regex`, `text`, `between`, `elemMatch`, `AND`, `OR`, `NOT`) | 10 d | 5 d |
| 6 | `IndexManager` | Secondary `MVMap<Object, long[]>` per `@Index`; index maintenance on insert / update / delete; index-scan path in query executor | 10 d | 5 d |
| 7 | `RelationResolver` | M:1 FK lookup in related `MVMap` in a single query pass; nullable FK = LEFT JOIN semantics | 7 d | 4 d |
| 8 | `EntityManager` adapter | `insert`, `findById`, `update`, `delete`, `deleteById`, `existsById`; routes through active session transaction | 5 d | 2 d |
| 9 | `SelectQuery` executor | Full-table scan + index scan; `ORDER BY` (in-memory sort); `LIMIT` / `OFFSET`; `toList()`, `toStream()`, `toCount()`; M:1 column resolution | 10 d | 5 d |
| 10 | `TransactionCoordinator` | `U1Session` lifecycle; MVStore `TransactionStore` wiring; `SAVEPOINT`; auto-rollback on close | 5 d | 3 d |
| 11 | `SchemaManager` | Metadata persistence in `_meta` MVMap; schema diff; `AUTO_UPDATE` / `VALIDATE` / `CREATE` / `NONE` modes; index rebuild; `schema.migrate()` API | 10 d | 5 d |
| 12 | In-memory mode | `U1.openInMemory()` via MVStore in-memory store; builder `.inMemory()` flag | 2 d | 0.5 d |
| 13 | Stream API | Lazy `Stream<T>` over `MVMap` cursor; `AutoCloseable` wrapper; `SequencedCollection` result type | 5 d | 2 d |
| 14 | Unit tests | Per-component isolation tests: serialiser round-trips, criterion evaluation, index correctness, schema diff logic | 15 d | 7 d |
| 15 | Integration tests | End-to-end scenarios: CRUD, M:1 JOIN, transaction + rollback, schema evolution, in-memory / file parity | 10 d | 5 d |
| 16 | Performance & load tests | See §17.2 | 10 d | 7 d |
| 17 | Documentation & examples | Javadoc on all public API; tutorial test class (modelled on Ujorm's `TutorialTest`); module README | 5 d | 2 d |

### 17.2 Performance and load tests

Performance testing is treated as a first-class deliverable, not an afterthought.
It is estimated separately because it involves profiling cycles that cannot be
fully accelerated by AI.

| Test type | Tool | What is measured |
|---|---|---|
| Insert throughput | JMH | Single-threaded inserts/s; batch vs. single commit |
| Read throughput | JMH | `findById` latency; full-scan throughput at 10K / 100K / 1M rows |
| Index scan vs. full scan | JMH | Speedup ratio for indexed vs. non-indexed `WHERE` clause |
| M:1 resolution overhead | JMH | Cost of FK lookup per row in a JOIN result set |
| Concurrent write throughput | JMH + virtual threads | N virtual threads writing independently; conflict rate measurement |
| Write-conflict recovery | custom | Retry latency under high contention (same-row writes) |
| Memory footprint | JVM heap profiler | Heap usage at rest; per-row overhead; cache pressure |
| Startup time | JUnit timing | Time to `U1.open()` with 1M rows on disk |
| Comparison baseline | JMH | Same scenarios against H2 in-memory and H2 file-based |

Claude assists with: JMH benchmark scaffolding (~70% acceleration), test data
generators, and result analysis. Manual work dominates: profiling, root-cause
analysis of regressions, and tuning MVStore cache parameters.

### 17.3 Summary

| Scope | Without Claude | With Claude |
|---|---|---|
| Core engine (tasks 1–13) | ~76 days / **~15 weeks** | ~42 days / **~8 weeks** |
| Tests (tasks 14–15) | ~25 days / **~5 weeks** | ~12 days / **~2.5 weeks** |
| Performance tests (task 16) | ~10 days / **~2 weeks** | ~7 days / **~1.5 weeks** |
| Documentation (task 17) | ~5 days / **~1 week** | ~2 days / **~0.5 week** |
| **Total** | **~116 days / ~23 weeks** | **~63 days / ~12.5 weeks** |

**Acceleration factor: ~1.8×** overall (lower than pure boilerplate projects because
architecture decisions, profiling, and debugging are not significantly accelerated).

### 17.4 Suggested delivery phases

```
Phase 1 — Foundation          (tasks 1–4, 12)       ~2 weeks with Claude
  Result: MVStore opens, rows serialise/deserialise, EntityManager CRUD works.
  Milestone: TutorialTest inserts and reads Employee records.

Phase 2 — Query engine        (tasks 5–7, 8–9)      ~3 weeks with Claude
  Result: SelectQuery with WHERE, ORDER BY, M:1 JOIN executes against MVStore.
  Milestone: TutorialTest JOIN queries return correct results.

Phase 3 — Transactions        (task 10)              ~1 week with Claude
  Result: Concurrent sessions commit and roll back independently.
  Milestone: Two-thread conflict test passes.

Phase 4 — Schema management   (task 11)              ~1.5 weeks with Claude
  Result: AUTO_UPDATE adds fields/indexes on restart; VALIDATE rejects drift.
  Milestone: Schema evolution test suite passes.

Phase 5 — Quality             (tasks 13–17)          ~5 weeks with Claude
  Result: Stream API, full test suite, JMH benchmarks, documentation.
  Milestone: Publish to Maven Central as 1.0.0-alpha.
```

**Total calendar time with Claude: ~12.5 weeks (~3 months) for one developer.**

---

*Draft written: 2026-06-05*
*Project: https://github.com/pponec/ujorm*
