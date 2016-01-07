/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xmlSpeed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.ujorm.implementation.pojo.PojoImplChild;

/**
 * A POJO Object
 * @author Pavel Ponec
 */
@javax.xml.bind.annotation.XmlRootElement @javax.xml.bind.annotation.XmlType  /*JAXB*/
public class PojoTree 
    extends PojoImplChild 
    implements Serializable 
//             , javolution.xml.XMLSerializable  /*Javolution*/

{
    
    /** List<PojoTree> */
    private ArrayList<PojoTree> children = new ArrayList<PojoTree>();
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public PojoTree() {
    }

    public ArrayList<PojoTree> getChilds() {
        return children;
    }

    public void setChilds(ArrayList<PojoTree> children) {
        this.children = children;
    }

    public List<PojoTree> addChild(PojoTree child) {
        getChilds().add(child);
        return getChilds();
    }
    
    public int size() {
        int result = 0;
        ArrayList<PojoTree> children = getChilds();      
        
        if (children!=null) for (PojoTree tree : children) {
            result += tree.size() + 1;
        }
        return result;
    }    
    
    // * * * * * * * * * * *
    
    public void init(ZCounter counter, int deep) {
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456f);
        
        this.setP0(o0);
        this.setP1(o1);
        this.setP2(o2);
        this.setP3(o3);
        this.setP4(o4);
        this.setP5(o0);
        this.setP6(o1);
        this.setP7(o2);
        this.setP8(o3);
        this.setP9(o4);
        
        for (int i=0; i<10; i++) {
            if (deep<=0 || counter.substract()){
                return;
            }
            PojoTree item = new PojoTree();
            item.init(counter, deep-1);
            this.addChild(item);
        }
        
    }

    
    /*Javolution*/
//     protected static final javolution.xml.XMLFormat<PojoTree> 
//           PERSON_XML = new javolution.xml.XMLFormat<PojoTree>(PojoTree.class) {
//
//        @Override
//        public void write(PojoTree person, OutputElement xml) 
//            throws javolution.xml.stream.XMLStreamException {
//            xml.setAttribute("p0", person.p0);
//            xml.setAttribute("p1", person.p1);
//            xml.setAttribute("p2", person.p2);
//            //xml.setAttribute("p3", person.p3); // Sorry, this I do not know.
//            xml.setAttribute("p4", person.p4);
//            xml.setAttribute("p5", person.p5);
//            xml.setAttribute("p6", person.p6);
//            xml.setAttribute("p7", person.p7);
//            //xml.setAttribute("p8", person.p8); // Sorry, this I do not know.
//            xml.setAttribute("p9", person.p9);
//            xml.add(person.children, "Person");
//        }
//
//        @Override
//        public void read(InputElement xml, PojoTree person) 
//            throws javolution.xml.stream.XMLStreamException {
//                    person.p0 = xml.getAttribute("p0", new Long(0));
//                    person.p1 = xml.getAttribute("p1", 0);
//                    person.p2 = xml.getAttribute("p2", "");
//                    //person.p3 = xml.getAttribute("p3", new Date()); // Sorry, this I do not know.
//                    person.p4 = xml.getAttribute("p4", 0.0f);
//                    person.p5 = xml.getAttribute("p5", new Long(0));
//                    person.p6 = xml.getAttribute("p6", 0);
//                    person.p7 = xml.getAttribute("p7", "");
//                    //person.p8 = xml.getAttribute("p8", new Date()); // Sorry, this I do not know.
//                    person.p9 = xml.getAttribute("p9", 0.0f);
//
//                    person.children = xml.get( "Person" );
//        }
//
//    };
    /**/
    
    
}
