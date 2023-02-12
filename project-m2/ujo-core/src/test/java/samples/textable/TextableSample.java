/*
 * TextableSample.java
 *
 * Created on 25. srpen 2007, 15:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package samples.textable;

/**
 * @author Pavel Ponec
 */
public class TextableSample {

    /** Creates a new instance of TextableSample */
    public TextableSample() {
    }

    /** Run the class */
    public static void main(String[] args) {

        Integer textable1 = Integer.valueOf(7);
        Integer textable2 = Integer.valueOf(textable1.toString());
        boolean result = textable1.equals(textable2);


    }
}
