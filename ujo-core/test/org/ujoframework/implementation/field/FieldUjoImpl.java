/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.field;

import java.util.Date;
import org.ujoframework.extensions.ValueAgent;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class FieldUjoImpl extends FieldUjo {
    
    private Long    p0;
    private Integer p1;
    private String  p2;
    private Date    p3;
    private Float   p4;

    /** (Long) */
    public static final FieldProperty<FieldUjoImpl,Long> PRO_P0 = newProperty("P0", Long.class, new ValueAgent<FieldUjoImpl,Long>() {
        public final void writeValue(final FieldUjoImpl ujo, final Long value) { ujo.p0 = value; }
        public final Long readValue (final FieldUjoImpl ujo) { return ujo.p0;  }
    });
    
    /** (Integer) */
    public static final FieldProperty<FieldUjoImpl,Integer> PRO_P1 = newProperty("P1", Integer.class, new ValueAgent<FieldUjoImpl,Integer>() {
        public final void writeValue(final FieldUjoImpl ujo, final Integer value) { ujo.p1 = value; }
        public final Integer readValue (final FieldUjoImpl ujo) { return ujo.p1;  }
    });
    
    /** (String) */
    public static final FieldProperty<FieldUjoImpl,String> PRO_P2 = newProperty("P2", String.class, new ValueAgent<FieldUjoImpl,String>() {
        public final void writeValue(final FieldUjoImpl ujo, final String value) { ujo.p2 = value; }
        public final String readValue (final FieldUjoImpl ujo) { return ujo.p2;  }
    });
    
    /** (Date) */
    public static final FieldProperty<FieldUjoImpl,Date> PRO_P3 = newProperty("P3", Date.class, new ValueAgent<FieldUjoImpl,Date>() {
        public final void writeValue(final FieldUjoImpl ujo, final Date value) { ujo.p3 = value; }
        public final Date readValue (final FieldUjoImpl ujo) { return ujo.p3;  }
    });
    
    /** (Float) */
    public static final FieldProperty<FieldUjoImpl,Float> PRO_P4 = newProperty("P4", Float.class, new ValueAgent<FieldUjoImpl,Float>() {
        public final void writeValue(final FieldUjoImpl ujo, final Float value) { ujo.p4 = value; }
        public final Float readValue (final FieldUjoImpl ujo) { return ujo.p4;  }
    });

    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public FieldUjoImpl() {
    }
    
}
