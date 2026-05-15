# <img src="docs/images/ujorm3-logo.png" align="right" height="150" hspace="20"> Ujorm Element

Ujorm Element is a lightweight Java library designed for building HTML pages and handling AJAX requests using pure Java code. It allows developers to construct user interfaces as an object tree, eliminating the need for external template engines or complex frontend frameworks.

### Design Philosophy

To maintain high performance and code clarity, Ujorm Element follows these core principles:
* **Java-First UI:** HTML is composed directly in Java, enabling static analysis, easy refactoring, and type safety for the page structure.
* **No Template Runtime:** Unlike JSP, Thymeleaf, or FreeMarker, this library does not require parsing template files during request handling.
* **Shared Rendering Logic:** The same methods can render full pages (GET) and partial fragments (AJAX POST), ensuring consistent behavior.
* **Explicit AJAX Contract:** Server responses for partial updates are simple JSON maps of CSS selectors to HTML fragments, making the logic easy to reason about.

---

## Table of Contents
* [Quick Start (TL;DR)](#quick-start-tldr)
* [Core Components](#core-components)
* [AJAX Workflow](#ajax-workflow)
* [Best Practices](#best-practices)
* [Maven Dependency](#maven-dependency)
* [Tutorial Source Code](#tutorial-source-code)

---

## Quick Start (TL;DR)

The library is centered around three main classes: `HtmlElement` for the document root, `Element` for tags, and `JsonBuilder` for AJAX responses.

```java
/** Create a basic HTML page */
void createPage(ExchangeContext ctx) {
    try (var html = AbstractHtmlElement.of("Page Title", ctx)) {
        html.getHead().addStyle().addRawText("body { font-family: sans-serif; }");
        try (var body = html.addBody()) {
            body.addHeading("Welcome to Ujorm Element");
        }
    }
}
```

---

## Core Components

### 1. HtmlElement (Document Entry Point)
`HtmlElement` manages the root of the HTML document. It handles the lifecycle of the document (using `try-with-resources`), provides access to the `head` and `body`, and manages configurations like titles, CSS links, and character sets.

### 2. Element (HTML Building Blocks)
The `Element` class provides a fluent API to build HTML tags and attributes.
* **Adding Tags:** Use `addDiv()`, `addForm()`, `addInput()`, etc.
* **Setting Attributes:** Use `setClass()`, `setName()`, `setValue()`, and others.
* **Adding Content:** Use `addText()` for safe, escaped content and `addRawText()` only for trusted internal CSS or JavaScript.

*Note: For performance reasons, `addElement()` may return a reused child instance. Avoid storing sibling references longer than necessary.*

### 3. JsonBuilder (Partial Updates)
For AJAX requests, `JsonBuilder` creates a JSON object where keys are CSS selectors (e.g., `.ajax-output` or `#result`) and values are the new HTML fragments. The client-side script automatically updates the matching DOM elements.

---

## AJAX Workflow

The library includes a `JavaScriptWriter` that attaches lightweight AJAX behavior to standard HTML forms:

1. **Event Trigger:** User interactions (submit, keyup, or change) trigger a debounced event (default 250ms).
2. **Asynchronous POST:** The browser sends a POST request with an additional `_ajax=true` parameter.
3. **Server Response:** The servlet detects the AJAX parameter and uses `JsonBuilder` to return only the necessary HTML fragments.
4. **DOM Update:** The client-side script receives the JSON and updates the specified DOM elements without a full page reload.

```java
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.web.request.HttpContext;

import java.io.IOException;

/** AJAX POST Example (Jakarta Servlet) */
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    var ctx = HttpContext.of(request, response);
    if (ctx.parameter("_ajax", Boolean::parseBoolean, false)) {
        try (var json = JsonBuilder.of(ctx)) {
            // Replace the content of elements with class "ajax-output"
            json.writeClass("ajax-output", e -> printResult(e, ctx));
        }
    } else {
        doGet(request, response);
    }
}
```

---

## Best Practices

* **Lifecycle Management:** Always use `try-with-resources` for `HtmlElement`, `Element`, and `JsonBuilder` to ensure proper closing of the output stream.
* **Security:** Use `addText()` for all user-provided data to prevent XSS. Use `addRawText()` only for internal, trusted constants.
* **DRY Rendering:** Keep rendering logic in shared methods (e.g., `printResult(...)`) so both GET and POST requests produce identical HTML fragments.
* **Selectors:** Reuse stable CSS classes or IDs instead of ad-hoc strings for AJAX updates.

---

## Maven Dependency

Add the following dependency to your `pom.xml`. The library requires **Java 17 or higher**.

```xml
<dependency>
    <groupId>org.ujorm</groupId>
    <artifactId>ujo-web</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## Tutorial Source Code

The complete implementation of the concepts described above, including the GET/POST flow and AJAX integration, can be found in the following file:

👉 **[TutorialServlet.java](https://github.com/pponec/ujorm/blob/master/project-m2/ujo-web/src/test/java/org/ujorm/tools/tutorial/TutorialServlet.java)**