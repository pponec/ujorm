package samples.patterDesing;

import java.util.List;

/*
 * GenericSorter.java
 *
 * Created on 5. listopad 2007, 16:59
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Pavel Ponec
 */

interface GenericSorter {
    void sort(List<MyMap> items, Property ... key);
}
