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
import org.ujorm.Validator;
import org.ujorm.core.UjoComparator;
import org.ujorm.criterion.*;
import org.ujorm.validator.ValidationError;
import static org.Company.CITY;
import static org.Employee.*;

/**
 * The tutorial in the class for the UJO CORE <br>
 * ------------------------------------------ <br>
 * Learn the basic skills in 15 minutes by a live Java code.
 *
 * Entities:
 * <ul>
 *   <li>{@link Employee} [id, name, wage, company]</li>
 *   <li>{@link Company} [id, name, city, created]</li>
 * </ul>
 *
 * Copyright 2011-2014, Pavel Ponec
 *
 * @see Employee
 * @see Company
 */
public class SampleCORE {

    // ======= TUTORIAL MENU: =======

    public static void main(String[] args) {
        SampleCORE sample = new SampleCORE();

        try {
            sample.readWriteAsBean();
            sample.readWriteAsUjo();
            sample.defaultValues();
            sample.keyValidator();
            sample.copyAttributes();
            sample.copyAttributesByType();
            sample.compositeKey();
            sample.compositeKeyAsFactory();
            sample.criterionAsValidator();
            sample.criterionAsFilter();
            sample.criterionAsFilterWithKey();
            sample.sortEmployeeList();

        } catch (Exception e) {
            Logger.getLogger(SampleCORE.class.getName()).log(Level.SEVERE, "Sample CORE", e);
        }
    }

    // ======= CHAPTERS: =======

    /** See a common data access for the JavaBean object */
    public void readWriteAsBean() {
        Employee person = new Employee();

        // Write:
        person.setId(7L);
        person.setName("Pavel");
        person.setWage(20.00);
        person.setCompany(new Company());

        // Read:
        Long id = person.getId();
        String name = person.getName();
        double wage = person.getWage();
        Company company = person.getCompany();

        assert id.equals(7L);
        assert name.equals("Pavel");
        assert wage == 20.00;
        assert company != null;
    }

    /** See a data access using API of the {@link Ujo} object */
    public void readWriteAsUjo() {
        Employee person = new Employee();

        // Write:
        person.set(ID, 7L);
        person.set(NAME, "Pavel");
        person.set(WAGE, 20.00);
        person.set(COMPANY, new Company());

        // Read:
        Long id = person.get(ID);
        String name = person.get(NAME);
        double wage = person.get(WAGE); // result is not never null due the default value
        Company company = person.get(COMPANY);

        assert id.equals(7L);
        assert name.equals("Pavel");
        assert wage == 20.00;
        assert company != null;

        // === Code sample where the compiler fails: ===
        // person.set(Company.ID, 7L);  // Key from another domain is not allowed
        // person.set(Employee.ID, "Pavel");    // Wrong data type of the argument value
        // String id = person.get(Employee.ID); // Wrong return class
    }

    /** How to restore default values) */
    @SuppressWarnings("unchecked")
    public void defaultValues() {
        Employee employee = getEmployee();

        for (Key key : employee.readKeys()) {
            employee.set(key, null);
        }
        assert employee.get(WAGE).equals(WAGE.getDefault())
                : "Check the default value";
    }

    /** There is a {@link Validator} class which checks an input data using the method
     * {@link Validator#checkValue(java.lang.Object, org.ujorm.Key, org.ujorm.Ujo) checkValue(value ...)}
     * to ensure an integration.
     * <h3>Features:</h3>
     * <ul>
     *    <li>two Validators can be joined using operator AND/OR to a new composite Validator</li>
     *    <li>one (composite) Validator can be assigned into an object type of {@link Key}</li>
     *    <li>the Validator can be assigned to the {@link Key} to check all input data in the <strong>writing time</strong> always</li>
     * </ul>
     */
    public void keyValidator() {
        final Integer wrongValue = 3;
        final Integer minValue = 10;
        final Integer maxValue = 20;
        final Validator<Integer> validator = Validator.Build.range(minValue, maxValue);

        // check the wrong input value:
        ValidationError error = validator.validate(wrongValue, null, null);

        // get a localization message using a custom template:
        String expected = "My input 3 must be before 10 and 20 including";
        String template = "My input ${INPUT} must be before ${MIN} and ${MAX} including";
        String realMessage = error.getMessage(template);
        assert expected.equals(realMessage);

        // Composite validator:
        final Validator<Integer> compositeValidator = validator.and(Validator.Build.notNull(Integer.class));
        error = compositeValidator.validate(wrongValue, null, null);
        assert error!=null;
    }

   /** How to copy all attributes from a source to a target object? */
    public void copyAttributes() throws Exception {
        Employee source = getEmployee();
        Employee target = source.getClass().newInstance();

        for (Key<Ujo,?> key : source.readKeys()) {
            key.copy(source, target);
        }

        assert source.getId()
            == target.getId() : "Compare the same IDs";
    }

   /** How to copy some key values to another object? */
    public void copyAttributesByType() {
        Employee source = getEmployee();
        Employee target = new Employee();

        for (Key<Ujo,?> key : source.readKeys()) {
            if (key.isTypeOf(String.class)) {
                key.copy(source, target);
            }
            if (key.equals(Employee.WAGE)) { // The direct key have got an unique instance always
                key.copy(source, target);
            }
        }

        assert source.get(ID) != target.get(ID) : "Compare the IDs";
        assert source.get(NAME) == target.get(NAME) : "Compare the NAMEs";
    }

    /** Two related keys can be joined to the new {@link Key} instance by the method {@link Key#add(org.ujorm.Key)}.
     * New key can be used also to reading and writing values too.
     */
    public void compositeKey() {
        final Key<Employee,String> companyCity = COMPANY.add(CITY);

        Employee employee = getEmployee();
        final String city1, city2;

        city1 = employee.get(companyCity);         // If the Company is null then the result is null too.
        city2 = employee.getCompany().getCity(); // In this case the statement have got the same result

        assert (city1==city2) : "The same streets ";
        assert COMPANY.add(CITY).toString().equals("company.city")
                : "Check composite key name";
    }

    /** Setter with a composite key can create instances of a reference */
    public void compositeKeyAsFactory() {
        Employee employee = new Employee();
        employee.set(COMPANY.add(CITY), "Prague"); // method creates a new instance of Company

        assert employee.get(COMPANY).get(CITY) == "Prague"
                : "Check the value";
    }

    /** Employee validator example */
    public void criterionAsValidator() {
        Criterion<Employee> validator = Employee.WAGE.whereGt(100.0);
        try {
            validator.validate(getEmployee(), "Minimal WAGE is: %f.", validator.getRightNode());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Filter all employees, where a city name of a company equals employee name. */
    public void criterionAsFilter() {
        List<Employee> employees = COMPANY.add(CITY)
                .whereEq("Prague")
                .evaluate(getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.get(COMPANY.add(CITY)) + " " + employee.get(NAME));
        }
        System.out.println(employees.size());
    }

    /** Filter all employees, where a <strong>company.city</strong> equals to the <strong>employee's name</strong>.
     * Note: the result if Employee's name is 'Prague'. */
    public void criterionAsFilterWithKey() {
        List<Employee> employees = COMPANY.add(CITY)
                .whereEq(Employee.NAME) // The employee NAME can be a correct value of the Criterion
                .evaluate(getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.get(COMPANY.add(CITY)) + " " + employee.get(NAME));
        }

        assert employees.size() == 1 : "Check the result count";
        assert employees.get(0).get(NAME).equals("Prague") : "Check the result value";
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
