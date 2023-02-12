/*
 * Person.java
 *
 * Created on 19. October 2007, 20:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package samples.xtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import javax.xml.parsers.ParserConfigurationException;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.core.UjoTools;
import org.ujorm.extensions.AbstractUjo;
import org.xml.sax.SAXException;

public class Person extends AbstractUjo {

    private static final KeyFactory<Person> f = newFactory(Person.class);
    public static final Key<Person, String> NAME = f.newKey("Name");
    public static final Key<Person, Boolean> MALE = f.newKey("Male");
    public static final Key<Person, Integer> HEIGHT = f.newKey("Height");

    // Lock the Key factory
    static { f.lock(); }

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
        return UjoTools.equals(this, obj);
    }

    /** Run the class */
    public static void main(String[] args) {

        Person person = new Person();

// Writing:
        Person.NAME.setValue(person, "Pavel Ponec");
        Person.MALE.setValue(person, true);
        Person.HEIGHT.setValue(person, 183);

// Reading:
        String name = Person.NAME.of(person);
        Boolean male = Person.MALE.of(person);
        Integer height = Person.HEIGHT.of(person);
    }
}