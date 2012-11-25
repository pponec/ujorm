/*
 *  Copyright 2012 Pavel Ponec
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
package com.ujorm.UjoCodeGenerator;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.List;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * String service
 * @author Pavel Ponec
 */
final public class StringService {
    
    /**
     * Returns variable getter name.
     *
     * @param variable
     * @return
     */
    public String getGetterName(VariableTree variable) {
        assert variable != null : "Variable cannot be null";
        return getGetterName(variable.getName().toString());
    }

    /**
     * Returns variable getter name.
     *
     * @param variable
     * @return
     */
    protected String getGetterName(String variable) {
        assert variable != null : "Variable cannot be null";
        return getVariableName("get", variable);
    }

    /**
     * Returns variable setter name.
     *
     * @param variable
     * @return
     */
    public String getSetterName(VariableTree variable) {
        assert variable != null : "Variable cannot be null";
        return getSetterName(variable.getName().toString());
    }
    
    /**
     * Returns variable setter name.
     *
     * @param variable
     * @return
     */
    protected String getSetterName(String variable) {
        assert variable != null : "Variable cannot be null";
        return getVariableName("set", variable);
    }
    
    /**
     * Returns variable setter name.
     *
     * @param variable
     * @return
     */
    public String getParameterName(VariableTree variable) {
        assert variable != null : "Variable cannot be null";
        return getParameterName(variable.getName().toString());
    }
    
    /**
     * Returns variable setter name.
     *
     * @param variable
     * @return
     */
    protected String getParameterName(String variable) {
        assert variable != null : "Variable cannot be null";
        return getVariableName("", variable);
    }
    
    /**
     * Returns prexixed variable name in camel case format.
     *
     * @param prefix
     * @param variable
     * @return
     */
    protected String getVariableName(String prefix, String variable) {        
        assert prefix != null : "Prefix cannot be null";
        assert variable != null : "Variable cannot be null";
        
        final StringBuilder result = new StringBuilder(32);
        result.append(prefix);        
        
        if (isUpperCase(variable)) {
            boolean lower = prefix.length()==0;
            for (int i = 0, max = variable.length(); i < max; i++) {
                final char c = variable.charAt(i);
                if (c == '_') {
                    lower = false;
                } else {
                    result.append(lower ? Character.toLowerCase(c) : c);
                    lower = true;
                }
            }
        } else {
            if (prefix.length()==0) {
                result.append(variable);                
            } else {
                result.append(Character.toUpperCase(variable.charAt(0)));
                result.append(variable.substring(1));
            }
        }
        return result.toString();            
    }
    
    /** Returns true if the parameter is an Upper Case text only */
    protected boolean isUpperCase(String value) {
        for (int i = value.length()-1; i>=0; i--) {
            char c = value.charAt(i);
            if (Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }
    
    /** Copy JavaDoc */
    public void copyJavaDoc(Tree field, MethodTree method, WorkingCopy workingCopy) throws IllegalStateException {
        final List<Comment> comments = workingCopy.getTreeUtilities().getComments(field, true);
        if (comments != null && comments.size()>0) {
            try {
                workingCopy.getTreeMaker().addComment(method, comments.get(0), true);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get JavaDoc
     */
    public String getInLineJavaDoc(String prefix, VariableTree field, WorkingCopy workingCopy) throws IllegalStateException {
        String result = "";
        try {
            final List<Comment> comments = workingCopy.getTreeUtilities().getComments(field, true);
            if (comments != null && !comments.isEmpty()) {
                result = comments.get(0).getText().trim();
                if (comments.get(0).isDocComment()) {
                    result = result.substring(3, result.length()-2).trim();
                }
                result = result.replace('\n', ' ');
                result = result.replaceAll(" \\* ", " ");
                while (result.indexOf("  ") > 0) {
                    result = result.replaceAll("  ", " ");
                }
                if (result.length()>0) {
                    result = prefix + result;                    
                }
            } 
        } catch (Throwable e) {
            // TODO: LogIt:
            result = "";
        }
        return result;
    }
    
    
}
