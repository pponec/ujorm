package org.ujorm.tools.web.ao;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class UServletResponse implements Appendable {

    private Charset charset;
    private final OutputStream out;
    private final OutputStreamWriter writer;


    public UServletResponse(@NotNull OutputStream out, @NotNull String charsetName) {
         this(out, Charset.forName(charsetName));
    }

    public UServletResponse(@NotNull OutputStream out) {
        this(out, StandardCharsets.UTF_8);
    }

    public UServletResponse(@NotNull OutputStream out, @NotNull Charset charset) {
        this.charset = charset;
        this.out = out;
        this.writer = new OutputStreamWriter(out, charset);
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
        writer.write(c);
        writer.flush();
    }

    public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
        writer.flush();
    }

    public void write(@NotNull String str, int off, int len) throws IOException {
        writer.write(str, off, len);
        writer.flush();
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        for (int i = start; i < end; i++) {
            write(csq.charAt(i));
        }
        return writer;
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        writer.append(csq);
        writer.flush();
        return writer;
    }

    @Override
    public String toString() {
        return this.out.toString();
    }

    public void setCharacterEncoding(String charset) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Appendable append(char c) throws IOException {
        writer.append(c);
        writer.flush();
        return writer;
    }
}
