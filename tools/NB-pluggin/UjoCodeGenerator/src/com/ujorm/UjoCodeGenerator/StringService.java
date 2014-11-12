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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.WorkingCopy;
import static com.ujorm.UjoCodeGenerator.ItemPrefix.*;

/**
 * String service
 * @author Pavel Ponec
 */
final public class StringService {

    /** Common Logger */
    private static final Logger LOGGER = Logger.getLogger(StringService.class.getName());

    /**
     * Returns variable getter name.
     *
     * @param variable
     * @return
     */
    public String getGetterName(ItemPrefix prefix, VariableTree variable) {
        assert variable != null : "Variable cannot be null";
        return getGetterName(prefix, variable.getName().toString());
    }

    /**
     * Returns variable getter name.
     *
     * @param variable
     * @return
     */
    protected String getGetterName(ItemPrefix prefix, String variable) {
        assert variable != null : "Variable cannot be null";
        return getVariableName(prefix, variable);
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
        return getVariableName(SET, variable);
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
        return getVariableName(EMPTY, variable);
    }

    /**
     * Returns prefixed variable name in camel case format.
     *
     * @param prefix
     * @param variable
     * @return
     */
    protected String getVariableName(ItemPrefix prefix, String variable) {
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
    public void copyJavaDoc(VariableTree variable, MethodTree newMethod, WorkingCopy workingCopy) {
        try {
            List<Comment> comments = workingCopy.getTreeUtilities().getComments(variable, true);
            if (comments!=null && comments.size()>0) {
                workingCopy.getTreeMaker().addComment(newMethod, comments.get(comments.size() - 1), true);
            }
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "Can't copy JavaDoc to the method: " + newMethod.getName(), e);
        }
    }

    /**
     * Get JavaDoc
     */
    public String getInLineJavaDoc(Tree field, WorkingCopy workingCopy) throws IllegalStateException {
        String result = "";
        try {
            final List<Comment> comments = workingCopy.getTreeUtilities().getComments(field, true);
            if (comments != null && !comments.isEmpty()) {
                final Comment lastComment= comments.get(comments.size() - 1);
                result = lastComment.getText().trim();
                if (lastComment.isDocComment()) {
                    result = result.substring(3, result.length()-2).trim();
                }
                result = result.replace('\n', ' ');
                result = result.replaceAll(" \\* ", " ");
                result = result.replaceAll("\\s+", " ");
            }
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "getInLineJavaDoc method error", e);
            result = "";
        }
        return result;
    }


}
