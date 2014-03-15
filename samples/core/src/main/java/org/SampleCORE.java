/*
 *  Copyright 2011-2014 Pavel Ponec
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

package org;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoComparator;
import org.ujorm.criterion.*;
import static org.Company.CITY;
import static org.Employee.*;

/**
 * The tutorial in the class for the UJO CORE <br>
 * ------------------------------------------ <br>
 * Learn the basic skills in 15 minutes by a live Java code.
 *
 * Entities: <pre>
 *  - Employee [id, name, wage, address]
 *  - Company [id, city, country]
 * </pre>
 *
 * Copyright 2011, Pavel Ponec
 *
 * @see Employee
 * @see Company
 */
public class SampleCORE {

    // ======= TUTORIAL MENU: =======

    public static void main(String[] args) {
        SampleCORE sample = new SampleCORE();

        try {
            sample.writeAndRead();
            sample.copyAllProperties();
            sample.copyAttributesByType();
            sample.restoreDefaultValues();
            sample.compositeKey();
            sample.valueOfCompositeKey();
            sample.employeeValidator();
            sample.filterEmployeeListByConstant();
            sample.filterEmployeeListByKey();
            sample.sortEmployeeList();
            sample.filterAndSortList();

        } catch (Exception e) {
            Logger.getLogger(SampleCORE.class.getName()).log(Level.SEVERE, "Sample CORE", e);
        }
    }

    // ======= CHAPTERS: =======

    /** How to write and read data of the UJO object? */
    public void writeAndRead() {
        Employee person = new Employee();

        // Write:
        person.set(ID, 7L);
        person.set(NAME, "Pavel");
        person.set(WAGE, 20.00);
        person.set(COMPANY, new Company());

        // Read:
        Long id = person.get(ID);
        String name = person.get(NAME);
        double wage = person.get(WAGE); // result is not null allways
        Company address = person.get(COMPANY);

        System.out.println("Employee: " + id + " " + name + " " + wage + " " + address);

        // == Sample of compilation bugs: ==
        // person.set(AnotherID, 7L);  // Key from another object is not allowed
        // person.set(ID, "Pavel");    // Wrong data type of the parameter
        // String id = person.get(ID); // Wrong the return data type
    }

   /** How to copy all key values from BO to another object? */
    public void copyAllProperties() throws Exception {
        Ujo employee1 = getEmployee();
        Ujo employee2 = employee1.getClass().newInstance();

        for (Key<Ujo,?> key : employee1.readKeys()) {
            key.copy(employee1, employee2);
        }

        assert ((Employee)employee1).getId()
            == ((Employee)employee2).getId() : "Compare the IDs";
    }

   /** How to copy some key values to another object? */
    public void copyAttributesByType() {
        Employee employee1 = getEmployee();
        Employee employee2 = new Employee();

        for (Key<Ujo,?> key : employee1.readKeys()) {
            if (key.isTypeOf(String.class)) {
                key.copy(employee1, employee2);
            }
            if (key.equals(Employee.WAGE)) { // Key have got an unique instance
                key.copy(employee1, employee2);
            }
        }

        assert employee1.get(ID) != employee2.get(ID) : "Compare the IDs";
        assert employee1.get(NAME) == employee2.get(NAME) : "Compare the NAMEs";
    }

    /** How to restore default values) */
    @SuppressWarnings("unchecked")
    public void restoreDefaultValues() {
        Employee employee = getEmployee();

        for (Key key : employee.readKeys()) {
             employee.set(key, null);
        }
        assert employee.get(WAGE).equals(WAGE.getDefault())
                : "Check the default value";
    }

    /** How to concatenate UJO Keys? */
    public void compositeKey() {
        Employee employee = getEmployee();

        final String city1, city2;
        city1 = employee.get(COMPANY).get(CITY);
        city2 = employee.get(COMPANY.add(CITY)); // If the Company is null then the result is null too.

        assert (city1==city2) : "The same streets ";
        assert COMPANY.add(CITY).toString().equals("company.city")
                : "Check composite key name";
    }

    /** Setter by a composite key does not need references */
    public void valueOfCompositeKey() {
        Employee employee = new Employee();
        employee.set(COMPANY.add(CITY), "Prague"); // method creates new instance of Compopany

        assert employee.get(COMPANY).get(CITY) == "Prague"
                : "Check the value";
    }

    /** Employee validator */
    public void employeeValidator() {
        Criterion<Employee> validator = Employee.WAGE.whereGt(100.0);
        try {
            validator.validate(getEmployee(), "Minimal WAGE is: %f.", validator.getRightNode());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Filter all employees, where a city name of a company equals employee name. */
    public void filterEmployeeListByConstant() {
        List<Employee> employees = COMPANY.add(CITY)
                .whereEq("Prague")
                .evaluate(getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.get(COMPANY.add(CITY)) + " " + employee.get(NAME));
        }
        System.out.println(employees.size());
    }

    /** Filter all employees, where a <strong>city name</strong> equals to the <strong>employee's name</strong>.
     * Note: the result Employee name is 'Prague'. */
    public void filterEmployeeListByKey() {
        List<Employee> employees = COMPANY.add(CITY)
                .whereEq(NAME)
                .evaluate(getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.get(COMPANY.add(CITY)) + " " + employee.get(NAME));
        }

        assert employees.size() == 1 : "Check the result count";
    }

    /** How to sort the List?  */
    public void sortEmployeeList() {
        List<Employee> employees = UjoComparator
                .newInstance(COMPANY.add(CITY), NAME.descending())
                .sort(getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.get(COMPANY.add(CITY)) + " " + employee.get(NAME));
        }

        assert employees.size() == 4 : "Check the result count";
    }

    /** Filter and sort a Employee list using the class CriteriaTool. */
    public void filterAndSortList() {
        CriteriaTool<Employee> tool = CriteriaTool.newInstance();

        // Select including sorting informations:
        List<Employee> employees = tool.select
                ( getEmployees()
                , WAGE.whereGt(5.0)
                , UjoComparator.newInstance(Employee.NAME.descending())
                );

        for (Employee employee : employees) {
            System.out.println("Filtered employee: " + employee);
        }
        assert employees.size() == 3 : "Check the result count";
    }

    /** Samples of WeakKey using are located in a separated class. */
    public void howToUseWeakKey() {
        new SampleWeakKeyService().testWeakKeys2List();
        new SampleWeakKeyService().testWeakKeys2Map();
        new SampleWeakKeyService().testWeakKeyAttributes();
    }

    // ======= Helper methods =======

    /** Find an Employee somewhere */
    private Employee getEmployee() {
        return createEmployee(10L, "Pavel", 50.00, getCompany());
    }

    /** Create the List of Persons */
    private Employee createEmployee(Long id, String name, Double wage, Company company) {
             Employee person = new Employee();
             person.set(ID, id);
             person.set(NAME, name);
             person.set(WAGE, wage);
             person.set(COMPANY, company);
             return person;
    }


    /** Find an Company somewhere */
    private Company getCompany() {
        return createCompany(20L, "My Company", "Prague");
    }


    /** Find an Company somewhere */
    private Company createCompany(Long id, String name, String city) {
        Company result = new Company();
        result.set(Company.ID, id);
        result.set(Company.NAME, name);
        result.set(Company.CITY, city);
        result.set(Company.CREATED, new Date());

        return result;
    }

    /** Create the List of Persons */
    private List<Employee> getEmployees() {
        final List<Employee> result = new ArrayList<Employee>();

        result.add(createEmployee(10L, "Pavel", 50.00, getCompany()));
        result.add(createEmployee(20L, "Petr", 80.00, getCompany()));
        result.add(createEmployee(30L, "Kamil", 20.00, getCompany()));
        result.add(createEmployee(40L, "Prague", 00.00, getCompany()));

        return result;
    }

    /** Set logging to the level SEVERE. */
    private void logSevere() {
        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(Level.SEVERE);
    }

}
