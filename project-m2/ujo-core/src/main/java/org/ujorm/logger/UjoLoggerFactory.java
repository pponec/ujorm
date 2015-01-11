/*
 *  Copyright 2011-2014 Pavel Ponec
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

import java.text.MessageFormat;
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
    public boolean isLoggable(final Level level) {
        return logger.isLoggable(level);
    }

    /** {@inheritdoc} */
    public void log(final Level level, final String message) {
        logger.log(level, message);
    }

    /** {@inheritdoc} */
    public void log(final Level level, final String message, final Throwable e) {
        logger.log(level, message, e);
    }

    /** {@inheritdoc} */
    public void log(final Level level, final String message, final Object parameter) {
        logger.log(level, message, parameter);
    }

    /** {@inheritdoc} */
    public void log(final Level level, final String message, final Object... parameters) {
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
                result.log(Level.FINEST, "Ujorm logging is switched to the SLF4J.");
            }
        } catch (Throwable e) {
            slf4jSupport = false;
            result = new UjoLoggerFactory(name);
            result.log(Level.INFO, "Ujorm logging is switched to the JUL.");
        }
        return result;
    }

    // ---------- LOG MESSAGES -----------------

    /** Get a runtime information */
    public static String getRuntimeInfo(String applicationName) {
        final String result = MessageFormat.format
                ( "The application ''{0}'' is starting with the properties"
                + ": java.version={1}"
                + ", java.vendor={2}"
                + ", java.home={3}"
                + ", os.name={4}"
                + ", os.arch={5}"
                + ", user.name={6}"
                + ", user.home={7}"
                + ", user.dir={8}"
                , applicationName
                , System.getProperty("java.version", "?")
                , System.getProperty("java.vendor", "?")
                , System.getProperty("java.home", "?")
                , System.getProperty("os.name", "?")
                , System.getProperty("os.arch", "?")
                , System.getProperty("user.name", "?")
                , System.getProperty("user.home", "?")
                , System.getProperty("user.dir", "?")
                );
        return result;
    }

}
