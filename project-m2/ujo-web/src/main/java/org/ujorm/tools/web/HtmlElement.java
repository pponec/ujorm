/*
 * Copyright 2018-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.tools.web;

import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import java.nio.charset.Charset;

/** The root of HTML elements
 *
 * <h4>Usage</h4>
 * <pre class="pre">
 * var response = HttpContext.of();
 * try (var html = AbstractHtmlElement.of(response)) {
 *     try (var body = html.getBody()) {
 *         body.addHeading("Hello!");
 *         body.addLabel().addText("Active:")
 *             .addCheckBox("active").setCheckBoxValue(true);
 *     }
 * }
 * assertTrue(response.toString().contains("&lt;h1&gt;Hello!&lt;/h1&gt;"));
 * </pre>
 *
 * For more information see the
 * <a target="_top" href="https://jbook-samples-free.ponec.net/sample?src=net.ponec.jbook.s01_hello.HelloWorldElement">next sample</a>.
 */
public class HtmlElement extends AbstractHtmlElement {

    /** New instance */
    public HtmlElement(@NotNull HtmlConfig config, @NotNull Appendable writer) {
        super(config, writer);
    }

    /** New instance */
    public HtmlElement(@NotNull ApiElement root, @NotNull HtmlConfig config, @NotNull Appendable writer) {
        super(root, config, writer);
    }

    // ------- Static methods ----------

    /** Create a root element for a required element name. The MAIN factory method. */
    @NotNull
    public static HtmlElement of(
            @NotNull final Appendable writer,
            @Nullable final HtmlConfig myConfig
    ) throws IllegalStateException {
        return AbstractHtmlElement.of(writer, myConfig);
    }

    /** Create a root element with an indented output format. The MAIN factory method. */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final Appendable writer,
            @Nullable final HtmlConfig myConfig
    ) throws IllegalStateException {
        DefaultHtmlConfig conf = new DefaultHtmlConfig(myConfig).setNiceFormat();
        return AbstractHtmlElement.of(writer, conf);
    }

    /** Create a root element for a required element name. The MAIN factory method. */
    @NotNull
    public static HtmlElement of(
            @NotNull final HttpContext context,
            @Nullable final HtmlConfig myConfig) {
        return of(context.writer(), myConfig);
    }

    /** Create new instance. */
    @NotNull
    public static HtmlElement ofResponse(
            @NotNull final HttpServletResponse httpServletResponse,
            @Nullable final HtmlConfig config) {
        return of(HttpContext.ofServletResponse(httpServletResponse).writer(), config);
    }

    /** Create new instance.
     * @param config Html configuration
     * @return An instance of the HtmlPage
     */
    @NotNull
    public static HtmlElement of(@Nullable final HtmlConfig config) throws IllegalStateException {
        return of(new StringBuilder(256), config);
    }

    // --- Convenience Factory Methods (Pairs of: of / niceOf) ---

    // 1. Appendable variants (Generic)

    /** Create new instance.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault();
        conf.setTitle(title);
        conf.setCssLinks(cssLinks);
        return of(response, conf);
    }

    /** Create new instance with an indented output format.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault();
        conf.setNiceFormat();
        conf.setTitle(title);
        conf.setCssLinks(cssLinks);
        return of(response, conf);
    }

    // 2. Appendable variants (With Charset)

    /** Create new instance. */
    @NotNull
    public static HtmlElement of(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final Charset charset, @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault();
        conf.setTitle(title);
        conf.setCharset(charset);
        conf.setCssLinks(cssLinks);
        return of(response, conf);
    }

    /** Create new instance with an indented output format.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final Charset charset, @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault();
        conf.setNiceFormat();
        conf.setTitle(title);
        conf.setCharset(charset);
        conf.setCssLinks(cssLinks);
        return of(response, conf);
    }

    // 3. Servlet/Response variants (Title + Response)

    /** Create new instance.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(
            @NotNull final String title,
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServletResponse(httpServletResponse).writer(), config);
    }

    /** Create new instance with an indented output format.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final String title,
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServletResponse(httpServletResponse).writer(), config);
    }

    // 4. Servlet/Response variants (Response only)

    /** Create new instance.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement ofResponse(
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServletResponse(httpServletResponse).writer(), config);
    }

    /** Create new instance with an indented output format.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOfResponse(
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServletResponse(httpServletResponse).writer(), config);
    }

    // 5. HttpContext variants (Title + Context)

    /** Create new instance.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(
            @NotNull final String title,
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(context.writer(), config);
    }

    /** Create new instance with an indented output format.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final String title,
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(context.writer(), config);
    }

    // 6. HttpContext variants (Context only)

    /** Create new instance.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault().setCssLinks(cssLinks);
        return of(context.writer(), conf);
    }

    /** Create new instance with an indented output format.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault().setCssLinks(cssLinks);
        conf.setNiceFormat();
        return of(context.writer(), conf);
    }

    /** Create new instance with an indented output format.
     * @see Appendable
     * @deprecated Use the method {@link #of(HtmlConfig, HttpContext, CharSequence[])}
     */
    @NotNull
    @Deprecated
    public static HtmlElement niceOf(
            @NotNull final HtmlConfig config,
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final var conf = new DefaultHtmlConfig(config);
        if (cssLinks.length > 0) {
            conf.setCssLinks(cssLinks);
        }
        conf.setNiceFormat();
        return of(context.writer(), conf);
    }

    /** Create new instance.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(
            @NotNull final HtmlConfig config,
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final var conf = new DefaultHtmlConfig(config);
        if (cssLinks.length > 0) {
            conf.setCssLinks(cssLinks);
        }
        return of(context.writer(), conf);
    }

    //---

    /** Create new instance with an indented output format.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final HtmlConfig config,
            @NotNull final HttpServletResponse response,
            @NotNull final CharSequence... cssLinks) {
        return niceOf(config, HttpContext.ofServletResponse(response), cssLinks);
    }

    /** Create new instance.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(
            @NotNull final HtmlConfig config,
            @NotNull final HttpServletResponse response,
            @NotNull final CharSequence... cssLinks) {
        return of(config, HttpContext.ofServletResponse(response), cssLinks);
    }

    //---

    /** Create new instance with an indented output format.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault().setCssLinks(cssLinks);
        conf.setNiceFormat();
        return of(HttpContext.ofServletResponse(httpServletResponse).writer(), conf);
    }

    /** Create new instance.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault().setCssLinks(cssLinks);
        return of(HttpContext.ofServletResponse(httpServletResponse).writer(), conf);
    }

    //---

    /** Create new instance with an indented output format and ISO number formatting.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceNumberOf(
            @NotNull final String title,
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault().setCssLinks(cssLinks);
        conf.setTitle(title);
        conf.setNiceFormat();
        conf.setIsoFormatter();
        return of(HttpContext.ofServletResponse(httpServletResponse).writer(), conf);
    }

    /** Create new instance with an indented output format and ISO number formatting.
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceNumberOf(
            @NotNull final String title,
            @NotNull final HttpContext httpContext,
            @NotNull final CharSequence... cssLinks) {
        final var conf = HtmlConfig.ofDefault().setCssLinks(cssLinks);
        conf.setTitle(title);
        conf.setNiceFormat();
        conf.setIsoFormatter();
        return of(httpContext, conf);
    }
}