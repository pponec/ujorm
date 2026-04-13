# <img src="docs/images/ujorm3-logo.png" align="right" height="150" hspace="20"> Ujorm3 Element for building HTML

> *"Do the simplest thing that could possibly work."* > — Kent Beck, creator of Extreme Programming and pioneer of Test-Driven Development.

Ujorm3 Web is a lightweight module for generating HTML code directly in Java.
It serves as a type-safe alternative to traditional templating systems (such as Thymeleaf, JSP, or FreeMarker).
The fundamental building block is the `Element` class, which provides a fluent API and utilizes the standard `try-with-resources` construct for automatic and correct closing of HTML tags.
This approach eliminates runtime errors from templates and shifts the validation of UI correctness to compile-time.

### Design Philosophy & Constraints

To keep the code as clear and reliable as possible, the module adheres to the following principles:
* **No External Templates:** The user interface (UI) is defined exclusively using Java code. There is no parsing of text files at runtime.
* **Type Safety:** Form field names, parameters, and passed data are checked by the compiler.
* **Full Refactoring Support:** Complex components or entire page blocks (e.g., tables, forms) can be safely extracted into separate, reusable methods using your IDE.
* **No Magic:** The resulting HTML code is generated explicitly, without hidden states or complex contexts.

---

## Menu
* [Quick Start (TL;DR)](#quick-start-tldr)
* [Building HTML Elements](#building-html-elements)
* [Forms and Inputs](#forms-and-inputs)
* [Refactoring and Reusability](#refactoring-and-reusability)
* [Simple AJAX Support](#simple-ajax-support)
* [Maven Dependencies & Setup](#maven-dependencies--setup)
* [Benchmarks](#benchmarks)
* [Related Links](#related-links)

---

## Quick Start (TL;DR)

Creating a valid HTML document requires only a context instance and method chaining. `try` blocks ensure element nesting.

```java
/** Generates a simple HTML page */
void quickStart() {
    var response = HttpContext.of();
    try (var html = AbstractHtmlElement.of(response)) {
        try (var body = html.getBody()) {
            body.addHeading("Hello!");
            body.addLabel()
                .addText("Active:")
                .addCheckBox("active")
                .setCheckBoxValue(true);
        }
    }
}
```
> 💡 **Sample Application:** For a practical demonstration of deploying the module in a real web application, check out the reference implementation **[PetStore](https://github.com/pponec/ujorm-petstore?tab=readme-ov-file#ujorm-petstore)**.

## Building HTML Elements

The `Element` class contains ready-made methods for most standard HTML tags (`addDiv`, `addSpan`, `addParagraph`, `addTable`, etc.). To set CSS classes, simply pass parameters as `varargs` of type `CharSequence`. Attributes can be easily set using the `setAttribute()` method or its shortened version `setAttr()`.

```java
/** Renders a styled alert box */
void renderAlert(Element parent) {
    try (var div = parent.addDiv("alert", "alert-warning")) {
        div.setAttr("role", "alert");
        div.addHeading(4, "Warning!", "alert-heading");
        div.addParagraph().addText("Please check your input data.");
    }
}
```

## Forms and Inputs

Creating forms is straightforward. The `Element` class allows easy method chaining for defining form elements, their names, and values. Special attention is paid to components like checkboxes, where the library automatically handles inserting a hidden field to correctly submit a negative (`false`) value.

```java
/** Renders a form for saving an entity */
void renderForm(Element body, Long entityId, String defaultName) {
    try (var form = body.addForm().setMethod(Html.V_POST).setAction("?action=save")) {
        try (var row = form.addDiv()) {
            try (var col = row.addDiv()) {
                col.addTextInput()
                    .setNameValue("name", defaultName)
                    .setHint("Enter name");
            }
            row.addDiv().addSubmitButton().addText("Save");
        }
    }
}
```

## Refactoring and Reusability

A major advantage of writing UI in Java is the possibility of natural code decomposition. Instead of using complex directives for inserting fragments (as with templates), you simply divide the page creation into logical methods. This keeps the main rendering method short and readable.

```java
/** Renders the whole page */
void renderPage(Element body, List<Pet> pets, String contextPath) {
    renderHeader(body, contextPath);
    renderTable(body, pets);
}

/** Renders the page header */
void renderHeader(Element body, String contextPath) {
    try (var header = body.addDiv()) {
        header.addHeading(1, "Ujorm PetStore", "text-primary");
    }
}

/** Renders the data table */
void renderTable(Element body, List<Pet> pets) {
    try (var table = body.addTable()) {
        try (var headRow = table.addTableHead().addTableRow()) {
            headRow.addTableDetail().addText("ID");
            headRow.addTableDetail().addText("Name");
        }
        try (var tbody = table.addTableBody()) {
            for (var pet : pets) {
                try (var row = tbody.addTableRow()) {
                    row.addTableDetail().addText(pet.id());
                    row.addTableDetail().addText(pet.name());
                }
            }
        }
    }
}
```

## Simple AJAX Support

Ujorm3 Web provides native and minimalist support for AJAX, which does not require writing custom client-side JavaScript. Using the built-in `JavaScriptWriter` class, a handler script can be generated into the header that automatically intercepts form submissions. On the server side, the request is then processed and the response is formatted using the `JsonBuilder` tool, which ensures that only specific HTML elements (e.g., based on CSS classes) are redrawn.

```java
void renderPageWithAjax(HttpContext context) {
    try (var html = AbstractHtmlElement.of("AJAX Demo", context)) {
        // Injects the necessary JavaScript into the <head>
        new JavaScriptWriter().write(html.getHead());

        try (var body = html.addBody()) {
            try (var form = body.addForm().setMethod(Html.V_POST).setAction("?")) {
                form.addTextInput().setNameValue("text", "Sample text");
                form.addDiv().addButton().addText("Evaluate");
                form.addDiv("out").addText("Result will appear here.");
            }
        }
    }
}

/** Handles the AJAX POST request */
JsonBuilder doAjax(HttpContext context, JsonBuilder output) {
    var text = context.getParameter("text", "");
    var result = "Processed: " + text;
    output.writeClass("out", e -> e.addDiv("out", Html.SPAN).addText(result));
    return output;
}
```

## Maven Dependencies & Setup

To add the HTML builder to your project, include the dependency on the `ujo-web` module in your `pom.xml`. **Java 17 or higher** is required.

```xml
<dependencies>
    <dependency>
        <groupId>org.ujorm</groupId>
        <artifactId>ujo-web</artifactId>
        <version>3.0.0-RC3</version>
    </dependency>
</dependencies>
```

---

## Benchmarks

Generating HTML directly in Java without using reflection and without complex background parsing ensures the maximum possible performance. Compared to traditional templating engines, `Element` provides significantly higher throughput and minimal memory load, reducing pressure on the Garbage Collector.

**Full benchmark source code and results:** 👉 [GitHub: html-benchmarks](https://github.com/pponec/html-benchmarks?tab=readme-ov-file#html-builder-benchmark)

---

## Related Links

* [Ujorm](https://github.com/pponec/ujorm/tree/ujorm3?tab=readme-ov-file#-ujorm3-library) - The main project page for the ORM library.
* [Petstore](https://github.com/pponec/ujorm-petstore?tab=readme-ov-file#ujorm-petstore) - A demonstration project combining Ujorm ORM and Ujorm Web (Element).
* [Benchmark test Ujorm Element](https://github.com/pponec/html-benchmarks?tab=readme-ov-file#html-builder-benchmark) - A performance comparison of Java HTML generation tools.