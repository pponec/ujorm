/*
 *  Copyright 2011-2013 Pavel Ponec
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


package org.ujorm.logger;

import java.util.logging.Level;

/**
 * Ujorm Logger Interface
 * @author Pavel Ponec
 */
public interface UjoLogger {
    
    /**
     * Check if a message of the given level would actually be logged
     * by this logger.  This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *
     * @param	level	a message logging level
     * @return	true if the given message level is currently being logged.
     */
    public boolean isLoggable(Level INFO);

    /**
     * Log a message, with no arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param	level	One of the message level identifiers, e.g. SEVERE
     * @param   msg	The string message (or a key in the message catalog)
     */
    public void log(Level level, String message);

    /**
     * Log a message, with associated Throwable information.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given arguments are stored in a LogRecord
     * which is forwarded to all registered output handlers.
     * <p>
     * Note that the thrown argument is stored in the LogRecord thrown
     * property, rather than the LogRecord parameters property.  Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message property.
     * <p>
     * @param	level   One of the message level identifiers, e.g. SEVERE
     * @param   msg	The string message (or a key in the message catalog)
     * @param   thrown  Throwable associated with log message.
     */
    public void log(Level level, String message, Throwable e);

    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param	level   One of the message level identifiers, e.g. SEVERE
     * @param   msg	The string message (or a key in the message catalog)
     * @param   params	array of parameters to the message
     */
    public void log(Level SEVERE, String message, Object ... parameters);

}
