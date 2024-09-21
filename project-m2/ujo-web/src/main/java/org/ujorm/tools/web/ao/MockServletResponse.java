/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web.ao;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.ujorm.tools.common.ObjectUtils;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Simple proxy servlet response for getting a writer content
 * @author Pavel Ponec
 * @since 2.03
 */
public class MockServletResponse implements HttpServletResponse {

    private final Charset charset = UTF_8;
    private final Appendable writer;
    private final PrintWriter printWriter;

    public MockServletResponse() {
        this(512);
    }

    public MockServletResponse(int size) {
        writer = new StringBuilder(size);
        printWriter = ObjectUtils.toPrintWriter(writer);
    }

    @Deprecated
    @Override
    public void addCookie(Cookie cookie) {
    }

    @Deprecated
    @Override
    public boolean containsHeader(String string) {
        return false;
    }

    @Deprecated
    @Override
    public String encodeURL(String string) {
        throw getException();
    }

    @Deprecated
    @Override
    public String encodeRedirectURL(String string) {
        throw getException();
    }

    @Deprecated
    @Override
    public String encodeUrl(String string) {
        throw getException();
    }

    @Deprecated
    @Override
    public String encodeRedirectUrl(String string) {
        throw getException();
    }

    @Deprecated
    @Override
    public void sendError(int i, String string) throws IOException {
        throw getException();
    }

    @Deprecated
    @Override
    public void sendError(int i) throws IOException {
        throw getException();
    }

    @Deprecated
    @Override
    public void sendRedirect(String string) throws IOException {
        throw getException();
    }

    @Deprecated
    @Override
    public void setDateHeader(String string, long l) {
    }

    @Deprecated
    @Override
    public void addDateHeader(String string, long l) {
    }

    @Deprecated
    @Override
    public void setHeader(String string, String string1) {
    }

    @Deprecated
    @Override
    public void addHeader(String string, String string1) {
    }

    @Deprecated
    @Override
    public void setIntHeader(String string, int i) {
    }

    @Deprecated
    @Override
    public void addIntHeader(String string, int i) {
    }

    @Deprecated
    @Override
    public void setStatus(int i) {
    }

    @Deprecated
    @Override
    public void setStatus(int i, String string) {
    }

    @Override
    public int getStatus() {
        return 200;
    }

    @Deprecated
    @Override
    public String getHeader(String string) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String string) {
        return Collections.emptyList();
    }

    @Deprecated
    @Override
    public Collection<String> getHeaderNames() {
        return Collections.emptyList();
    }

    @Override
    public String getCharacterEncoding() {
        return charset.name();
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                throw getException();
            }

            @Override
            public void write(int b) throws IOException {
                writer.append((char) b);
            }
        };
    }

    @NotNull
    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Deprecated
    @Override
    public void setCharacterEncoding(String string) {
    }

    @Deprecated
    @Override
    public void setContentLength(int i) {
    }

    @Deprecated
    @Override
    public void setContentLengthLong(long l) {
    }

    @Override
    public void setContentType(String string) {
    }

    @Deprecated
    @Override
    public void setBufferSize(int i) {
    }

    @Deprecated
    @Override
    public int getBufferSize() {
        return 520;
    }

    @Override
    public void flushBuffer() {
        printWriter.flush();
    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Deprecated
    @Override
    public void setLocale(Locale locale) {
    }

    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    /** Get a content of the writer */
    @Override
    public String toString() {
        flushBuffer();
        return getContent();
    }

    /** Get a content of the writer */
    public String getContent() {
        return writer.toString();
    }

    /** Throw an unsupported exception */
    private UnsupportedOperationException getException() {
        return new UnsupportedOperationException("Not supported");
    }

}
