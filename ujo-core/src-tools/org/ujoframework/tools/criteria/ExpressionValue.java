/*
 *  Copyright 2007-2008 Paul Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */   
   


package org.ujoframework.tools.criteria;

import java.util.regex.Pattern;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * Expression
 * @author pavel
 */
public class ExpressionValue<UJO extends Ujo> extends Expression<UJO> {
    
    final private UjoProperty property;
    final private Operator    operator;
    final private Object      value;
    
    public ExpressionValue(UjoProperty<UJO,? extends Object> property, Operator operator, UjoProperty<UJO,Object> value) {
        this(property, operator, (Object) value);    
    }
    
    public ExpressionValue(UjoProperty<UJO,? extends Object> property, Operator operator, Object value) {
        this.property = property;
        this.value    = value;
        this.operator = operator;
    }

    @SuppressWarnings("unchecked")
    public boolean evaluate(UJO ujo) {
        Object value2 = value instanceof UjoProperty ? ((UjoProperty)value).getValue(ujo) : value;
        
        switch (operator) {
            case EQ:
            case NOT_EQ:
                boolean result = property.equals(ujo, value2);
                return operator==Operator.EQ ? result : !result ;
            case REGEXP:
            case NOT_REGEXP:
                Pattern p = value2 instanceof Pattern 
                    ? (Pattern) value2 
                    : Pattern.compile(value2.toString())
                    ;
                Object val1 = property.getValue(ujo);
                boolean result2 = val1!=null && p.matcher(val1.toString()).matches();
                return operator==Operator.REGEXP ? result2 : !result2 ;
        }
        
        Comparable val2 = (Comparable) value2;
        if (null== val2)  {return false; }
        Comparable val1 = (Comparable) property.getValue(ujo);
        if (null== val1)  {return false; }
        int result = compare(val1, val2);
        
        switch (operator) {
            case LT: return result< 0;       
            case LE: return result<=0;       
            case GT: return result> 0;       
            case GE: return result>=0;       
        }
        
        throw new IllegalArgumentException("Illegal operator: " + operator);
    }

    /** Compare two object */
    @SuppressWarnings("unchecked")
    protected int compare(Comparable o1, Comparable o2) {
        if (o1==o2) {
            return 0;
        }
        if (o1==null) {
            return +1;
        }
        if (o2==null) {
            return -1;
        }
        return o1.compareTo(o2);
    }
        

    @Override
    public String toString() {
        return property + " " + operator.name() + " " + value;
    }
    

}
