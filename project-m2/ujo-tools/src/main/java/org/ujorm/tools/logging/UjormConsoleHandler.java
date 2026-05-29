/*
 * Copyright 2024-2026 Pavel Ponec
 * https://github.com/pponec/ujorm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.logging;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * A JUL handler that writes to {@code System.out} instead of {@code System.err}.
 *
 * <p>The standard {@link java.util.logging.ConsoleHandler} always writes to {@code System.err},
 * which is invisible to {@code ./mvnw test | tee log.txt} or CI log capture.
 * This handler solves that by routing output to {@code System.out} so that
 * SQL statements logged by Ujorm appear in the same stream as test results.
 *
 * <h4>Usage in {@code ujorm-config.properties} (test resources)</h4>
 * <pre>
 * handlers = org.ujorm.tools.logging.UjormConsoleHandler
 * org.ujorm.tools.jdbc.AbstractSqlQuery.level = INFO
 * java.util.logging.SimpleFormatter.format = %1$tFT%1$tT.%1$tL %4$s: %5$s%6$s%n
 * </pre>
 *
 * <p>Add these lines to {@code ujorm-config.properties} in {@code src/test/resources/} and they will
 * be automatically applied by {@code org.ujorm.orm.Config} on class load —
 * no JVM argument or Maven Surefire configuration needed.
 */
public class UjormConsoleHandler extends StreamHandler {

    public UjormConsoleHandler() {
        super(System.out, new SimpleFormatter());
        setLevel(java.util.logging.Level.ALL);
    }

    @Override
    public synchronized void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    @Override
    public synchronized void close() {
        flush();
    }
}
