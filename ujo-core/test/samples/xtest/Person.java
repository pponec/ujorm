/*
 * Person.java
 *
 * Created on 19. øíjen 2007, 20:40
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
import org.ujoframework.Ujo;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.UjoManagerXML;
import org.ujoframework.implementation.map.*;
import org.xml.sax.SAXException;
public class Person extends MapUjo {
    
    public static final MapProperty<Person,String>  NAME   = newProperty("Name" , String.class);
    public static final MapProperty<Person,Boolean> MALE   = newProperty("Male" , Boolean.class);
    public static final MapProperty<Person,Integer> HEIGHT = newProperty("Height", Integer.class);
    
    
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
        Person.NAME.setValue(person, "Paul Ponec");
        Person.MALE.setValue(person, true);
        Person.HEIGHT.setValue(person, 183);
        
// Reading:
        String  name   = Person.NAME.getValue(person);
        Boolean male   = Person.MALE.getValue(person);
        Integer height = Person.HEIGHT.getValue(person);
    }
    
}