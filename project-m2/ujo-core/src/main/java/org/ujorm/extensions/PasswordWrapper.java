/*
 *  Copyright 2017-2017 Pavel Ponec
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
package org.ujorm.extensions;

import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.UTF_8;
import javax.annotation.Nonnull;

/**
 * A binary array wrapper ready is to save a password as base64 String.
 * @author Pavel Ponec
 * @see ValueTextable#toString()
 */
public class PasswordWrapper extends BinaryWrapper {

    /** Default password */
    private static final String DEFAULT_PASSWORD = "changeit";

    /** Constructor for BASE64 encoded text */
    public PasswordWrapper(String base64) {
        super(base64);
    }

    /** Constroctor for some character list of password */
    public PasswordWrapper(char[] password) {
        super(password);
    }

    /** Constructor for default passwrod is {@code changeit} */
    public PasswordWrapper() {
        super(DEFAULT_PASSWORD.toCharArray());
    }

    /** Returns an asterisk character or {@code "null"} for invalidated object. */
    @Override
    public String toString() {
        return binary != null ? "*" : String.valueOf((Object) null);
    }

    /** Get an original password as String
     * @throws IllegalStateException Object can be invalid by an {line #internalClean() method}
     */
    public String getPassword() throws IllegalStateException {
        return new String(getPasswordAsChars());
    }

    /** Get an original password as Characters
     * @throws IllegalStateException Object can be invalid by an {line #internalClean() method}
     */
    public char[] getPasswordAsChars() throws IllegalStateException {
        checkNoClean();
        return UTF_8.decode(ByteBuffer.wrap(binary)).array();
    }

    /** Clear binary data and invalid the object */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        super.internalClean();
    }

    /** Factory to create an instance of {@link BinaryWrapper} class. */
    @Nonnull
    public static PasswordWrapper of(@Nonnull String password) {
        return new PasswordWrapper(password.toCharArray());
    }

}
