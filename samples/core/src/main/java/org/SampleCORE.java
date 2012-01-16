/*
 *  Copyright 2011-2011 Pavel Ponec
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

import org.ujorm.Ujo;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.UjoProperty;
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
            sample.copySomeProperties();
            sample.restoreDefaultValues();
            sample.concatenateProperties();
            sample.filterEmployeeListByConstant();
            sample.filterEmployeeListByProperty();
            sample.sortEmployeeList();
            sample.filterAndSortList();
            sample.employeeValidator();

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
        // person.set(AnotherID, 7L);  // Property from another object is not allowed
        // person.set(ID, "Pavel");    // Wrong data type of the parameter
        // String id = person.get(ID); // Wrong the return data type
    }

   /** How to copy all properties from BO to another object? */
    public void copyAllProperties() throws Exception {
        Ujo employee1 = getEmployee();
        Ujo employee2 = employee1.getClass().newInstance();

        for (UjoProperty p : employee1.readProperties()) {
            p.copy(employee1, employee2);
        }
        System.out.println("Employee 2: " + employee2);
    }

   /** How to copy some properties to another object? */
    public void copySomeProperties() {
        Employee employee1 = getEmployee();
        Employee employee2 = new Employee();

        for (UjoProperty p : employee1.readProperties()) {
            if (p.isTypeOf(String.class)) {
                p.copy(employee1, employee2);
            }
            if (p==Employee.WAGE) { // Property have got an unique instance
                p.copy(employee1, employee2);
            }
        }
        System.out.println("employee 2: " + employee2);
    }

    /** How to restore default values) */
    public void restoreDefaultValues() {
        Employee employee = new Employee();

        employee.set(WAGE, 123.00);
        for (UjoProperty p : employee.readProperties()) {
             employee.set(p, p.getDefault());
        }

        // Another solution:
        employee.set(WAGE, null);
        double newWage = employee.get(WAGE) + 10L; // type safe

        System.out.println("Employee: " + employee + " wage: " + newWage);
    }

    /** How to concatenate UJO Properties? */
    public void concatenateProperties() {
        Employee employee = getEmployee();

        final String city1, city2;
        city1 = employee.get(COMPANY).get(CITY);
        city2 = employee.get(COMPANY.add(CITY)); // If the Company is null then the result is null too.

        System.out.println("The same streets: " + (city1==city2) );
        System.out.println("The composite property: " + COMPANY.add(CITY) );
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

    /** Filter all employees, where a city name equals the Prague. */
    public void filterEmployeeListByProperty() {
        List<Employee> employees = COMPANY.add(CITY)
                .whereEq(NAME)
                .evaluate(getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.get(COMPANY.add(CITY)) + " " + employee.get(NAME));
        }
        System.out.println(employees.size());
    }

    /** How to sort the List?  */
    public void sortEmployeeList() {
        List<Employee> employees = getEmployees();
        Collections.sort(employees, UjoComparator.newInstance(COMPANY.add(CITY), NAME.descending()));

        for (Employee employee : employees) {
            System.out.println(employee.get(COMPANY.add(CITY)) + " " + employee.get(NAME));
        }
        System.out.println(employees.size());
    }

    /** Filter and sort a Employee list using the class CriteriaTool. */
    public void filterAndSortList() {
        CriteriaTool<Employee> ct = CriteriaTool.newInstance();

        // Select include sorting:
        List<Employee> employees = ct.select
                ( getEmployees()
                , WAGE.whereGt(5.0)
                , UjoComparator.newInstance(Employee.NAME.descending())
                );

        for (Employee employee : employees) {
            System.out.println("Filtered employee: " + employee);
        }
        System.out.println(employees.size());
    }

    /** Employee validator */
    public void employeeValidator() {
        Criterion<Employee> validator
                = WAGE.whereGt(10.0)
                .or(COMPANY.add(CITY).whereEq("Prague"));

        Employee employee = getEmployee();
        boolean isValid = validator.evaluate(employee);
        System.out.println("Is valid: " + isValid + " for " + employee);
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
