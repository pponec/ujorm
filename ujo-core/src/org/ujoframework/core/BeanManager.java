/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoPropertyList;

/**
 * Manager of <code>BeanUjo</code>
 * @author Pavel Ponec
 */
public class BeanManager<UJO,VALUE> {
    
    private final UjoProperty property;
    
    /** An empty array of classes. */
    private Method setter;
    private Method getter;    
    
    public BeanManager(UjoProperty property) {
        this.property = property;
    }
    
    /** WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(final UJO bean, final VALUE value) throws IllegalArgumentException {
        try {
            getMethod(bean, true).invoke(bean, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("BeanProperty:"+property.getName()+"="+value, e);
        }
    }
    
    /** WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public Object readValue(final UJO bean) throws IllegalArgumentException {
        try {
            return getMethod(bean, false).invoke(bean);
        } catch (Exception e) {
            throw new IllegalArgumentException("BeanProperty:"+property.getName(), e);
        }
    }
    
    
    /**
     * Get or create a setter or getter.
     * @param ujo UJO object.
     * @param set The result method can be a setter (TRUE) or a getter (FALSE).
     * @throws java.lang.IllegalArgumentException Method can't find usable method.
     */
    public Method getMethod(final UJO ujo, final boolean set) throws IllegalArgumentException {
        Method result = set ? setter : getter ;
        if (result==null) {
            String methodName = getMethodName(set);
            Exception ex = null ;
            try {
                Class ujoClass = ujo.getClass();
                Class type = set ? property.getType() : null;
                
                result = getMethodPlain(ujoClass, type, methodName);
                if (result==null) {
                    if (UjoPropertyList.class.isInstance(property) && !methodName.endsWith("s")) {
                        // TAG name is a singular:
                        result = getMethodPlain(ujoClass, type, methodName+'s');
                    } else if ((type=getPrimitive(type))!=null) {
                        // Try a primitive type:
                        result = getMethodPlain(ujoClass, type, methodName);
                    }
                }
                if (set) {
                    setter = result;
                } else {
                    getter = result;
                }
            } catch (SecurityException e) {
                ex = e;
            }
            if (result==null) {
                throw new IllegalArgumentException("Can't find method: " + methodName+'('+property.getType().getName()+')', ex);
            }
        }
        return result;
    }
    
    /** Returns a primitive type if can or a null value. */
    protected Class getPrimitive(Class objClass) {
        
        if (objClass!=null && !objClass.isPrimitive()) try {
            Field field = objClass.getField("TYPE");
            if (field.getType()==Class.class) {
                final Object result = field.get(null);
                return (Class) result;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Method getMethodPlain
    ( final Class ujoClass
    , final Class type
    , final String methodName
    ) throws SecurityException {
        
        try {
            final Class[] types = type!=null ? new Class[]{type} : null ;
            final Method result = ujoClass.getMethod(methodName, types);
            return result;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    
    /** Returns a method name by a name */
    protected String getMethodName(final boolean set) {
        String result = (set ? "set" : Boolean.class.equals(property.getType()) ? "is" : "get")
        + Character.toUpperCase(property.getName().charAt(0))
        + property.getName().substring(1)
        ;
        return result;
    }    
    
    /** Create new instance of BeanManager */
    public static <UJO,VALUE> BeanManager<UJO,VALUE> getInstance(UjoProperty property) {
        return new BeanManager<UJO,VALUE>(property);
    }

    @Override
    public String toString() {
        return property.toString();
    }

    
}
