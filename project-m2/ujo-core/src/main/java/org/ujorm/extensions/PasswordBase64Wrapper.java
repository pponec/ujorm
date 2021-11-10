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
import org.jetbrains.annotations.NotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A binary array wrapper ready is to save any text to BASE64 encoded String.
 * The text inside this class is <strong>not protected</strong> by any security code.
 * A method {link #toString() returns three asterisks only.
 * @author Pavel Ponec
 * @see ValueTextable#toString()
 */
public class PasswordBase64Wrapper extends BinaryWrapper {

    /** Default password */
    private static final String DEFAULT_PASSWORD = "changeit";

    /** Constructor for BASE64 encoded text */
    public PasswordBase64Wrapper(String base64) {
        super(base64);
    }

    /** Constroctor for some character list of password */
    public PasswordBase64Wrapper(char[] password) {
        super(password);
    }

    /** Constructor for default passwrod is {@code changeit} */
    public PasswordBase64Wrapper() {
        super(DEFAULT_PASSWORD.toCharArray());
    }

    /** Returns an asterisk character or {@code "null"} for invalidated object. */
    @Override
    public String toString() {
        return binary != null ? "***" : String.valueOf((Object) null);
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
    @NotNull
    public static PasswordBase64Wrapper of(@NotNull String password) {
        return new PasswordBase64Wrapper(password.toCharArray());
    }

}
