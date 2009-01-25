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

import org.ujoframework.Ujo;

/**
 * Expression
 * @author pavel
 */
final public class ExpressionNot<UJO extends Ujo> extends Expression<UJO> {
    
    final private Expression<UJO> expression;
    
    public ExpressionNot(Expression<UJO> expression) {
        this.expression = expression;  
    }
    
    public boolean evaluate(UJO ujo) {
        return !expression.evaluate(ujo);
    }
        
    @Override
    public String toString() {
        return "!(" + expression + ")";
    }
    
    
}
