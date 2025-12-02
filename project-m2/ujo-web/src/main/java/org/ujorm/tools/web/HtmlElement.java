/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
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
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 *    ServletResponse response = new ServletResponse();
 *    try (HtmlElement html = HtmlElement.of(response)) {
 *        html.addBody().addHeading("Hello!");
 *    }
 *    assertTrue(response.toString().contains("&lt;h1&gt;Hello!&lt;/h1&gt;"));
 * </pre>
 *
 * For more information see the
 * <a target="_top" href="https://jbook-samples-free.ponec.net/sample?src=net.ponec.jbook.s01_hello.HelloWorldElement">next sample</a>.
 */
public class HtmlElement extends AbstractHtmlElement {
    public HtmlElement(@NotNull HtmlConfig config, @NotNull Appendable writer) {
        super(config, writer);
    }

    public HtmlElement(@NotNull ApiElement root, @NotNull HtmlConfig config, @NotNull Appendable writer) {
        super(root, config, writer);
    }

    // ------- Static methods ----------

    /** Create a root element for a required element name. The MAIN factory method. */
    @NotNull
    public static HtmlElement of(
            @NotNull final Appendable writer,
            @NotNull final HtmlConfig myConfig
    ) throws IllegalStateException {
        return AbstractHtmlElement.of(writer, myConfig);
    }

    /** Create a root element for a required element name. The MAIN factory method. */
    @NotNull
    public static HtmlElement of(
            @NotNull final HttpContext context,
            @NotNull final HtmlConfig myConfig) {
        return of(context.writer(), myConfig);
    }

    /** Create a new instance with empty HTML headers, The MAIN servlet factory method.
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement ofResponse(
            @NotNull final HttpServletResponse httpServletResponse,
            @Nullable final HtmlConfig config) {
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement ofResponse(
            @NotNull final String title,
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final Charset charset, @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final Charset charset, @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCharset(charset);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final String title,
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final HttpServletResponse httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final String title,
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(context.writer(), config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setCssLinks(cssLinks);
        return of(context.writer(), config);
    }

    /** Create new instance with empty HTML headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final String title,
            @NotNull final Appendable response,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty HTML headers
     * @param config Html configuration
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     */
    @NotNull
    public static HtmlElement of(@Nullable final HtmlConfig config) throws IllegalStateException {
        return of(new StringBuilder(256), config);
    }
}
