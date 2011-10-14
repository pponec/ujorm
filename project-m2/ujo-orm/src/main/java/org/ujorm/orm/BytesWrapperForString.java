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
package org.ujorm.orm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.ujorm.core.UjoService;

/**
 * The String type support to store to the CLOB (or TEXT) database type.
 * @author Pavel Ponec
 */
public class BytesWrapperForString implements BytesWrapper {

    /** The main string */
    final private String text;

    /** Create String from bytes in code UTF-8 */
    public BytesWrapperForString(final byte[] bytes) {
        text = bytes != null
             ? new String(bytes, UjoService.UTF_8)
             : null
             ;
    }

    /** Create from any CharSequence */
    public BytesWrapperForString(final CharSequence text) {
        this.text = text!=null
                ? text.toString()
                : null
                ;
    }

    /** Create from any InputStream Encode UTF-8 */
    public BytesWrapperForString(final InputStream is) throws IOException {
        this(is, UjoService.UTF_8);
    }

    /** Create from any InputStream */
    public BytesWrapperForString(final InputStream is, Charset charset) throws IOException {
        final InputStreamReader ir = new InputStreamReader(is, charset);
        final StringBuilder sb = new StringBuilder();
        int c;
        while ((c=ir.read())!=-1) {
            sb.append((char)c);
        }
        this.text = sb.toString();
    }

    /** Create NULL String */
    public BytesWrapperForString() {
        this.text = null;
    }

    /** Bytes in UTF-8 */
    @Override
    public byte[] exportToBytes() {
        return text != null
             ? text.getBytes(UjoService.UTF_8)
             : null
             ;
    }

    /** For an undefined text returns the "null" */
    @Override
    public String toString() {
        return String.valueOf(text);
    }

    /** For an undefined string returns 0 */
    public int length() {
        return text!=null ? text.length() : 0 ;
    }

    /** For an undefined string returns -1 */
    public char charAt(int index) {
        return text!=null ? text.charAt(index) : (char)-1;
    }

    /** For an undefined string throws the NullPointerException. */
    public CharSequence subSequence(int start, int end) {
        return text.subSequence(start, end);
    }

    /** Text is null */
    public boolean isNull() {
        return text==null;
    }

    /** Text is not empty */
    public boolean isFilled() {
        return text!=null && text.length()>0;
    }
}
