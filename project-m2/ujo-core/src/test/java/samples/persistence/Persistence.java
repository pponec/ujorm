/*
 * Persistence.java
 *
 * Created on 9. May 2008, 21:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.ujorm.core.UjoManagerCSV;
import org.ujorm.core.UjoManagerRBundle;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.core.XmlHeader;
import samples.map.Person;

/**
 *
 * @author Pavel Ponec
 */
public class Persistence {
    
    /** Creates a new instance of Persistence */
    public Persistence() {
    }
    
    void showXML() throws Exception {
        
        Person person = new Person(); // Set attributes ...
        
        // Save XML:
        XmlHeader defaultXmlHeader = new XmlHeader();
        UjoManagerXML.getInstance().saveXML(new File("file.xml"), person, defaultXmlHeader, "SaveContext");
        
        // Load XML:
        person = UjoManagerXML.getInstance().parseXML(new File("file.xml"), Person.class, "LoadContext");
    }
    
    void showCSV() throws Exception {
        
        List<Person> people = new ArrayList<>(0);
        UjoManagerCSV<Person> manager = UjoManagerCSV.of(Person.class);
        
        // Save CSV:
        manager.saveCSV(new File("file.csv"), people, "SaveContext");
        
        // Load CSV:
        people = manager.loadCSV(new File("file.csv"), "LoadContext");
        
    }
    
    void showRBundle() throws Exception  {
        
        Person person = new Person(); // Set attributes ...
        UjoManagerRBundle<Person> manager = UjoManagerRBundle.of(Person.class);
        
        // Save CSV:
        String header = "Header Description";
        manager.saveResourceBundle(new File("file.properties"), person, header, "SaveContext");
        
        // Load CSV:
        person = manager.loadResourceBundle(new File("file.properties"), true, "LoadContext");
        
    }
    
    
}
