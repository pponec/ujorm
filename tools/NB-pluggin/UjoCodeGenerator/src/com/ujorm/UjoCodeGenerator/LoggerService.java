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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Logger service
 * @author Pavel Ponec
 */
final public class LoggerService {
    
    /** Enable an Error Message Dialog */
    private static boolean DIALOG_ENABLED = true;
    
    /** Common Logger */
    private static final Logger LOGGER = Logger.getLogger(LoggerService.class.getName());   

    /** Show Message on Dialog display */
    public static void displayLogMessage(Object msg) {
        displayLogMessage(String.valueOf(msg), null);
    }
    
    /** Show Message on Dialog display */
    public static void displayLogMessage(String msg, Throwable e) {                
        if (DIALOG_ENABLED) {
            Throwable ex = msg!=null ? new RuntimeException(msg, e) : e ;
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(ex));            
        } else {
            LOGGER.log(Level.SEVERE, msg, e);
        }
    }
    
}
