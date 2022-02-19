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

import java.util.logging.*;
import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.msg.MsgFormatter;

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
    private final Logger logger;

    private UjoLoggerFactory(@NotNull String name) {
        this.logger = java.util.logging.Logger.getLogger(name);
    }

    /** Konstructor */
    public UjoLoggerFactory(@NotNull Class name) {
        this(name.getName());
    }

    /** {@inheritdoc} */
    @Override
    public boolean isLoggable(@NotNull final Level level) {
        return logger.isLoggable(level);
    }

    /** {@inheritdoc} */
    @Override
    public void log(@NotNull final Level level, final String message) {
        logger.log(level, message);
    }

    /** {@inheritdoc} */
    @Override
    public void log(@NotNull final Level level, final String message, final Throwable e) {
        logger.log(level, message, e);
    }

    /** {@inheritdoc} */
    @Override
    public void log(@NotNull final Level level, final String message, final Object parameter) {
        if (logger.isLoggable(level)) {
            logger.log(level, MsgFormatter.format(message, parameter));
        }
    }

    /** {@inheritdoc} */
    @Override
    public void log(@NotNull final Level level, final String message, final Object... parameters) {
        if (logger.isLoggable(level)) {
            logger.log(level, MsgFormatter.format(message, parameters));
        }
    }

    // ---------- FACTORY -----------------

    public static UjoLogger getLogger(@NotNull Class<?> name) {
        return slf4jSupport
             ? newUjoLoggerBridge2Slf4j(name)
             : new UjoLoggerFactory(name)
             ;
    }

    private static UjoLogger newUjoLoggerBridge2Slf4j(@NotNull Class name) {
        UjoLogger result;
        try {
            result = new UjoLoggerBridge2Slf4j(name);
            if (showLog) {
                showLog = false;
                result.log(Level.FINEST, "Ujorm logging is switched to the SLF4J.");
            }
        } catch (RuntimeException | NoClassDefFoundError | OutOfMemoryError e) {
            slf4jSupport = false;
            result = new UjoLoggerFactory(name);
            result.log(Level.INFO, "Ujorm logging is switched to the JUL.");
        }
        return result;
    }

    // ---------- LOG MESSAGES -----------------

    /** Get a runtime information */
    public static String getRuntimeInfo(String applicationName) {
        final StringBuilder result = new StringBuilder(256);
        result.append(MsgFormatter.format("The application ''{0}'' is starting with the properties"
                , applicationName));
        final String[] properties =
        {  "java.version"
        ,  "java.vendor"
        ,  "java.home"
        ,  "os.name"
        ,  "os.arch"
        ,  "user.name"
        ,  "user.home"
        ,  "user.dir"
        };
        for (int i = 0; i < properties.length; i++) {
            final String property = properties[i];
            result.append(i==0 ? ": " : ", ");
            result.append(property);
            result.append('=');
            result.append(System.getProperty(property, "?"));
        }
        return result.toString();
    }

}
