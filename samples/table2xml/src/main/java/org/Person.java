package org;
import org.ujorm.*;

/** Person BO */
public class Person extends org.ujorm.implementation.map.MapUjoExt<Person> {

  public static final Key<Person,Integer> ID        = newProperty("ID", Integer.class);
  public static final Key<Person,String>  FIRSTNAME = newProperty("FirstName", String.class);
  public static final Key<Person,String>  SURNAME   = newProperty("Surname", String.class);
  public static final Key<Person,Integer> AGE       = newProperty("Age" , Integer.class);
  public static final Key<Person,Boolean> MALE      = newProperty("Male", Boolean.class);
  public static final Key<Person,Double>  CASH      = newProperty("Cash", 0d);

  public void addCash(double cash) {
    double newCash = get(CASH) + cash;
    set(CASH, newCash);
  }
}