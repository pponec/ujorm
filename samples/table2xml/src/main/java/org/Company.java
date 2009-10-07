package org;
import org.ujoframework.*;
import org.ujoframework.extensions.*;

/** Company BO */
public class Company extends org.ujoframework.implementation.map.MapUjoExt<Company> {

  public static final UjoProperty<Company,String>     NAME = newProperty   ("Name" , String.class);
  public static final ListProperty<Company,Person> PERSONS = newListProperty("Person", Person.class);

  public Company() {
    set(NAME, "My Company"); // assign a default company name
  }
}