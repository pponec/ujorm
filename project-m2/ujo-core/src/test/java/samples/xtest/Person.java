/*
 * Person.java
 *
 * Created on 19. October 2007, 20:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.xtest;





/* --- */
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import javax.xml.parsers.ParserConfigurationException;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.implementation.map.*;
import org.xml.sax.SAXException;
public class Person extends MapUjo {
    
    public static final Key<Person,String>  NAME   = newKey("Name");
    public static final Key<Person,Boolean> MALE   = newKey("Male");
    public static final Key<Person,Integer> HEIGHT = newKey("Height");
    
    
    public void testExport() throws IOException, ParserConfigurationException, SAXException {
        
        Person person = this;
        
// Make Serialization:
        Writer writer = null;
        UjoManagerXML.getInstance().saveXML(writer, person, null, "My Export");
        
  // Make Deserialization:
        InputStream inputStream = null;
        person = UjoManagerXML.getInstance().parseXML(inputStream, Person.class, "My Import");
    }
    
    
    
    public boolean equals(Object obj) {
        return UjoManager.getInstance().equals(this, (Ujo) obj );
    }
    
    
    
    
    
    
    
    
    /** Run the class */
    public static void main(String[] args) {
        
        Person person = new Person();
        
// Writing:
        Person.NAME.setValue(person, "Pavel Ponec");
        Person.MALE.setValue(person, true);
        Person.HEIGHT.setValue(person, 183);
        
// Reading:
        String  name   = Person.NAME.getValue(person);
        Boolean male   = Person.MALE.getValue(person);
        Integer height = Person.HEIGHT.getValue(person);
    }
    
}