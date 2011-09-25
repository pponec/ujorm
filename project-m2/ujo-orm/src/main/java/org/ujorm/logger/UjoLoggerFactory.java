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

import java.lang.reflect.Constructor;
import java.util.logging.*;
import org.ujorm.orm.JdbcStatement;

/**
 * Bridge to logging framework JSF4J.
 * @author Ponec
 */
final public class UjoLoggerFactory implements UjoLogger {

    /** Class name of the UjoLoggerBridge2Slf4j */
    private static final String SLF4J_BRIDGE_TYPE = "org.ujorm.logger.UjoLoggerBridge2Slf4j";

    /** Logger */
    final Logger logger;

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

    public static UjoLogger getLogger(Class<JdbcStatement> name) {
        return isSlf4jSupport()
             ? newUjoLoggerBridge2Slf4j(name)
             : new UjoLoggerFactory(name)
             ;
    }

    public static UjoLogger getLogger(String name) {
        return isSlf4jSupport()
             ? newUjoLoggerBridge2Slf4j(name)
             : new UjoLoggerFactory(name)
             ;
    }

    private static UjoLogger newUjoLoggerBridge2Slf4j(String name) {
        UjoLogger result;
        try {
            final Class clazz = Class.forName(SLF4J_BRIDGE_TYPE);
            final Constructor c = clazz.getConstructor(name.getClass());
            result = (UjoLogger) c.newInstance(name);
        } catch (Exception e) {
            result = new UjoLoggerFactory(name);
        }
        return result;
    }

    private static UjoLogger newUjoLoggerBridge2Slf4j(Class name) {
        UjoLogger result;
        try {
            final Class clazz = Class.forName(SLF4J_BRIDGE_TYPE);
            final Constructor c = clazz.getConstructor(name.getClass());
            result = (UjoLogger) c.newInstance(name);
            result.log(Level.INFO, "Ujorm logging is switched to the SLF4J.");
        } catch (Exception e) {
            result = new UjoLoggerFactory(name);
            result.log(Level.INFO, "Ujorm logging is switched to the JUL.");
        }
        return result;
    }

    /** Is supported a Slf4Java */
    private static boolean isSlf4jSupport() {
        return true;
    }

}
