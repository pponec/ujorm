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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge to logging framework JSF4J.
 * @author Ponec
 */
final class UjoLoggerBridge2Slf4j implements UjoLogger {

    /** JUL SEVERE -&gt; */
    private static final int ERROR = Level.SEVERE.intValue();
    /** JUL WARNING -&gt; */
    private static final int WARN = Level.WARNING.intValue();
    /** JUL INFO -&gt; */
    private static final int INFO = Level.INFO.intValue();
    /** JUL FINER,FINE  -&gt; */
    private static final int DEBUG = Math.min(Level.FINER.intValue(), Level.FINE.intValue());
    /** JUL FINEST  -&gt; */
    private static final int TRACE = Level.FINEST.intValue();

    /** Logger */
    final Logger logger;

    /** Konstructor */
    public UjoLoggerBridge2Slf4j(Class name) {
        this(name.getName());
    }

    /** Konstructor */
    public UjoLoggerBridge2Slf4j(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    public boolean isLoggable(Level level) {
        final int levelId = level.intValue();
        if (levelId>=ERROR) {
            return logger.isErrorEnabled();
        }
        else if(levelId >= WARN) {
            return logger.isWarnEnabled();
        }
        else if (levelId>=INFO) {
            return logger.isInfoEnabled();
        }
        else if (levelId>=DEBUG) {
            return logger.isDebugEnabled();
        }
        else /* if (levelId>=TRACE) */ {
            return true;
        }
    }

    /** Log Message */
    public void log(Level level, String message) {
        log(level, message, (Throwable) null);
    }

    /** Log Message */
    public void log(Level level, String message, Throwable e) {
        final int levelId = level.intValue();

        if (levelId>=ERROR) {
            logger.error(message, e);
        }
        else if(levelId >= WARN) {
            logger.warn(message, e);
        }
        else if (levelId>=INFO) {
            logger.info(message, e);
        }
        else if (levelId>=DEBUG) {
            logger.debug(message, e);
        }
        else /* if (levelId>=TRACE) */ {
            logger.trace(message, e);
        }
    }

    /** Log Message */
    public void log(Level level, String message, Object... params) {
        final int levelId = level.intValue();

        if (levelId>=ERROR) {
            logger.error(message, params);
        }
        else if(levelId >= WARN) {
            logger.warn(message, params);
        }
        else if (levelId>=INFO) {
            logger.info(message, params);
        }
        else if (levelId>=DEBUG) {
            logger.debug(message, params);
        }
        else /* if (levelId>=TRACE) */ {
            logger.trace(message, params);
        }
    }
}
