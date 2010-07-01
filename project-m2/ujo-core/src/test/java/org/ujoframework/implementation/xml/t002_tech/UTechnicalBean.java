/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t002_tech;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.map.MapUjo;
import static org.ujoframework.implementation.xml.t002_tech.ZeroProvider.*;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UTechnicalBean extends MapUjo  {
    
    public static final Property<UTechnicalBean,Boolean>    P0_BOOL     = newProperty("Boolean", ZERO_BOOL);
    public static final Property<UTechnicalBean,Byte>       P1_BYTE     = newProperty("Byte", ZERO_BYTE);
    public static final Property<UTechnicalBean,Character>  P2_CHAR     = newProperty("Character", ZERO_CHAR);
    public static final Property<UTechnicalBean,Short>      P3_SHORT    = newProperty("Short", ZERO_SHORT);
    public static final Property<UTechnicalBean,Integer>    P4_INTE     = newProperty("Integer", ZERO_INT);
    public static final Property<UTechnicalBean,Long>       P5_LONG     = newProperty("Long", ZERO_LONG);
    public static final Property<UTechnicalBean,Float>      P6_FLOAT    = newProperty("Float", 0f);
    public static final Property<UTechnicalBean,Double>     P7_DOUBLE   = newProperty("Double", 0d);
    public static final Property<UTechnicalBean,BigInteger> P8_BIG_INT  = newProperty("BigInteger", ZERO_BIG_INT);
    public static final Property<UTechnicalBean,BigDecimal> P9_BIG_DECI = newProperty("BigDecimal", ZERO_BIG_DECI);
    public static final Property<UTechnicalBean,Date>       PD_DATE     = newProperty("Date", Date.class);
    // Some Arrays
    public static final Property<UTechnicalBean,byte[]>     PA_BYTES    = newProperty("bytes", byte[].class);
    public static final Property<UTechnicalBean,char[]>     PB_CHARS    = newProperty("chars", char[].class);
    
    
    // public static final Boolean    ZERO_BOOL     = Boolean.FALSE;
    // public static final Byte       ZERO_BYTE     = new Byte((byte)0);
    // public static final Character  ZERO_CHAR     = new Character((char)0);
    // public static final Short      ZERO_SHORT    = new Short((short)0);
    // public static final Integer    ZERO_INT      = new Integer(0);
    // public static final Long       ZERO_LONG     = new Long(0);
    // public static final Float      ZERO_FLOAT    = new Float(0);
    // public static final Double     ZERO_DOUBLE   = new Double(0);
    // public static final BigInteger ZERO_BIG_INT  = BigInteger.ZERO;
    // public static final BigDecimal ZERO_BIG_DECI = BigDecimal.valueOf(0);
    // /* Some Arrays */
    // public static final byte[]     ZERO_BYTES    = new byte[0];
    // public static final char[]     ZERO_CHARS    = new char[0];
    
    
}
