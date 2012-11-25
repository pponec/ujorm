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

import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Lookup;

public class UjoSettersGenerator extends GettersSettersGenerator implements CodeGenerator {
    public UjoSettersGenerator(Lookup context) {
        super(context);
    }
    
    @MimeRegistration(mimeType = "text/x-java", service = CodeGenerator.Factory.class)
    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            // Tady se musí rozhodnout, zda-li je vůbec generátor použitelný
            return Collections.singletonList(new UjoSettersGenerator(context));
        }
    }

    @Override
    public String getDisplayName() {
        return "UJO setters generator";
    }

    @Override
    protected CancellableTask getModificationTask() {
        return new GenerateSettersTask();
    }
}
