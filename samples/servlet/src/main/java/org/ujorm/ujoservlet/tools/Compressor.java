package org.ujorm.ujoservlet.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Field converter
 * @author Pavel Ponec
 */
public class Compressor {

    /** Comppress */
    public byte[] compress(final byte[] fields) {
        final ByteArrayOutputStream result = new ByteArrayOutputStream(fields.length / 4);
        try (OutputStream out = new DeflaterOutputStream(result)) {
            out.write(fields);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return result.toByteArray();

    }

    /** Decompress */
    public byte[] decompress(final byte[] data) {
        final InputStream in = new InflaterInputStream(new ByteArrayInputStream(data));
        try (ByteArrayOutputStream result = new ByteArrayOutputStream(512)) {
            byte[] buffer = new byte[512];
            int len;
            while ((len = in.read(buffer)) > 0) {
                result.write(buffer, 0, len);
            }
            return result.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
