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

package org.ujorm.gxt.client.samples;

import org.ujorm.gxt.client.cquery.CCriterion;
import org.ujorm.gxt.client.CujoProperty;
import com.extjs.gxt.ui.client.widget.menu.Item;
import org.ujorm.gxt.client.Cujo;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.gxt.client.cquery.CQuery;
import static org.ujorm.gxt.client.cquery.COperator.*;
import static org.ujorm.gxt.client.samples.Company.CITY;
import static org.ujorm.gxt.client.samples.Employee.*;

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
public class SampleGxtModel {

    // ======= TUTORIAL MENU: =======

    public static void main(String[] args) {
        SampleGxtModel sample = new SampleGxtModel();

        try {
            sample.writeAndRead();
            sample.copyAllProperties();
            sample.copySomeProperties();
            sample.restoreDefaultValues();
            sample.concatenateProperties();
            sample.employeeValidator();
            sample.selectFromDatabase();

        } catch (Exception e) {
            Logger.getLogger(SampleGxtModel.class.getName()).log(Level.SEVERE, "Sample CORE", e);
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
        Cujo employee1 = findEmployee();
        Cujo employee2 = employee1.createInstance();

        for (CujoProperty p : employee1.readProperties()) {
            p.copy(employee1, employee2);
        }
        System.out.println("Employee 2: " + employee2);
    }

   /** How to copy some properties to another object? */
    public void copySomeProperties() {
        Employee employee1 = findEmployee();
        Employee employee2 = new Employee();

        for (CujoProperty p : employee1.readProperties()) {
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
        for (CujoProperty p : employee.readProperties()) {
             employee.set(p, p.getDefault());
        }

        // Another solution:
        employee.set(WAGE, null);
        double newWage = employee.get(WAGE) + 10L; // type safe

        System.out.println("Employee: " + employee + " wage: " + newWage);
    }

    /** How to concatenate UJO Properties? */
    public void concatenateProperties() {
        final String city1, city2;
        Employee employee = findEmployee();

        city1 = employee.get(COMPANY).get(CITY);
        city2 = employee.get(COMPANY.add(CITY)); // Case: Company is NULL ?

        System.out.println("The same streets: " + (city1==city2) );
        System.out.println("The composite property: " + COMPANY.add(CITY) );
    }

    public void employeeValidator() {
        final CCriterion<Employee> validWage, validStreet, validator;
        validWage = CCriterion.where(WAGE, GT, 10.0);
        validStreet = CCriterion.where(COMPANY.add(CITY), "Brno");
        validator = validWage.or(validStreet);

        Employee employee = findEmployee();
        boolean isValid = validator.evaluate(employee);
        System.out.println("Is valid: " + isValid + " for " + employee);
    }


    /** A small introduction to database Object-Relation-Mapping */
    public void selectFromDatabase() {
         CCriterion<Employee> crn1, crn2, criterion;

         crn1 = CCriterion.where( Employee.ID, GE, 1L );
         crn2 = CCriterion.where( Employee.COMPANY.add(Company.CREATED), LT, new Date());
         criterion = crn1.and(crn2);
         
         CQuery query = CQuery.newInstance(Employee.class
                 , Employee.NAME
                 , Employee.COMPANY.add(Company.NAME)
                 );

         List<Item> items = callRcp4ItemList(query);

    }

    // ======= Helper methods =======

    /** Find an Employee somewhere */
    private Employee findEmployee() {
        Employee result = new Employee();
        result.set(ID, 10L);
        result.set(NAME, "Pavel");
        result.set(WAGE, 50.00);
        result.set(COMPANY, findCompany());

        return result;
    }

    /** Find an Company somewhere */
    private Company findCompany() {
        Company result = new Company();
        result.set(Company.ID, 20L);
        result.set(Company.NAME, "My Company");
        result.set(Company.CITY, "Prague");
        result.set(Company.CREATED, new Date());

        return result;
    }

    /** Create the List of Persons */
    private List<Employee> findEmployeeList() {
        List<Employee> result = new ArrayList<Employee>();

        for (long i=1; i<=3; ++i) {
             Employee person = findEmployee();
             person.set(ID, i);
             person.set(NAME, "Name-" + i);
             result.add(person);
        }
        return result;
    }

    /** Call a RCP method. */
    private List<Item> callRcp4ItemList(CQuery query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
