package org.ujorm.tools.web.ao;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class UServletResponse implements Appendable {

    private Charset charset;

    private final OutputStream out;


    public UServletResponse(@NotNull OutputStream out, @NotNull String charsetName) throws UnsupportedEncodingException {
        this.charset = Charset.forName(charsetName);
        this.out = out;
    }

    public UServletResponse(@NotNull OutputStream out) {
        this.out = out;
    }

    public UServletResponse(@NotNull OutputStream out, @NotNull Charset charset) {
        this.charset = charset;
        this.out = out;
    }

    public UServletResponse() {
        this(new ByteArrayOutputStream());
    }

    public void setStatus(int i) {
    }

    public void addHeader(String s, String s1) {
    }

    public void setHeader(String s, String s1) {
    }

    public void setContentType(String contentType) {
    }

    public void write(int c) throws IOException {
        String charStr = Character.valueOf((char)c).toString();
        out.write(this.charset.encode(charStr));
    }

    public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
        for (int i = off, max = off + len; i < max; i++) {
            write(cbuf[i]);
        }
    }

    public void write(@NotNull String str, int off, int len) throws IOException {
        for (int i = off, max = off + len; i < max; i++) {
            write(str.charAt(i));
        }
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        for (int i = start; i < end; i++) {
            write(csq.charAt(i));
        }
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        return out.write(csq.toString().getBytes(charset));
    }

    @Override
    public String toString() {
        return this.out.toString();
    }
}
