/*
 *  Copyright 2011-2022 Pavel Ponec
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
 * Ujorm Logger Interface.<br>
 * The interface provides a level constants with name similar to the SLF4J, for example: UjoLogger.ERROR,  UjoLogger.DEBUG.
 * @author Pavel Ponec
 */
public interface UjoLogger {

    /** A level alias for the constant {@code SEVERE} from the class {@code java.util.logging.Level} */
    public static final Level ERROR = Level.SEVERE;
    /** A level alias for the constant {@code WARNING} from the class {@code java.util.logging.Level} */
    public static final Level WARN = Level.WARNING;
    /** A level alias for the constant {@code INFO} from the class {@code java.util.logging.Level} */
    public static final Level INFO = Level.INFO;
    /** A level alias for the constant {@code FINE} from the class {@code java.util.logging.Level} */
    public static final Level DEBUG = Level.FINE;
    /** A level alias for the constant {@code FINEST} from the class {@code java.util.logging.Level} */
    public static final Level TRACE = Level.FINEST;


    /**
     * Check if a message of the given level would actually be logged
     * by this logger.  This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *
     * @param	level   One of the message level identifiers, see for example {@link UjoLogger#ERROR},  {@link UjoLogger#DEBUG}
     * @return	true if the given message level is currently being logged.
     */
    public boolean isLoggable(Level level);

    /**
     * Log a message, with no arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then the given message is forwarded to all the
     * registered output Handler objects.
     * <p>
     * @param	level   One of the message level identifiers, see for example {@link UjoLogger#ERROR},  {@link UjoLogger#DEBUG}
     * @param   message	The string message (or a key in the message catalog)
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
     * key, rather than the LogRecord parameters key.  Thus is it
     * processed specially by output Formatters and is not treated
     * as a formatting parameter to the LogRecord message key.
     * <p>
     * @param	level   One of the message level identifiers, see for example {@link UjoLogger#ERROR},  {@link UjoLogger#DEBUG}
     * @param   message	The string message (or a key in the message catalog)
     * @param   e  Throwable associated with log message.
     */
    public void log(Level level, String message, Throwable e);

    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param	level   One of the message level identifiers, see for example {@link UjoLogger#ERROR},  {@link UjoLogger#DEBUG}
     * @param   message	The string message (or a key in the message catalog)
     * @param   parameter An optional parameter for the message
     *          are replaced by the {@code {}} sequence or the {@code {0}} one
     */
    public void log(Level level, String message, Object parameter);

    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param	level   One of the message level identifiers, see for example {@link UjoLogger#ERROR},  {@link UjoLogger#DEBUG}
     * @param   message	The string message (or a key in the message catalog)
     * @param   parameters An optional array of parameters for the message
     *          are replaced by the {@code {}} sequence or the {@code {0}} one
     */
    public void log(Level level, String message, Object ... parameters);

}
