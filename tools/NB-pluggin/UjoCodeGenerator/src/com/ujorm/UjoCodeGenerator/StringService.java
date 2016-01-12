/*
 *  Copyright 2012-2014 Pavel Ponec
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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.ujorm.UjoCodeGenerator.bo.PrefixEnum;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.WorkingCopy;
import static com.ujorm.UjoCodeGenerator.bo.PrefixEnum.*;

/**
 * String service
 * @author Pavel Ponec
 */
final public class StringService {

    /** Common Logger */
    private static final Logger LOGGER = Logger.getLogger(StringService.class.getName());

    /** Comment class */
    private static final  String COMMENT_SIMPLE = "Comment";
    /** Comment Package */
    private static final  String  COMMENT_PACKAGE = "org.ujorm.orm.annot.";


    /**
     * Returns variable getter name.
     *
     * @param variable
     * @return
     */
    public String getGetterName(PrefixEnum prefix, VariableTree variable) {
        assert variable != null : "Variable cannot be null";
        return getGetterName(prefix, variable.getName().toString());
    }

    /**
     * Returns variable getter name.
     *
     * @param variable
     * @return
     */
    protected String getGetterName(PrefixEnum prefix, String variable) {
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
    protected String getVariableName(PrefixEnum prefix, String variable) {
        assert prefix != null : "Prefix cannot be null";
        assert variable != null : "Variable cannot be null";

        final StringBuilder result = new StringBuilder(32);
        result.append(prefix);

        if (isUpperCase(variable)) {
            boolean lower = prefix.isEmpty();
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
            if (prefix.isEmpty()) {
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
            Comment comment = getComment(workingCopy, variable);
            if (comment != null) {
                workingCopy.getTreeMaker().addComment(newMethod, comment, true);
            }
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "Can't copy JavaDoc to the method: " + newMethod.getName(), e);
        }
    }

    /** Create comment */
    private Comment getComment(WorkingCopy workingCopy, VariableTree field) {
        final List<Comment> comments = workingCopy.getTreeUtilities().getComments(field, true);
        final Comment comment = (comments!=null && !comments.isEmpty())
                ? comments.get(comments.size() - 1)
                : null;

        return comment != null
                ? comment
                : getCommentMessage(field);
    }

    /** Get a message of the @Comment annotation in the JavaDoc format */
    private Comment getCommentMessage(final VariableTree field) {
        for (AnnotationTree annotation : field.getModifiers().getAnnotations()) {
            if (isCommentType(annotation)) {
                final String msg = annotation.getArguments().get(0).toString();
                final char quotation = '"';
                final int beg = 1 + msg.indexOf(quotation);
                final int end = msg.lastIndexOf(quotation);

                if (beg < end) {
                    final String doc = "/** " + msg.substring(beg, end) + " */";
                    return Comment.create(Comment.Style.JAVADOC, doc);
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    /** Check a comment annotation. */
    private boolean isCommentType(AnnotationTree anot) {
        if (anot.getArguments().size()!=1) {
            return false;
        }
        final String annotationType = anot.getAnnotationType().toString();
        return annotationType.equals(COMMENT_SIMPLE)
                || annotationType.equals(COMMENT_PACKAGE + COMMENT_SIMPLE);
    }

    /** Get the JavaDoc */
    public String getInLineJavaDoc(VariableTree field, WorkingCopy workingCopy) throws IllegalStateException {
        String result = "";
        try {
            final Comment comment = getComment(workingCopy, field);
            if (comment != null) {
                result = comment.getText().trim();
                if (comment.isDocComment()) {
                    result = result.substring(3, result.length()-2).trim();
                }
                result = result.replace('\n', SPACE);
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
