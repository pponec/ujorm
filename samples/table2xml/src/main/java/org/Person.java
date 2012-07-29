package org;
import org.ujorm.*;

/** Person BO */
public class Person extends org.ujorm.implementation.map.MapUjoExt<Person> {

  public static final Key<Person,Integer> ID        = newKey("ID", Integer.class);
  public static final Key<Person,String>  FIRSTNAME = newKey("FirstName", String.class);
  public static final Key<Person,String>  SURNAME   = newKey("Surname", String.class);
  public static final Key<Person,Integer> AGE       = newKey("Age" , Integer.class);
  public static final Key<Person,Boolean> MALE      = newKey("Male", Boolean.class);
  public static final Key<Person,Double>  CASH      = newKey("Cash", 0d);

  public void addCash(double cash) {
    double newCash = get(CASH) + cash;
    set(CASH, newCash);
  }
}