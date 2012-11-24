package com.ujorm.UjoCodeGenerator;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Martin Mahr <mahr@effectiva.cz>
 */
public class GenerateGettersTask extends GenerateGettersSettersTask implements CancellableTask<WorkingCopy> {

    @Override
    protected void addVariableToGenerate(VariableTree member) {
        if (!getterExistsForVariable(member)){
            super.addVariableToGenerate(member);
        }
    }

    @Override
    protected ClassTree generateSetter(VariableTree variable, ClassTree modifiedClass) {
        return modifiedClass;
    }
}
