package com.ujorm.UjoCodeGenerator;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Martin Mahr <mahr@effectiva.cz>
 */
public class GenerateSettersTask extends GenerateGettersSettersTask implements CancellableTask<WorkingCopy> {

    @Override
    protected void addVariableToGenerate(VariableTree member) {
        if (!setterExistsForVariable(member)){
            super.addVariableToGenerate(member);
        }
    }

    @Override
    protected ClassTree generateGetter(VariableTree variable, ClassTree modifiedClass) {
        return modifiedClass;
    }
}
