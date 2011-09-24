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
class UjoLoggerBridge2Jul implements UjoLogger {

    /** Logger */
    final Logger logger;

    public UjoLoggerBridge2Jul(String name) {
        this.logger = java.util.logging.Logger.getLogger(name);
    }

    /** Konstructor */
    public UjoLoggerBridge2Jul(Class name) {
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
}
