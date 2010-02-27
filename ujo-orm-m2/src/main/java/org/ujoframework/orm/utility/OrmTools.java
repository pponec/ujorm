/*
 *  Copyright 2010 Ponec.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujoframework.orm.utility;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;

/**
 * BLOB/CLOB useful methods.
 * @author Ponec
 */
final public class OrmTools {

    /**
     * Create a new Blob
     * @param bytes The null value is supported.
     * @return
     */
    public static final SerialBlob createBlob(byte[] bytes) {
        try {
            return bytes!=null ? new SerialBlob(bytes) : null;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Create a new Blob
     * @param inputStream
     * @return
     */
    public static final SerialBlob createBlob(InputStream inputStream) {
        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
            int len;

            while ((len = inputStream.read(buffer)) >= 0) {
                baos.write(buffer, 0, len);
            }
            return new SerialBlob(baos.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Reding error", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            throw new IllegalStateException("Reding error", e);
            }
        }
    }

    /**
     * Returns byte array to the maximal length Integer.MAX_VALUE.
     * @param blob The null value is supported.
     * @throws IllegalStateException A container for the SQLExeption
     * @throws IndexOutOfBoundsException Length of the bytes is great than Integer.MAX_VALUE
     */
    public static byte[] getBlobBytes(Blob blob) throws IllegalStateException, IndexOutOfBoundsException {
        try {
            if (blob==null) {
                return null;
            }
            if (blob.length() <= Integer.MAX_VALUE) {
                return blob.getBytes(1, (int) blob.length());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Reding error", e);
        }
        throw new IndexOutOfBoundsException("Length of the result is great than Integer.MAX_VALUE");
    }

    /**
     * Returns a Blob byte array to the maximal length Integer.MAX_VALUE.
     * @throws IllegalStateException A container for the SQLExeption
     */
    public static InputStream getBlobStream(Blob blob) throws IllegalStateException {
        try {
            return blob.getBinaryStream();
        } catch (Exception e) {
            throw new IllegalStateException("Reding error", e);
        }
    }


    // --------------


    /**
     * Create a new Clob.
     * @param text The null value is supported.
     */
    public static final SerialClob createClob(char[] text) {
        try {
            return text!=null ? new SerialClob(text) : null;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Create a new Clob.
     * @param text The null value is supported.
     */
    public static final SerialClob createClob(String text) {
        return text!=null ? createClob(text.toCharArray()) : null;
    }

    /**
     * Create a new Clob.
     */
    public static final SerialClob createClob(Reader reader) {
        try {
            char[] buffer = new char[1024];
            CharArrayWriter baos = new CharArrayWriter(buffer.length);
            int len;

            while ((len = reader.read(buffer)) >= 0) {
                baos.write(buffer, 0, len);
            }
            return new SerialClob(baos.toCharArray());
        } catch (Exception e) {
            throw new IllegalStateException("Reader error", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new IllegalStateException("Reader error", e);
            }
        }
    }

    /**
     * Returns a result to the maximal length Integer.MAX_VALUE.
     * @param clob The null value is supported.
     * @throws IllegalStateException A container for the SQLExeption
     * @throws IndexOutOfBoundsException Length of the bytes is great than Integer.MAX_VALUE
     */
    public static String getClobString(Clob clob) throws IllegalStateException, IndexOutOfBoundsException {
        try {
            if (clob==null) {
                return null;
            }
            if (clob.length() <= Integer.MAX_VALUE) {
                return clob.getSubString(1, (int) clob.length());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Reding error", e);
        }
        throw new IndexOutOfBoundsException("Length of the result is great than Integer.MAX_VALUE");
    }

    /**
     * Returns a result to the maximal length Integer.MAX_VALUE.
     * @param clob The null value is supported
     * @throws IllegalStateException A container for the SQLExeption
     * @throws IndexOutOfBoundsException Length of the bytes is great than Integer.MAX_VALUE
     */
    public static char[] getClob(Clob clob) throws IllegalStateException, IndexOutOfBoundsException {
        return clob!=null ? getClobString(clob).toCharArray() : null;
    }


}
