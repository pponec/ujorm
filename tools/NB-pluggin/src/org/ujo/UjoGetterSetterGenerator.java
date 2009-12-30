/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujo;

import org.netbeans.spi.editor.codegen.CodeGenerator;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Michal Vanco
 */
public class UjoGetterSetterGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {

        private static final String ERROR = "<error>"; //NOI18N

        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? Utilities.getPathElementOfKind(Tree.Kind.CLASS, path) : null;
            if (component == null || controller == null || path == null)
                return ret;
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            Elements elements = controller.getElements();
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass())
                return ret;
            Map<String, List<ExecutableElement>> methods = new HashMap<String, List<ExecutableElement>>();
            for (ExecutableElement method : ElementFilter.methodsIn(elements.getAllMembers(typeElement))) {
                List<ExecutableElement> l = methods.get(method.getSimpleName().toString());
                if (l == null) {
                    l = new ArrayList<ExecutableElement>();
                    methods.put(method.getSimpleName().toString(), l);
                }
                l.add(method);
            }
            Map<Element, List<ElementNode.Description>> gDescriptions = new LinkedHashMap<Element, List<ElementNode.Description>>();
            Map<Element, List<ElementNode.Description>> sDescriptions = new LinkedHashMap<Element, List<ElementNode.Description>>();
            Map<Element, List<ElementNode.Description>> gsDescriptions = new LinkedHashMap<Element, List<ElementNode.Description>>();
            for (VariableElement variableElement : ElementFilter.fieldsIn(elements.getAllMembers(typeElement))) {
                if (ERROR.contentEquals(variableElement.getSimpleName()))
                    continue;
                // v pripade, ze nejde o ujo property, neresim...
                if (!GeneratorUtils.isUjoField(variableElement)) {
                    continue;
                }
                ElementNode.Description description = ElementNode.Description.create(variableElement, null, true, false);
                boolean hasGetter = GeneratorUtils.hasUjoGetter(controller, variableElement, methods);
                boolean hasSetter = variableElement.getModifiers().contains(Modifier.FINAL) || GeneratorUtils.hasUjoSetter(controller, variableElement, methods);
                if (!hasGetter) {
                    List<ElementNode.Description> descriptions = gDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<ElementNode.Description>();
                        gDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
                if (!hasSetter) {
                    List<ElementNode.Description> descriptions = sDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<ElementNode.Description>();
                        sDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
                if (!hasGetter && !hasSetter) {
                    List<ElementNode.Description> descriptions = gsDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<ElementNode.Description>();
                        gsDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
            }
            if (!gDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : gDescriptions.entrySet())
                    descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new UjoGetterSetterGenerator(component, ElementNode.Description.create(typeElement, descriptions, false, false), GeneratorUtils.GETTERS_ONLY));
            }
            if (!sDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : sDescriptions.entrySet())
                    descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new UjoGetterSetterGenerator(component, ElementNode.Description.create(typeElement, descriptions, false, false), GeneratorUtils.SETTERS_ONLY));
            }
            if (!gsDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : gsDescriptions.entrySet())
                    descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
                Collections.reverse(descriptions);
                ret.add(new UjoGetterSetterGenerator(component, ElementNode.Description.create(typeElement, descriptions, false, false), 0));
            }
            return ret;
        }
    }

    private JTextComponent component;
    private ElementNode.Description description;
    private int type;

    /** Creates a new instance of GetterSetterGenerator */
    private UjoGetterSetterGenerator(JTextComponent component, ElementNode.Description description, int type) {
        this.component = component;
        this.description = description;
        this.type = type;
    }

    public String getDisplayName() {
        if (type == GeneratorUtils.GETTERS_ONLY)
            return org.openide.util.NbBundle.getMessage(UjoGetterSetterGenerator.class, "LBL_getter"); //NOI18N
        if (type == GeneratorUtils.SETTERS_ONLY)
            return org.openide.util.NbBundle.getMessage(UjoGetterSetterGenerator.class, "LBL_setter"); //NOI18N
        return org.openide.util.NbBundle.getMessage(UjoGetterSetterGenerator.class, "LBL_getter_and_setter"); //NOI18N
    }

    public void invoke() {
        final int caretOffset = component.getCaretPosition();
        final GetterSetterPanel panel = new GetterSetterPanel(description, type);
        String title;
        if (type == GeneratorUtils.GETTERS_ONLY)
            title = NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_getter"); //NOI18N
        else if (type == GeneratorUtils.SETTERS_ONLY)
            title = NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_setter"); //NOI18N
        else
            title = NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_getter_and_setter"); //NOI18N
        DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, title);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            if (js != null) {
                try {
                    ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
                            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
                            int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree)path.getLeaf(), caretOffset);
                            ArrayList<VariableElement> variableElements = new ArrayList<VariableElement>();
                            for (ElementHandle<? extends Element> elementHandle : panel.getVariables())
                                variableElements.add((VariableElement)elementHandle.resolve(copy));
                            GeneratorUtils.generateGettersAndSetters(copy, path, variableElements, type, idx);
                        }
                    });
                    GeneratorUtils.guardedCommit(component, mr);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
