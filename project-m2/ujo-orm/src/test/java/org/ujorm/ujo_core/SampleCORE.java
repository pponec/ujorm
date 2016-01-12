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

package org.ujorm.ujo_core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.KeyRing;
import org.ujorm.core.UjoComparator;
import org.ujorm.core.UjoManagerCSV;
import org.ujorm.criterion.*;
import org.ujorm.validator.ValidationException;
import static org.ujorm.core.UjoTools.SPACE;
import static org.ujorm.ujo_core.Company.CITY;
import static org.ujorm.ujo_core.Employee.*;

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
            sample.readWriteBean();
            sample.readWriteUjo();
            sample.defaultValues();
            sample.numericDefaultValues();
            sample.copyAttributes();
            sample.keyValidator();
            sample.localizedMessageOfValidator();
            sample.compositeKey();
            sample.theCriterion();
            sample.criterionAsFilter();
            sample.criterionAsFilterWithKey();
            sample.sortEmployeeList();
            sample.keySerialization();
            sample.importCSV();

        } catch (Exception e) {
            Logger.getLogger(SampleCORE.class.getName()).log(Level.SEVERE, "Sample CORE", e);
            throw new IllegalStateException("Sample CORE", e);
        }
    }

    // ======= CHAPTERS: =======

    /** See a common data access for the JavaBean object */
    public void readWriteBean() {
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

        assert id == 7L;
        assert name == "Pavel";
        assert wage == 20.00;
        assert company != null;
    }

    /** See a data access using API of the {@link Ujo} object */
    public void readWriteUjo() {
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

        assert id == 7L;
        assert name == "Pavel";
        assert wage == 20.00;
        assert company != null;

        // === Code sample where the compiler fails: ===
        // person.set(Company.ID, 7L);  // Key from another domain is not allowed
        // person.set(Employee.ID, "Pavel");    // Wrong data type of the argument value
        // String id = person.get(Employee.ID); // Wrong return class
    }

    /** How to restore default values? */
    @SuppressWarnings("unchecked")
    public void defaultValues() {
        Employee employee = service.getEmployee();

        for (Key key : employee.readKeys()) {
            employee.set(key, null);
        }

        assert employee.getWage() == 0.0 : "Default value is zero";
        assert employee.getWage() == WAGE.getDefault() : "Check the default value";
    }

    /** See how to restore a default value for all Numbers only.
     * A type of the Key can be checked by the method {@link Key#isTypeOf(java.lang.Class) }.
     */
    @SuppressWarnings("unchecked")
    public void numericDefaultValues() {
        Employee employee = service.getEmployee();

        for (Key key : employee.readKeys()) {
            if (key.isTypeOf(Number.class)) {
                employee.set(key, null);
            }
        }
        assert employee.getWage() == WAGE.getDefault() : "Check the default value";
    }

    /** How to copy all attributes from a source to a target object? */
    public void copyAttributes() throws Exception {
        Employee source = service.getEmployee();
        Employee target = source.getClass().newInstance();

        for (Key<Employee,?> key : source.readKeyList()) {
            key.copy(source, target);
        }

        assert source.getId() == target.getId()
                : "The same IDs";
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
     * The {@link Employee#NAME} has got allowed max 7 characters length.
     * <br/>The key definition in the {@link Employee} class is:
     * <pre>{@code public static final Key<Employee, String> NAME = factory.newKey(length(7));
     *  }</pre>
     */
    public void keyValidator() {
        final String correctName = "1234567";
        final String wrongName = "12345678";

        Employee employee = new Employee();
        employee.set(NAME, correctName);
        try {
            employee.set(NAME, wrongName);
        } catch (ValidationException e) {
            String expected
                    = "Length of Employee.name must be between 0 and 7, "
                    + "but the input has 8 characters";
            assert e.getMessage().equals(expected);
        }
        assert employee.getName() == correctName;
    }

    /** Messages from validator can be located using the templates */
    public void localizedMessageOfValidator() {
        final String wrongName = "12345678";
        ValidationException exception = null;

        try {
            new Employee().set(NAME, wrongName);
        } catch (ValidationException e) {
            exception = e;
        }

        String template = "The name can be up to ${MAX} characters long, not ${LENGTH}.";
        String expected = "The name can be up to 7 characters long, not 8.";
        String result = exception.getError().getMessage(template);
        assert expected.equals(result);
    }

    /** Two related keys can be joined to the new {@link Key} instance by the method {@link Key#add(org.ujorm.Key)}.
     * A CompositeKey can be used to reading and writing attributes of related domain objects,
     * however the setter with the CompositeKey <create>creates</create> missing domain relations automatically.
     */
    public void compositeKey() {
        CompositeKey<Employee, String> companyNameKey = COMPANY.add(Company.NAME);

        Employee employee = new Employee();
        String companyName = employee.get(companyNameKey); //!
        assert companyName == null;

        employee.set(companyNameKey, "Prague"); //!
        companyName = employee.get(companyNameKey);

        assert employee.getCompany() != null;
        assert companyName == "Prague";
    }

    /** Employee theCriterion example */
    public void theCriterion() {
        Criterion<Employee> validator = Employee.WAGE.whereGt(100.0);
        try {
            validator.validate(service.getEmployee()
                    , "Minimal WAGE is: %s units"
                    , validator.getRightNode());
            assert false : Employee.WAGE + " is not valid";
        } catch (IllegalArgumentException e) {
            assert e.getMessage() != null;
        }
    }

    /** Filter all employees, where a city name of a company equals employee name. */
    public void criterionAsFilter() {
        List<Employee> employees = COMPANY.add(CITY)
                .whereEq("Prague")
                .evaluate(service.getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.getCompany().getCity() + SPACE + employee.getName());
        }
        assert employees.size() == 4;
    }

    /** Filter all employees, where a <strong>company.city</strong> equals to the <strong>employee's name</strong>.
     * Note: the result if Employee's name is 'Prague'. */
    public void criterionAsFilterWithKey() {
        List<Employee> employees = COMPANY.add(CITY)
                .whereEq(Employee.NAME) // The employee NAME can be a correct value of the Criterion
                .evaluate(service.getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.getCompany().getCity() + SPACE + employee.getName());
        }

        assert employees.size() == 1 : "Check the result count";
        assert employees.get(0).getName().equals("Prague") : "Check the result value";
    }

    /** How to sort the List?  */
    public void sortEmployeeList() {
        List<Employee> employees = UjoComparator.of
                ( COMPANY.add(CITY)
                , NAME.descending()
                ).sort(service.getEmployees());

        for (Employee employee : employees) {
            System.out.println(employee.getCompany().getCity() + SPACE + employee.getName());
        }

        assert employees.size() == 4 : "Check the result count";
    }

    /** Each direct Key has an unique instance in a class-loader,
     * similar like an item of the {@link Enum} type.
     * For the serialization use a {@link KeyRing} envelope.
     */
    public void keySerialization() {
        final KeyRing<Employee> keyRing1, keyRing2;
        keyRing1 = KeyRing.of(Employee.ID, Employee.COMPANY.add(Company.NAME));
        keyRing2 = service.serialize(keyRing1);

        assert keyRing1 != keyRing2 : "Different instances";
        assert keyRing1.get(0) == keyRing2.get(0) : "The same direct keys";
        assert keyRing1.get(1).equals(keyRing2.get(1)) : "The equal composite keys";
        assert new Employee().readKeys() instanceof KeyRing : "readKeys() returns the KeyRing";
    }

    /** Import the CSV file using a Composite Keys from the file content:
     * <pre>{@code id;name;companyId
     * 1;Pavel;10
     * 2;Petr;30
     * 3;Kamil;50}</pre>
     */
    public void importCSV() throws Exception {
        Scanner scanner = service.getCsvData();
        UjoManagerCSV<Employee> manager = UjoManagerCSV.of
                ( Employee.ID
                , Employee.NAME
                , Employee.COMPANY.add(Company.ID)
                );
        List<Employee> employes = manager.loadCSV(scanner, this);

        assert employes.size() == 3;
        assert employes.get(0).getId() .equals(1L);
        assert employes.get(0).getName().equals("Pavel");
        assert employes.get(0).getCompany().getId().equals(10L);
    }

    // ===========- HELPER CODE ===========-

    /** Helper methods */
    private final Service service = new Service();

    /** Helper methods */
    private static class Service {

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

        /** Object serialization */
        @SuppressWarnings("unchecked")
        private <T extends Serializable> T serialize(T object) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream(1000);
                ObjectOutputStream encoder = new ObjectOutputStream(os);
                encoder.writeObject(object);
                encoder.close();

                InputStream is = new ByteArrayInputStream(os.toByteArray());
                ObjectInputStream decoder = new ObjectInputStream(is);
                Object result = (Serializable) decoder.readObject();
                decoder.close();

                return (T) result;
            } catch (Exception e) {
                throw new IllegalStateException("Serializaton error", e);
            }
        }

        /** CSV data */
        private Scanner getCsvData() {
            return new Scanner(" id;name;companyId"
                    + "\n1;Pavel;10"
                    + "\n2;Petr;30"
                    + "\n3;Kamil;50");
        }
    }
}
