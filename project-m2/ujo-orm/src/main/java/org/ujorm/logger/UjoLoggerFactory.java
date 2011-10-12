/*
 *  Copyright 2011-2011 Pavel Ponec
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

import java.util.logging.*;

/**
 * Bridge to logging framework JSF4J.
 * @author Ponec
 */
final public class UjoLoggerFactory implements UjoLogger {

    /** SLF4J Support */
    volatile private static boolean slf4jSupport = true;

    /** Sign to show a log */
    volatile private static boolean showLog = true;
    
    /** Target Logger */
    final private Logger logger;

    private UjoLoggerFactory(String name) {
        this.logger = java.util.logging.Logger.getLogger(name);
    }

    /** Konstructor */
    public UjoLoggerFactory(Class name) {
        this(name.getName());
    }

    /** {@inheritdoc} */
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    /** {@inheritdoc} */
    public void log(Level level, String message) {
        logger.log(level, message);
    }

    /** {@inheritdoc} */
    public void log(Level level, String message, Throwable e) {
        logger.log(level, message, e);
    }

    /** {@inheritdoc} */
    public void log(Level level, String message, Object... parameters) {
        logger.log(level, message, parameters);
    }

    // ---------- FACTORY -----------------

    public static UjoLogger getLogger(Class<?> name) {
        return slf4jSupport
             ? newUjoLoggerBridge2Slf4j(name)
             : new UjoLoggerFactory(name)
             ;
    }

    private static UjoLogger newUjoLoggerBridge2Slf4j(Class name) {
        UjoLogger result;
        try {
            result = new UjoLoggerBridge2Slf4j(name);
            if (showLog) {
                showLog = false;
                result.log(Level.INFO, "Ujorm logging is switched to the SLF4J.");
            }
        } catch (Throwable e) {
            slf4jSupport = false;
            result = new UjoLoggerFactory(name);
            result.log(Level.INFO, "Ujorm logging is switched to the JUL.");
        }
        return result;
    }

}
