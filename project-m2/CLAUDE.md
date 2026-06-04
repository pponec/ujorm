# CLAUDE.md — Ujorm 3

Guidance for AI assistants working with this codebase.

## Modules

| Module | Purpose |
|---|---|
| `ujo-core` | `Key<D,V>` property descriptor, `CriterionProvider` (WHERE factory), `DomainHandler` |
| `ujo-orm` | `EntityManager`, `Crud`, `SelectQuery`, `SqlQuery`, `EntityContext` |
| `ujo-web` | `HtmlElement`, `Element` (HTML builder), `HttpContext`, `HttpParameter` |
| `ujo-tools` | `SqlBuilder`, `AbstractSqlQuery`, `Check`, `Assert` |
| `ujorm-meta-processor` | Annotation processor that generates `QXxx` metamodel classes |

---

## ujo-orm

### Setup

```java
// One EntityContext per application, one EntityManager per entity class — both static singletons
private static final EntityContext CTX = EntityContext.ofDefault();
private static final EntityManager<Printer, Long> PRINTER_EM = CTX.entityManager(Printer.class);
```

`EntityContext` variants:
```java
EntityContext.ofDefault()                        // default config
EntityContext.of(Config.ofDefault())             // explicit config
EntityContext.ofSqlInfoWithParams(true)          // log SQL + params
```

### API priority — always prefer in this order

```
EntityManager.crud()  →  SelectQuery  →  SqlQuery (raw)
```

Use `crud()` for simple CRUD by PK. Use `SelectQuery` for any WHERE/JOIN. Use `SqlQuery` only for DDL, schema migrations, or SQL that has no higher-level equivalent.

---

### Crud<D, V>  — via `em.crud(connection)`

#### INSERT

```java
D     insert(D domain)                                  // single entity — returns entity with generated PK
D[]   insert(D... domains)                              // varargs batch — ALWAYS use the returned array
long  insert(Stream<D> domains, Consumer<D> onInserted) // streaming batch; callback receives inserted entity with PK
D     insertOrUpdate(D domain)                          // INSERT if PK null/0, UPDATE otherwise
long  insertOrUpdate(Stream<D>, Consumer<D>, CharSequence... properties)
```

#### READ

```java
Optional<D>  findById(V id)           // safe — Optional
D            findByIdNullable(V id)   // returns null if not found
<R> R        selectWhere(String whereClause, SqlFunction<SqlQuery, R> fun) // escape hatch to raw SQL
```

**Note:** `findById` does NOT load join projections (e.g. foreign-key entity names). Use `SelectQuery` with `.column(key1, key2)` whenever you need a field from a related entity.

#### UPDATE

```java
long  update(D domain, CharSequence... properties)       // all columns if properties empty; partial if specified
long  update(Stream<D>, CharSequence... properties)      // batch
long  updateChanged(D2 domain)                           // snapshot-based — only actually changed columns sent to DB
long  updateChanged(D2... domains)                       // varargs batch snapshot-based
long  updateChanged(Stream<D2> domains)                  // streaming snapshot-based
```

`updateChanged` requires entity to implement `SnapshotProvider`. Entities with zero changes are skipped entirely.

#### DELETE

```java
int   delete(D domain)              // by PK extracted from entity
int   deleteById(V id)              // by PK value directly — no entity needed
int   delete(Stream<D> domains)     // streaming batch
int   deleteBatchEntities(D... domains) // varargs batch
```

---

### SelectQuery<D>  — SELECT / DELETE with type-safe WHERE

```java
// Static factory — auto-closes, handles exceptions
List<Printer> result = SelectQuery.run(conn, PRINTER_EM, q -> q
        .columns(false)
        .where(QPrinter.isActive.whereEq(true))
        .tail("ORDER BY", QPrinter.name)
        .toList());
```

#### Columns

```java
q.columns(false)   // all columns EXCEPT FK join-columns — use for plain SELECT
q.columns(true)    // all columns INCLUDING FK stubs — required when adding .column() joins
q.column(QPrintProfile.formula, QPriceFormula.name)   // 2-level join projection
q.column(QA.b, QB.c, QC.d)                           // 3-level join projection
```

**Critical rule:**
- `.columns(false)` + no `.column()` → plain SELECT, all own columns
- `.columns(true)` + `.column(key1, key2)` → JOIN projection, FK stub included and filled
- **Never** combine `.columns(false)` with `.column()` — the join column is silently omitted

#### WHERE

```java
q.where(QPrinter.isActive.whereEq(true))                          // single condition
q.whereAnd(QPrinter.isActive.whereEq(true), QPrinter.maxX.whereGe(200)) // multiple ANDed
// Repeated .where() calls are also ANDed
```

#### SQL head override (for DELETE)

```java
q.sql("DELETE").where(QLog.createdAt.whereLt(cutoff)).execute()
```

#### Tail — ORDER BY, LIMIT, FOR UPDATE, GROUP BY

```java
q.tail("ORDER BY", QPrinter.name, "DESC")
q.tail("ORDER BY", QPrinter.isDefault, "DESC,", QPrinter.maxX)  // Key objects resolve to column names
q.tail("LIMIT 10 OFFSET 5")
q.tail("GROUP BY", QLog.profileId, "HAVING COUNT(*) > 1")
```

`Key` objects inside `tail()` are resolved to their quoted column names automatically.

#### Terminals

```java
List<D>      toList()       // all rows as list
Stream<D>    toStream()     // consume or close immediately
Optional<D>  findFirst()    // first row or empty
Optional<D>  findUnique()   // throws IllegalStateException if more than 1 row found
int          execute()      // for DELETE/UPDATE — returns affected row count
```

---

### SqlQuery  — raw SQL with named parameters

```java
// Static factory
SqlQuery.run(conn, q -> q
        .sql("SELECT id, name FROM printer WHERE id > :minId ORDER BY id")
        .bind("minId", 5L)
        .toStream(rs -> new MyDto(rs.getLong(1), rs.getString(2)))
        .toList());

// Named params with :name syntax
q.sql("DELETE FROM log WHERE created_at < :since").bind("since", cutoff).execute();

// IN clause
q.sql("SELECT * FROM t WHERE id IN (:ids)").bind("ids", 1L, 2L, 3L).toStream(mapper).toList();

// Enum binding — honors @Enumerated(ORDINAL/STRING) ORM metadata
q.bind("status", QEmployee.status, EmployeeState.ACTIVE, EmployeeState.PENDING)
```

**`bind` supports:** `Boolean`, `Byte`, `Short`, `Integer`, `Long`, `BigDecimal`, `String`, `LocalDate`, `LocalDateTime`. For other types: `bindObject(name, JDBCType, values...)`.

#### ${COLUMNS} placeholder — dynamic column list

```java
q.sql("SELECT ${COLUMNS} FROM employee t WHERE t.id > :id")
 .column("t.name",    QEmployee.name)
 .column("t.city_id", QEmployee.city, QCity.name)   // join path
 .bind("id", 0L)
 .toStream(EMPLOYEE_EM.mapper())
 .toList();
```

#### ${placeholder} label substitution

```java
q.sql("SELECT * FROM t WHERE ${filter}")
 .label("filter", QEmployee.status)
 .bind(...)
```

---

## ujo-core — Key and CriterionProvider

`Key<DOMAIN, VALUE>` is a property descriptor — static, immutable, contains only metadata.
Generated into `QXxx` metamodel classes by `ujorm-meta-processor`.

### Criterion factory methods (via `QXxx.field.whereXxx(value)`)

```java
// Equality / inequality
.whereEq(VALUE value)          // =
.whereNeq(VALUE value)         // !=   NOTE: "whereNeq", not "whereNe"
.whereNull()                   // IS NULL
.whereNotNull()                // IS NOT NULL

// Comparisons
.whereGt(VALUE value)          // >
.whereGe(VALUE value)          // >=
.whereLt(VALUE value)          // <
.whereLe(VALUE value)          // <=

// IN / NOT IN
.whereIn(VALUE... values)                   // IN (?, ?, ...)
.whereNotIn(VALUE... values)
.whereIn(Collection<VALUE> values)          // from collection
.whereNotIn(Collection<VALUE> values)

// Custom SQL — {0} = column name, {1} = bound value(s)
.whereSql("UPPER({0}) = UPPER({1})", value)
.whereSql("{0} BETWEEN {1} AND {1}", low, high)

// Generic form — use when no named shortcut exists
.where(Operator.EQ, value)
.where(Operator.GE, proxyValue)             // ProxyValue: deferred evaluation
.where(Operator.EQ, otherKey)              // column = otherColumn
```

### Other Key methods

```java
VALUE   getValue(D bean)
void    setValue(D bean, VALUE value)
String  name()                   // "fieldName"
String  fullName()               // "DomainClass.fieldName"
Class<VALUE> type()
KeyInfo info()                   // ORM metadata: foreignKey(), mapEnumByOrdinal()
short   index()                  // position in domain class
```

### ujorm-meta-processor — Q-class generation

Add to Maven `pom.xml`:
```xml
<dependency>
    <groupId>org.ujorm</groupId>
    <artifactId>ujorm-meta-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

Q-classes are generated at compile time into `target/generated-sources/annotations/…/meta/`.
Each domain field becomes a `public static final Key<D,V> fieldName` constant.
Import from the generated `.meta` package, not from the domain class directly.

---

## ujo-web — Element and HtmlElement

### HtmlElement — page root

```java
try (var html = HtmlElement.of(ctx, BOOTSTRAP_CSS, "/app.css")) {
    html.addCssBody("body { margin: 0 }");
    html.addJavascriptLink(true, "/app.js");   // defer=true
    try (var body = html.addBody("container")) { ... }
}

HtmlElement.niceOf(ctx, cssLinks...)   // indented output (development)
```

`cssLinks` are stylesheet URLs, not CSS class names.

### Element — named creator methods (always prefer over `addElement`)

```java
// Layout
el.addDiv(css...)          // <div>
el.addSpan(css...)         // <span>
el.addParagraph(css...)    // <p>
el.addHeading(2, "Title", css...)   // <h2>
el.addUnorderedlist(css...)
el.addOrderedList(css...)
el.addListItem(css...)
el.addPreformatted(css...)
el.addFieldset("Legend", css...)
el.addBreak()              // <br>  — NOT addElement("br")

// Links and media
el.addAnchor(url, css...)          // <a href="url">  — NOT addElement("a").setAttr("href", ...)
el.addImage(src, alt, css...)      // <img src alt>   — NOT addElement("img").setAttr(...)
el.addImg(css...)                  // <img> without src/alt

// Forms
el.addForm(css...)
el.addInput(css...)                // <input>  — NOT addElement("input")
el.addTextInput(css...)            // <input type="text">
el.addPasswordInput(css...)        // <input type="password">
el.addTextArea(css...)
el.addSelect(css...)
el.addOption(css...)
el.addButton(css...)               // <button>  — NOT addElement("button")
el.addSubmitButton(css...)         // <button type="submit">
el.addCheckBox(name, css...)       // hidden false + visible checkbox
el.addHiddenInput(name, value)

// Tables
el.addTable(css...)
el.addTableHead(css...)
el.addTableBody(css...)
el.addTableRow(css...)             // <tr>  — NOT addElement("tr")
el.addTableDetail(css...)          // <td>  — NOT addElement("td")
```

`addElement(name, css...)` is the fallback for tags without a named method:
`nav`, `footer`, `header`, `section`, `article`, `strong`, `small`, `code`, `hr`, `colgroup`, `col`, etc.

### Named attribute setters (always prefer over `.setAttr`)

```java
.setType(value)                    // NOT .setAttr("type", value)
.setValue(value)                   // NOT .setAttr("value", value)
.setName(name)                     // NOT .setAttr("name", name)
.setNameValue(name, value)         // shorthand for .setName().setValue()
.setId(id)                         // NOT .setAttr("id", id)
.setChecked("checked")             // NOT .setAttr("checked", "checked")
.setChecked(cond ? "checked" : null)
.setTitle(text)                    // NOT .setAttr("title", text)
.setHref(url)                      // NOT .setAttr("href", url)
.setFor(inputId)                   // NOT .setAttr("for", id)
.setMethod(Html.V_POST)            // NOT .setAttr("method", "post")
.setAction(url)                    // NOT .setAttr("action", url)
.setAttribute("required")         // boolean attribute — NOT .setAttr("required", "required")
.setAttribute("disabled")
.setClass(css...)                  // secondary — prefer passing css to the addXxx() factory
.setRows(n)  .setCols(n)  .setColSpan(n)  .setRowSpan(n)
```

`.setAttr(name, value)` is the fallback for attributes without a named method:
`data-*`, `aria-*`, `style`, `enctype`, `accept`, `onclick`, `selected`, `src`, `min`, `max`,
`step`, `placeholder`, `size`, `spellcheck`, `autocomplete`, etc.

### HttpContext — request parameters

```java
HttpContext ctx = HttpContext.of(req, resp);

String val   = ctx.parameter("name", "default");
String orNull = ctx.parameter("name", (String) null);
Long id      = ctx.parameter("id", Long::parseLong);          // null on parse failure
boolean flag = ctx.parameter("flag", Boolean::parseBoolean, false);

ctx.sendRedirect("/path?id=" + id);
ctx.getServletRequest()   // Optional<HttpServletRequest>
ctx.getServletResponse()  // HttpServletResponse
```

### HttpParameter — type-safe parameter enum

```java
enum Param implements HttpParameter {
    ACTION, ITEM_ID, NAME;

    @Override public String toString() {
        return name().toLowerCase().replace('_', '-');  // wire name: "item-id"
    }
}

String val = ACTION.of(ctx, "default");
long id    = ITEM_ID.of(ctx, 0L);
```

---

## Common pitfalls

| Wrong | Correct |
|---|---|
| Stream-filter after `toList()` | `.where(QX.field.whereEq(val))` inside SelectQuery |
| `whereNe(value)` | `whereNeq(value)` — method is named `whereNeq` |
| `.columns(false)` + `.column(k1, k2)` | `.columns(true)` + `.column(k1, k2)` |
| `crud().findById(id)` when join field needed | SelectQuery with `.column(QA.b, QB.fieldName)` |
| `addElement("input").setAttr("type","number")` | `addInput(css).setType("number")` |
| `addElement("button").setAttr("class",css)` | `addButton(css)` |
| `addElement("a",css).setAttr("href",url)` | `addAnchor(url, css)` |
| `addElement("br")` | `addBreak()` |
| `addElement("span", css)` | `addSpan(css)` |
| `addElement("tr")` / `addElement("td")` | `addTableRow()` / `addTableDetail(css)` |
| `.setAttr("type", v)` | `.setType(v)` |
| `.setAttr("value", v)` | `.setValue(v)` |
| `.setAttr("id", v)` | `.setId(v)` |
| `.setAttr("title", v)` | `.setTitle(v)` |
| `.setAttr("checked","checked")` | `.setChecked("checked")` |
| `.setAttr("required","required")` | `.setAttribute("required")` |
