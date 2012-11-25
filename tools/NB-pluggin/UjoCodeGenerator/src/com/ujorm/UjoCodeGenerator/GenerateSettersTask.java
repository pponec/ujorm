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
