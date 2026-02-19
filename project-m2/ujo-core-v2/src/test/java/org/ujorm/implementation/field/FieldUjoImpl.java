/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.field;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.extensions.ValueAgent;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class FieldUjoImpl extends FieldUjo {
    
    private Long    p0;
    private Integer p1;
    private String  p2;
    private Date    p3;
    private Float   p4;

    /** (Long) */
    public static final Key<FieldUjoImpl,Long> PRO_P0 = newKey("P0", new ValueAgent<FieldUjoImpl,Long>() {
        public void writeValue(final FieldUjoImpl ujo, final Long value) { ujo.p0 = value; }
        public Long readValue (final FieldUjoImpl ujo) { return ujo.p0;  }
    });
    
    /** (Integer) */
    public static final Key<FieldUjoImpl,Integer> PRO_P1 = newKey("P1", new ValueAgent<FieldUjoImpl,Integer>() {
        public void writeValue(final FieldUjoImpl ujo, final Integer value) { ujo.p1 = value; }
        public Integer readValue (final FieldUjoImpl ujo) { return ujo.p1;  }
    });
    
    /** (String) */
    public static final Key<FieldUjoImpl,String> PRO_P2 = newKey("P2", new ValueAgent<FieldUjoImpl,String>() {
        public void writeValue(final FieldUjoImpl ujo, final String value) { ujo.p2 = value; }
        public String readValue (final FieldUjoImpl ujo) { return ujo.p2;  }
    });
    
    /** (Date) */
    public static final Key<FieldUjoImpl,Date> PRO_P3 = newKey("P3", new ValueAgent<FieldUjoImpl,Date>() {
        public void writeValue(final FieldUjoImpl ujo, final Date value) { ujo.p3 = value; }
        public Date readValue (final FieldUjoImpl ujo) { return ujo.p3;  }
    });
    
    /** (Float) */
    public static final Key<FieldUjoImpl,Float> PRO_P4 = newKey("P4", new ValueAgent<FieldUjoImpl,Float>() {
        public void writeValue(final FieldUjoImpl ujo, final Float value) { ujo.p4 = value; }
        public Float readValue (final FieldUjoImpl ujo) { return ujo.p4;  }
    });

    static {
        init(FieldUjoImpl.class);
    }
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public FieldUjoImpl() {
    }
    
}
