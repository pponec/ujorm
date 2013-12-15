/*
 *  Copyright 2009-2013 Pavel Ponec
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


package org.ujorm.orm.ao;

import org.ujorm.orm.annot.Comment;

/**
 * Lazy loading policy.
 * @author Pavel Ponec
 */
@Comment
public enum LazyLoading {

    /** Lazy loading is disabled */
    DISABLED,
    /** Lazy loading is allowed using an open open sesson. It is the <strong>default</strong> value */
    ALLOWED_USING_OPEN_SESSION,
    /** Lazy loading is allowed on an open or closed sessions, for the second case Ujorm creates new temporary session.
     * The action is logged with the WARNING but no stacktrace. */
    ALLOWED_ANYWHERE_WITH_WARNING,
    /** Lazy loading is allowed on an open or closed sessions, for the second case Ujorm creates new temporary session.
     * The action is logged with the WARNING level including a stacktrace. */
    ALLOWED_ANYWHERE_WITH_STACKTRACE,
    /** Lazy loading is allowed on an open or closed sessions, for the second case Ujorm creates new temporary session.
     * The action is newer logged. */
    ALLOWED_ANYWHERE;

    /** Type safe equalsTo */
    public boolean equalsTo(final LazyLoading another) {
        return this == another;
    }

}
