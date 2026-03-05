# Ujorm 3: Rychlý start a tutoriál

Ujorm 3 je nový, lehký a rychlý ORM framework pro Javu. Je navržen s ohledem na maximální výkon, jednoduchost a bezpečné použití. Podporuje mapování na standardní Java Beans i moderní třídy typu Record. Zásadní výhodou je, že nevyhazuje kontrolované výjimky (využívá vlastní nekontrolovanou `SQLException`), což výrazně zjednodušuje psaní kódu.

---

## Architektura a hlavní komponenty

Celé řešení stojí na třech nezávislých, ale vzájemně spolupracujících třídách:

* **EntityManager (a rozhraní Crud):** Slouží ke správě entit. Rozhraní `Crud` poskytuje metody pro standardní databázové operace (Insert, Read, Update, Delete) a pro svůj běh vyžaduje databázové spojení (`Connection`).
* **ResultSetMapper:** Autonomní třída, která mapuje výsledky z databáze (`Stream<ResultSet>`) přímo na doménové objekty. Zvládá i komplexní hierarchické mapování pomocí tečkové notace (např. `city.id`). Nepotřebuje k běhu `EntityManager`, ale `EntityManager` ji využívá interně.
* **SqlParamBuilder:** Zcela nezávislý nástroj pro bezpečné a pohodlné ruční sestavování SQL dotazů s podporou pojmenovaných parametrů. Může vracet `Stream<ResultSet>`, který pak snadno zpracujete pomocí `ResultSetMapperu`.

---

## 1. Definice entit

Framework podporuje jak tradiční POJO, tak třídy typu Record. Ukážeme si definici obou typů. Všimněte si specifického formátování JavaDocu pro třídy Record.

### Entita City (Record)

/** City entity */
public record City(
/** The city identifier */
Long id,
/** The city name */
String name,
/** The country code */
String country,
/** The geographical latitude */
Double latitude,
/** The geographical longitude */
Double longitude
) {
}

### Entita Employee (Java Bean / POJO styl)

import org.ujorm.core.SnapshotProvider;
import java.time.LocalDate;

/** Employee entity */
public class Employee implements SnapshotProvider<Employee> {
private Long id;
private String name;
private Employee superior;
private City city;
private LocalDate contractDay;
private boolean active;
private Employee snapshot;

    /** Gets the employee identifier */
    public Long getId() { return id; }
    /** Sets the employee identifier */
    public void setId(Long id) { this.id = id; }

    /** Gets the name */
    public String getName() { return name; }
    /** Sets the name */
    public void setName(String name) { this.name = name; }

    /** Gets the superior employee */
    public Employee getSuperior() { return superior; }
    /** Sets the superior employee */
    public void setSuperior(Employee superior) { this.superior = superior; }

    /** Gets the associated city */
    public City getCity() { return city; }
    /** Sets the associated city */
    public void setCity(City city) { this.city = city; }

    /** Gets the contract day */
    public LocalDate getContractDay() { return contractDay; }
    /** Sets the contract day */
    public void setContractDay(LocalDate contractDay) { this.contractDay = contractDay; }

    /** Checks if the employee is active */
    public boolean isActive() { return active; }
    /** Sets the active status */
    public void setActive(boolean active) { this.active = active; }

    @Override
    public Employee readSnapshot() { return snapshot; }
    
    public Employee saveSnapshot() {
        this.snapshot = new Employee();
        this.snapshot.setId(this.id);
        this.snapshot.setName(this.name);
        this.snapshot.setActive(this.active);
        // ... copy other necessary fields
        return this;
    }

    /** Creates a new instance of Employee */
    public static Employee of(Long id, String name, Employee superior, City city, LocalDate contractDay, boolean active) {
        var employee = new Employee();
        employee.setId(id);
        employee.setName(name);
        employee.setSuperior(superior);
        employee.setCity(city);
        employee.setContractDay(contractDay);
        employee.setActive(active);
        return employee;
    }
}

---

## 2. Operace CRUD (Create, Read, Update, Delete)

K provádění standardních operací slouží třída `EntityManager`. Ta by se měla typicky inicializovat pouze jednou (např. při startu aplikace) a následně se z ní získává instance `Crud` pro konkrétní transakci nebo spojení.

import org.ujorm.mapper.core.EntityManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;

/** Tutorial demonstrating CRUD operations */
public class UjormTutorial {

    /** Main execution method */
    public static void runTutorial() {
        try (var dbConnection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")) {
            
            // 1. Initialization of Entity Managers
            var cityManager = EntityManager.of(City.class, Long.class);
            var employeeManager = EntityManager.of(Employee.class, Long.class);

            // 2. Getting CRUD interfaces for the current transaction
            var cityDao = cityManager.crud(dbConnection);
            var employeeDao = employeeManager.crud(dbConnection);

            // --- CREATE ---
            var city = cityDao.insert(new City(null, "Prague", "CZ", 50.0755, 14.4378));
            var employee = employeeDao.insert(Employee.of(null, "Jan Novak", null, city, LocalDate.now(), true));

            // --- READ ---
            var loadedCity = cityDao.read(city.id()).orElseThrow();
            var loadedEmployee = employeeDao.readNullable(employee.getId());

            // --- UPDATE (Partial update using Snapshot) ---
            loadedEmployee.saveSnapshot().setName("Jan Novak ml.");
            employeeDao.updateChanged(loadedEmployee);

            // --- UPDATE (Batch update of specific columns) ---
            loadedEmployee.setActive(false);
            employeeDao.updateBatch(java.util.List.of(loadedEmployee), "active");

            // --- DELETE ---
            employeeDao.delete(loadedEmployee);
            cityDao.deleteById(city.id());

        } catch (java.sql.SQLException e) {
            throw new org.ujorm.tools.jdbc.SQLException(e);
        }
    }
}

---

## 3. Vlastní SQL dotazy a mapování (SqlParamBuilder + ResultSetMapper)

Pokud potřebujete provádět složitější dotazy, které přesahují základní CRUD, využijete `SqlParamBuilder` v kombinaci s `ResultSetMapper`.

import org.ujorm.mapper.ResultSetMapper;
import org.ujorm.tools.jdbc.SqlParamBuilder;
import java.sql.Connection;
import java.util.List;

/** Custom queries tutorial */
public class CustomQueryTutorial {

    /** Finds employees by city name */
    public static List<Employee> findEmployeesByCity(Connection dbConnection, String cityName) {
        var mapper = ResultSetMapper.of(Employee.class);
        
        var sql = """
            SELECT 
                e.id AS "id",
                e.name AS "name",
                e.is_active AS "active",
                e.contract_day AS "contractDay",
                c.id AS "city.id",
                c.name AS "city.name"
            FROM employee e
            JOIN city c ON e.city_id = c.id
            WHERE c.name = :cityName
              AND e.is_active = :activeStatus
            """;

        try (var builder = new SqlParamBuilder(dbConnection)) {
            return builder.sql(sql)
                .bind("cityName", cityName)
                .bind("activeStatus", true)
                .streamMap(rs -> mapper.convert(rs).findFirst().orElse(null))
                .toList();
        }
    }
}

---

## Zpracování výjimek

Při chybách na úrovni databáze framework nevyhazuje standardní kontrolovanou výjimku `java.sql.SQLException`, ale zabalí ji do vlastní nekontrolované výjimky `org.ujorm.tools.jdbc.SQLException` (případně jejího potomka `SqlException` v rámci builderu). Díky tomu není nutné všude psát bloky `try-catch` nebo deklarovat `throws`, což udržuje kód čistý.