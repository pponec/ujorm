/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samples.criteria;

import java.util.Arrays;
import java.util.List;
import org.ujoframework.core.UjoComparator;
import org.ujoframework.tools.UjoCriteria;
import org.ujoframework.tools.criteria.Expression;
import static samples.criteria.Person.*;

/**
 *
 * @author pavel
 */
public class CriteriaSample {
    
    public void sample() {
        
    Person child  = new Person("Pavel", 140.0);
    Person mother = new Person("Mary", 150.0);
    Person father = new Person("John", 160.0);

    child.set(MOTHER, mother);
    child.set(FATHER, father);

    List<Person> persons = Arrays.asList(child, mother, father);
    //

    UjoCriteria<Person> criteria = UjoCriteria.create();
    Expression<Person> exp = criteria.newExpr(NAME, "John");
    UjoComparator     sort = UjoComparator.create(true, NAME);
    List<Person>    result = criteria.select(persons, exp, sort);
        
        
        
        
        
   
        
        
        
    }

}
