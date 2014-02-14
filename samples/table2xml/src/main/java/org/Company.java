package org;
import org.ujorm.*;
import org.ujorm.implementation.quick.SmartUjo;

/** Company BO */
public class Company extends SmartUjo<Company> {

  public static final Key<Company,String> NAME = newKey("Name");
  public static final ListKey<Company,Person> PERSONS = newListKey("Person");

  public Company() {
    set(NAME, "My Company"); // assign a default company name
  }
}