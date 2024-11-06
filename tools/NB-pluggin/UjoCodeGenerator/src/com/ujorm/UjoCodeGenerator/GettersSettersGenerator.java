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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Lookup;

/**
 * Getters and Setters Generator
 * @author Martin Mahr <mahr@effectiva.cz>
 */
public class GettersSettersGenerator implements CodeGenerator {

    /** Common Logger */
    private static final Logger LOGGER = Logger.getLogger(GettersSettersGenerator.class.getName());
    /** Field from a context */
    private JTextComponent textComp;

    /**
     *
     * @param context containing JTextComponent and possibly other items
     * registered by {@link CodeGeneratocontextProvider}
     */
    public GettersSettersGenerator(Lookup context) { // Good practice is not to save Lookup outside ctor
        textComp = context.lookup(JTextComponent.class);
    }

    @MimeRegistration(mimeType = "text/x-java", service = CodeGenerator.Factory.class)
    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new GettersSettersGenerator(context));
        }
    }

    /**
     * The name which will be inserted inside Insert Code dialog
     */
    @Override
    public String getDisplayName() {
        return "UJO getters/setters";
    }

    /**
     * This will be invoked when user chooses this Generator from Insert Code
     * dialog
     */
    @Override
    public void invoke() {
        try {
            Document doc = textComp.getDocument();
            JavaSource javaSource = JavaSource.forDocument(doc);
            CancellableTask task = getModificationTask();

            ModificationResult result = javaSource.runModificationTask(task);
            result.commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error of method invoke()", e);
        }
    }

    protected CancellableTask getModificationTask(){
        return new GenerateGettersSettersTask();
    }
}
