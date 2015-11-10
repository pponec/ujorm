/*
 *  Copyright 2009-2015 Pavel Ponec
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
package org.ujorm.implementation.orm;

import org.ujorm.Validator;
import org.ujorm.extensions.Property;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;
import static org.ujorm.extensions.PropertyModifier.*;
import static org.ujorm.orm.ao.LazyLoading.*;

/** The special Key pro a LazyLoading */
public class OrmProperty<U extends OrmUjo, VALUE> extends Property<U, VALUE> {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(OrmProperty.class);

    // /** Is the Key type of Relation  */
    // private boolean relation;

    public OrmProperty(int index) {
        super(index);
    }

    public OrmProperty(int index, String name, VALUE defaultValue, Validator<VALUE> validator) {
        super(index);
        init(NAME, name);
        init(DEFAULT_VALUE, defaultValue);
        init(VALIDATOR, validator);
    }

    /** Lock the Property */
    @Override
    protected void lock() {
        super.lock();
        // relation = isTypeOf(OrmUjo.class); // TODO ...
    }

    /**
     * An alias for the method of(Ujo) with a lazy-loading features.<br>
     * Note: if no session is available than lazy attributes provides the {@code null} value - since the release 1.33.
     */
    @SuppressWarnings("unchecked")
    @Override
    public VALUE of(final U ujo) {
        Session mySession = ujo.readSession();  // maybe readSession() is better?
        Object result = ujo.readValue(this);

        if (isTypeOf(OrmUjo.class)) {
            if (result instanceof ForeignKey) {
                if (mySession == null) {
                    return null;
                }
                if (DISABLED.equalsTo(mySession.getLazyLoading())) {
                    throw new IllegalStateException("The lazy loading is disabled in the current Session.");
                }
                if (mySession.isClosed()) {
                   IllegalStateException e = null;
                    switch (mySession.getLazyLoading()) {
                        default:
                            // The method "ujo.toString()" calls a never ending loop here!
                            final String msg = String.format
                                   ( "The lazy loading of the key '%s' is disabled due the closed Session"
                                    , getFullName());
                            throw new IllegalStateException(msg);
                        case ALLOWED_ANYWHERE_WITH_STACKTRACE:
                            if (LOGGER.isLoggable(UjoLogger.INFO)) {
                                e = new IllegalStateException(mySession.getLazyLoading().name());
                            }
                        case ALLOWED_ANYWHERE_WITH_WARNING:
                            if (LOGGER.isLoggable(UjoLogger.INFO)) {
                                LOGGER.log(UjoLogger.WARN, "The lazy loading on closed session on the key " + getFullName() + " = " + result, e);
                            }
                        case ALLOWED_ANYWHERE:
                            // open temporary session if it's closed ;) - because of lazy-loading of detached objects (caches, etc.) */
                            final Session tempSession = mySession.getHandler().createSession();
                            try {
                                result = loadOrmUjo(tempSession, (ForeignKey) result);
                                if (result != null) {
                                    // Assign the same session due original session arguments (lazyLoading e.g.):
                                    ((OrmUjo) result).writeSession(mySession);
                                }
                            } finally {
                                tempSession.close();
                            }
                    }
                } else {
                    result = loadOrmUjo(mySession, (ForeignKey) result);
                }
                ujo.writeSession(null); // Replacing of the foreign key is not a key change
                ujo.writeValue(this, result);
                ujo.writeSession(mySession); // Restore the Session
            } else if (result != null
                && mySession != null
                && mySession != ((OrmUjo) result).readSession()
                // A sesssion will be written to a result in case missing primary key too - due more relations
            ) {
                // Write the current session to a related object:
                ((OrmUjo) result).writeSession(mySession);
            }
        }
        return result != null
                ? (VALUE) result
                : getDefault();
    }

    /**
     * Load OrmUjo
     * @param session A required session
     * @param foreignKey A required foreign key
     * @return Load UJO by a unique id. If the result is not unique, then an exception is throwed.
     */
    protected OrmUjo loadOrmUjo(final Session session, final ForeignKey foreignKey) {
        assert session != null;
        assert foreignKey != null;
        return session.loadInternal(this, foreignKey.getValue(), true);
    }
}
