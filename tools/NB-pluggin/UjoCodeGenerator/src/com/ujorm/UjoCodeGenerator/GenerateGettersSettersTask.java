/*
 *  Copyright 2012 Martin Mahr
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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.ujorm.UjoCodeGenerator.bo.KeyItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Martin Mahr <mahr@effectiva.cz>
 */
public class GenerateGettersSettersTask implements CancellableTask<WorkingCopy> {

    private static final StringService stringService = new StringService();
    /** List of Method model */
    private List<VariableTree> ujoMembers = new ArrayList<VariableTree>();
    private List<MethodTree> methods = new ArrayList<MethodTree>();
    private WorkingCopy workingCopy;
    private TreeMaker treeMaker;
    private ClassTree clazz = null;
    private boolean copyJavaDoc = true;
    PropertiesChooser propertiesChooser = null;

    /**
     * Opens dialog with Ujo members list.
     * 
     * @param wc
     * @throws IOException 
     */
    @Override
    public void run(WorkingCopy wc) throws IOException {        
        workingCopy = wc;
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        treeMaker = workingCopy.getTreeMaker();

        CompilationUnitTree cut = workingCopy.getCompilationUnit();

        for (Tree typeDecl : cut.getTypeDecls()) {
            if (!isClass(typeDecl)) {
                continue;
            }

            clazz = (ClassTree) typeDecl;

            for (Tree member : clazz.getMembers()) {
                if (isMethod(member)) {
                    methods.add((MethodTree) member);
                    continue;
                }
                if (isUjoStaticVariable(member)) {
                    addVariableToGenerate((VariableTree) member);
                    continue;
                }
            }
        }
        showDialog();
    }

    /**
     * Adds member to list for generating getters and setters.
     * 
     * @param member 
     */
    protected void addVariableToGenerate(VariableTree member) {
        assert member != null : "Member must not be null";
        
        if (!getterExistsForVariable(member) 
        ||  !setterExistsForVariable(member)) {
            ujoMembers.add(member);
        }
    }

    /**
     * Generates getters and setters for given members.
     * 
     * @param variables 
     */
    private void generateCode(KeyItem[] items, boolean getters, boolean setters) {
        assert items != null : "Variables cannot be null";
        
        ClassTree modifiedClass = clazz;

        for (KeyItem item : items) {
            VariableTree variable = item.getVariableTree();
            if (getters) {
                modifiedClass = generateGetter(variable, modifiedClass);                
            }
            if (setters) {
                modifiedClass = generateSetter(variable, modifiedClass);
            }
        }

        workingCopy.rewrite(clazz, modifiedClass);
    }

    /**
     * Creates new setter method for given variable if it isn't exists yet.
     *
     * @param variable
     * @param modifiedClass
     * @return
     */
    protected ClassTree generateSetter(VariableTree variable, ClassTree modifiedClass) {
        assert variable != null : "Variable cannot be null";
        assert modifiedClass != null : "Modified class cannot be null";

        if (!setterExistsForVariable(variable)) {
            modifiedClass = createSetter(modifiedClass, variable);
        }

        return modifiedClass;
    }

    /**
     * Creates new getter method for given variable if it isn't exists yet.
     *
     * @param variable
     * @param modifiedClass
     * @return
     */
    protected ClassTree generateGetter(VariableTree variable, ClassTree modifiedClass) {
        assert variable != null : "Variable cannot be null";
        assert modifiedClass != null : "Modified class cannot be null";

        if (!getterExistsForVariable(variable)) {
            modifiedClass = createGetter(modifiedClass, variable);
        }

        return modifiedClass;
    }

    /**
     * Creates new getter method for given UJO member.
     *
     * @param clazz
     * @param variable
     * @return
     */
    private ClassTree createGetter(ClassTree clazz, VariableTree variable) {
        assert clazz != null : "Clazz cannot be null";
        assert variable != null : "Variable cannot be null";

        ParameterizedTypeTree type = (ParameterizedTypeTree) variable.getType();
        List<? extends Tree> typeArguments = type.getTypeArguments();
        String getterName = stringService.getGetterName(variable);

        ModifiersTree methodModifiers =
                treeMaker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC),
                Collections.<AnnotationTree>emptyList());
        
        MethodTree newMethod =
                treeMaker.Method(methodModifiers,
                getterName,
                typeArguments.get(1),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                "{\nreturn " + variable.getName() + ".of(this);}\n",
                null);
        copyJavaDoc(type, newMethod);

        return treeMaker.addClassMember(clazz, newMethod);
    }

    /**
     * Creates new setter method for given UJO member.
     *
     * @param clazz
     * @param variable
     * @return
     */
    private ClassTree createSetter(ClassTree clazz, VariableTree variable) {
        assert clazz != null : "Clazz cannot be null";
        assert variable != null : "Variable cannot be null";

        ParameterizedTypeTree type = (ParameterizedTypeTree) variable.getType();
        List<? extends Tree> typeArguments = type.getTypeArguments();
        String methodName = stringService.getSetterName(variable);
        String paramName = stringService.getParameterName(variable);
        
        ModifiersTree methodModifiers =
                treeMaker.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC),
                Collections.<AnnotationTree>emptyList());

        VariableTree parameter =
                treeMaker.Variable(
                treeMaker.Modifiers(Collections.<Modifier>emptySet()),
                paramName,
                typeArguments.get(1),
                null);

        MethodTree newMethod =
                treeMaker.Method(methodModifiers,
                methodName,
                treeMaker.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(parameter),
                Collections.<ExpressionTree>emptyList(),
                "{\n" + clazz.getSimpleName().toString()
                + "."
                + variable.getName() + ".setValue(this, " + paramName + ");}\n",
                null);
        copyJavaDoc(type, newMethod);

        return treeMaker.addClassMember(clazz, newMethod);
    }

    @Override
    public void cancel() {
    }

    /**
     * Checks if given tree object is class.
     *
     * @param member
     * @return
     */
    private boolean isClass(Tree member) {
        assert member != null : "Member cannot be null";

        return Tree.Kind.CLASS == member.getKind();
    }

    /**
     * Checks if given tree object is method.
     *
     * @param member
     * @return
     */
    private boolean isMethod(Tree member) {
        assert member != null : "Member cannot be null";

        return Tree.Kind.METHOD == member.getKind();
    }

    /**
     * Checks if given tree object is UjoProperty static member.
     *
     * @param member
     * @return
     */
    private boolean isUjoStaticVariable(Tree member) {
        assert member != null : "Member cannot be null";

        if (Tree.Kind.VARIABLE == member.getKind()) {
            final VariableTree variable = (VariableTree) member;            
            final Set<Modifier> modifiers = variable.getModifiers().getFlags();

            if (Tree.Kind.PARAMETERIZED_TYPE == variable.getType().getKind()
            &&  modifiers.contains(Modifier.PUBLIC)
            &&  modifiers.contains(Modifier.STATIC)
            &&  modifiers.contains(Modifier.FINAL)                    
            ) {
                final ParameterizedTypeTree type = (ParameterizedTypeTree) variable.getType();
                if (type.getTypeArguments().size()!=2) {                    
                    // Field must have got two generics exactly:
                    return false;
                }
                
                final String variableTypeName = type.getType().toString();
                if (variableTypeName.equals("Key")
                ||  variableTypeName.equals("UjoProperty")
                ||  variableTypeName.equals("Property")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if method exists (in examined class) based on its name.
     *
     * @param variable
     * @return
     */
    protected boolean getterExistsForVariable(VariableTree variable) {
        assert variable != null : "Variable cannot be null";

        String getterName = stringService.getGetterName(variable);
        return methodExists(getterName);
    }

    /**
     * Checks if setter exists (in examined class) based on its name.
     *
     * @param variable
     * @return
     */
    protected boolean setterExistsForVariable(VariableTree variable) {
        assert variable != null : "Variable cannot be null";

        String setterName = stringService.getSetterName(variable);

        return methodExists(setterName);
    }

    /**
     * Checks if method exists (in examined class) based on its name.
     *
     * @param methodName
     * @return
     */
    private boolean methodExists(String methodName) {
        assert methodName != null : "Method name cannot be null";

        for (MethodTree method : methods) {
            if (method.getName().toString().equals(methodName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Shows dialog with available UJO properties.
     */
    private void showDialog() {
        List<KeyItem> members = new ArrayList<KeyItem>();

        for (VariableTree var : ujoMembers) {            
            final String comment = stringService.getInLineJavaDoc("", var, workingCopy);
            members.add(new KeyItem(var, comment));                
        }

        propertiesChooser = new PropertiesChooser(members);
        propertiesChooser.selectAll();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(propertiesChooser, "Select properties", true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateCode
                        ( propertiesChooser.getSeletedProperties()
                        , propertiesChooser.isGettersRequired()
                        , propertiesChooser.isSettersRequired()
                        );
            }
        });
        DialogDisplayer.getDefault().notify(dialogDescriptor);
    }

    /** Copy JavaDoc */
    private void copyJavaDoc(Tree field, MethodTree method) throws IllegalStateException {
        if (copyJavaDoc) {
            stringService.copyJavaDoc(field, method, workingCopy);
        }
    }
}
