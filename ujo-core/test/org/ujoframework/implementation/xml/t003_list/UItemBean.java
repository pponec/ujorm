/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t003_list;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.implementation.map.MapProperty;


/**
 * An UnifiedDataObject Imlpementation
 * @author pavel
 */
public class UItemBean extends MapUjo  {
    
    public static final MapProperty<UItemBean,Boolean>    P0_BOOL     = newProperty("Boolean", Boolean.class);
    public static final MapProperty<UItemBean,Byte>       P1_BYTE     = newProperty("Byte", Byte.class);
    public static final MapProperty<UItemBean,Character>  P2_CHAR     = newProperty("Character", Character.class);
    public static final MapProperty<UItemBean,Short>      P3_SHORT    = newProperty("Short", Short.class);
    public static final MapProperty<UItemBean,Integer>    P4_INTE     = newProperty("Integer", Integer.class);
    public static final MapProperty<UItemBean,Long>       P5_LONG     = newProperty("Long", Long.class);
    public static final MapProperty<UItemBean,Float>      P6_FLOAT    = newProperty("Float", Float.class);
    public static final MapProperty<UItemBean,Double>     P7_DOUBLE   = newProperty("Double", Double.class);
    public static final MapProperty<UItemBean,BigInteger> P8_BIG_INT  = newProperty("BigInteger", BigInteger.class);
    public static final MapProperty<UItemBean,BigDecimal> P9_BIG_DECI = newProperty("BigDecimal", BigDecimal.class);
    public static final MapProperty<UItemBean,Date>       PD_DATE     = newProperty("Date", Date.class);
    // Some Arrays
    public static final MapProperty<UItemBean,byte[]>     PA_BYTES    = newProperty("bytes", byte[].class);
    public static final MapProperty<UItemBean,char[]>     PB_CHARS    = newProperty("chars", char[].class);
    
    
}
