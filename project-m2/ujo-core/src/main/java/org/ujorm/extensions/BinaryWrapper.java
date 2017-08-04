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

import java.io.Serializable;
import java.nio.CharBuffer;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Arrays;
import java.util.Base64;
import javax.annotation.Nonnull;

/**
 * A byte array wrapper is ready is to save (to database) 
 * binary data as a BASE64 String.
 * @author Pavel Ponec
 */
public class BinaryWrapper implements StringWrapper, Serializable {
    private static final long serialVersionUID = 2017_08_04L;

    /** Binary content */
    @Nonnull
    protected byte[] binary;

    /**
     * Constructor for Base64
     * @param base64 Restore object using a BASE64 string
     */
    public BinaryWrapper(@Nonnull String base64) {
        this.binary = Base64.getDecoder().decode(base64);
    }

    /** Constructor for an array of code characters */
    public BinaryWrapper(@Nonnull char[] code) {
        binary = UTF_8.encode(CharBuffer.wrap(code)).array();
    }

    /** Constructor for bytes */
    public BinaryWrapper(@Nonnull byte[] bytes) {
        binary = new byte[bytes.length];
        System.arraycopy(bytes, 0, binary, 0, binary.length);
    }

    @Nonnull
    /** Export data in Base64 format
     * @throws IllegalStateException Object can be invalid by an {line #internalClean() method}
     */
    public String getBase64() throws IllegalStateException {
        checkNoClean();
        return Base64.getEncoder().encodeToString(binary);
    }

    @Nonnull
    @Override
    /** Export data in Base64 format
     * @throws IllegalStateException Object can be invalid by an {line #internalClean() method}
     */
    public String exportToString() throws IllegalStateException {
        return getBase64();
    }

    /** Get a copy of binary content
     * @throws IllegalStateException Object can be invalid by an {line #internalClean() method}
     */
    @Nonnull
    public byte[] getBinary() throws IllegalStateException {
        checkNoClean();
        final byte[] result = new byte[binary.length];
        System.arraycopy(binary, 0, result, 0, result.length);
        return result;
    }

    /** Returns a data encoded by BASE64 or {@code "null"} for invalidated object. */
    @Nonnull
    @Override
    public String toString() {
        return binary != null
                ? getBase64()
                : String.valueOf((Object) null);
    }

    /** Clean binary data */
    protected void internalClean() {
        if (binary != null) {
            for (int i = binary.length - 1 ; i >= 0; i--) {
                binary[i] = ' ';
            }
            binary = null;
        }
    }

    /** Check the binary data is not cleaned */
    protected void checkNoClean() throws IllegalStateException {
        if (binary == null) {
            throw new IllegalStateException("Binary data was cleaned");
        }
    }

    /** Compare with another objec */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return Arrays.equals(this.binary, ((BinaryWrapper)obj).binary);
    }


    /** Factory to create an instance of {@link BinaryWrapper} class from any text */
    @Nonnull
    public static BinaryWrapper of(@Nonnull String text) {
        return new BinaryWrapper(text.toCharArray());
    }

}
