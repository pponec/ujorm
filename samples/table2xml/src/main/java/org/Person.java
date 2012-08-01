package org;
import org.ujorm.*;
import org.ujorm.implementation.quick.QuickUjoMid;

/** Person BO */
public class Person extends QuickUjoMid<Person> {

  public static final Key<Person,Integer> ID        = newKey("ID");
  public static final Key<Person,String>  FIRSTNAME = newKey("FirstName");
  public static final Key<Person,String>  SURNAME   = newKey("Surname");
  public static final Key<Person,Integer> AGE       = newKey("Age");
  public static final Key<Person,Boolean> MALE      = newKey("Male");
  public static final Key<Person,Double>  CASH      = newKey("Cash");

  public void addCash(double cash) {
    double newCash = get(CASH) + cash;
    set(CASH, newCash);
  }
}