/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t003_list;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.implementation.quick.QuickUjo;


/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class UItemBean extends QuickUjo  {
    
    public static final Key<UItemBean,Boolean>    P0_BOOL     = newKey("Boolean");
    public static final Key<UItemBean,Byte>       P1_BYTE     = newKey("Byte");
    public static final Key<UItemBean,Character>  P2_CHAR     = newKey("Character");
    public static final Key<UItemBean,Short>      P3_SHORT    = newKey("Short");
    public static final Key<UItemBean,Integer>    P4_INTE     = newKey("Integer");
    public static final Key<UItemBean,Long>       P5_LONG     = newKey("Long");
    public static final Key<UItemBean,Float>      P6_FLOAT    = newKey("Float");
    public static final Key<UItemBean,Double>     P7_DOUBLE   = newKey("Double");
    public static final Key<UItemBean,BigInteger> P8_BIG_INT  = newKey("BigInteger");
    public static final Key<UItemBean,BigDecimal> P9_BIG_DECI = newKey("BigDecimal");
    public static final Key<UItemBean,Date>       PD_DATE     = newKey("Date");
    // Some Arrays
    public static final Key<UItemBean,byte[]>     PA_BYTES    = newProperty("bytes", byte[].class); // TODO.pop
    public static final Key<UItemBean,char[]>     PB_CHARS    = newProperty("chars", char[].class); // TODO.pop
    
    /** Vrací Long */
    public Long getLong() {
        return P5_LONG.of(this);
    }

}
